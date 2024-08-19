package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import com.example.demo.services.TireChangeTimesGetResponseXMLService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.StringReader;
import java.util.*;

@RestController
public class HTTPFrontendRequestController {
    @Autowired
    private Environment env;

    @PostMapping("/book")
    public ResponseEntity<String> handlePostRequest(@RequestParam String beginTime,
                                  @RequestParam String vehicleType,
                                  @RequestParam String workshopName) {
        System.out.println(beginTime);
        System.out.println(vehicleType);
        System.out.println(workshopName);
        // Implement booking logic here
        return ResponseEntity.ok("Time booked successfully");
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TireReplacementTimeSlot>> handleGetRequest(@RequestParam String beginTime,
                                                          @RequestParam String endTime,
                                                          @RequestParam String vehicleTypes,
                                                          @RequestParam String workshopPick) {

        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
        List<String> vehicleTypesList = new ArrayList<>();

        if (vehicleTypes.equals("any"))
            vehicleTypesList = getListFromEnviromentProperties(env,"servers.allServiceableCarTypes");

        if (!Objects.equals(workshopPick, "any")){ // If a specific workshop was picked.
            // Fetching property values within the method
            String serverPort = env.getProperty("servers.port." + workshopPick);
            String serverHost = env.getProperty("servers.host." + workshopPick);
            String serverGetAddress = env.getProperty("servers.address.get." + workshopPick);
            String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopPick);
            String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopPick);
            // Construct the full URL for both XML and JSON responses.
            String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
            String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;
            List<String> workshopAllowedVehiclesList = getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopPick);
            if (!vehicleTypes.equals("any")){
                vehicleTypesList = new ArrayList<>();
                vehicleTypesList.add(vehicleTypes);
            }

            if (haveCommonElements(vehicleTypesList,workshopAllowedVehiclesList)) // If this warehouse can service the needed vehicle
                timeSlots.addAll(sendGetRequest(urlXML,urlJSON, workshopPick,endTime));
    }
        if (Objects.equals(workshopPick, "any")){

            List<String> workshopList = getListFromEnviromentProperties(env,"servers.list");

            for (String workshopName : workshopList) {
                // Fetching property values within the method
                String serverPort = env.getProperty("servers.port." + workshopName);
                String serverHost = env.getProperty("servers.host." + workshopName);
                String serverGetAddress = env.getProperty("servers.address.get." + workshopName);
                String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName);
                String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName);
                // Construct the full URL for both XML and JSON responses.
                String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
                String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;
                List<String> workshopAllowedVehiclesList = getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopName);

                if (!vehicleTypes.equals("any")){
                    vehicleTypesList = new ArrayList<>();
                    vehicleTypesList.add(vehicleTypes);
                }

                if (haveCommonElements(vehicleTypesList,workshopAllowedVehiclesList)) // If this warehouse can service the needed vehicle
                    timeSlots.addAll(sendGetRequest(urlXML, urlJSON, workshopName, endTime));
            }

        }
        if (timeSlots.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(timeSlots);

        return ResponseEntity.ok(timeSlots);

    }


    private List<TireReplacementTimeSlot> sendGetRequest(String urlXML, String urlJSON, String workshopName,String endTime){
        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();

        if (Objects.equals(env.getProperty("servers.responseBodyFormat." + workshopName), "XML")) {// If the format is XML
            timeSlots = parseXML(workshopName,urlXML);

        }

        if (Objects.equals(env.getProperty("servers.responseBodyFormat." + workshopName), "JSON")) {// If the format is JSON
            timeSlots = parseJSON(workshopName,urlJSON,endTime);
        }
        return timeSlots;

    }


    private List<TireReplacementTimeSlot> parseXML(String workshopName, String url){

        // Make the HTTP GET request
        RestTemplate restTemplate = new RestTemplate();
        String XMLData = restTemplate.getForObject(url, String.class);
        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
            // Parse the XML response
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(TireChangeTimesGetResponseXMLService.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                TireChangeTimesGetResponseXMLService tireChangeTimesResponse =
                        (TireChangeTimesGetResponseXMLService) unmarshaller.unmarshal(new StringReader(XMLData));

                // Convert the parsed data to TireReplacementTimeSlot objects
                for (TireChangeTimesGetResponseXMLService.AvailableTime availableTime : tireChangeTimesResponse.getAvailableTimes()) {
                    timeSlots.add(new TireReplacementTimeSlot(
                            workshopName,
                            env.getProperty("servers.physicalAddress." + workshopName), // Get physical address from properties
                            availableTime.getUuid(),
                            availableTime.getTime(),
                            env.getProperty("servers.allowedVehicles." + workshopName)
                    ));
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return timeSlots;

}


    private List<TireReplacementTimeSlot> parseJSON(String workshopName, String url,String endTime) {
        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();

        // Make the HTTP GET request
        RestTemplate restTemplate = new RestTemplate();
        String jsonData = restTemplate.getForObject(url, String.class);

        try {
            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonData);

            // Iterate through the JSON array and filter available times
            for (JsonNode node : rootNode) {

                boolean available = node.get("available").asBoolean();

                String id = node.get("id").asText();
                String time = node.get("time").asText();

                if (isDateBeforeDateTime(endTime,time)) // Stops the reading when endDate is reached.
                    break;
                if (available) {

                    // Create a new TireReplacementTimeSlot object and add it to the list
                    TireReplacementTimeSlot timeSlot = new TireReplacementTimeSlot(
                            workshopName,
                            env.getProperty("servers.physicalAddress." + workshopName), // Get physical address from properties
                            id,
                            time,
                            env.getProperty("servers.allowedVehicles." + workshopName)
                    );
                    timeSlots.add(timeSlot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return timeSlots;
    }


    public static boolean isDateBeforeDateTime(String dateStr, String dateTimeStr) {
        // Returns true, if the parameter dateStr refers to a time after that of the dateTimeStr

        // Parse the first date string to LocalDate
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

        // Parse the second date-time string to ZonedDateTime
        ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);

        // Compare the LocalDate to the date part of the ZonedDateTime
        return date.isBefore(dateTime.toLocalDate());
    }


    private static List<String> getListFromEnviromentProperties(Environment env, String ConfProperties){
        // Retrieve the property value as a comma-separated string
        String workShops = env.getProperty(ConfProperties);
        // Convert the comma-separated string to a List
        List<String> propertiesList = Arrays.asList(workShops.split(","));
        return propertiesList;
    }


    public static boolean haveCommonElements(List<String> list1, List<String> list2) {
        // Convert the first list to a Set for faster lookup
        Set<String> set = new HashSet<>(list1);

        // Iterate through the second list and check if any element is in the set
        for (String element : list2) {
            if (set.contains(element)) {
                return true; // A common element is found
            }
        }

        // No common elements found
        return false;
    }
}
