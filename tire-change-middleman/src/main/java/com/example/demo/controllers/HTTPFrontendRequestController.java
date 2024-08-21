package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class HTTPFrontendRequestController {
    @Autowired
    private Environment env;

    @PostMapping("/book")
    public ResponseEntity<String> handlePostRequest(@RequestParam String beginTime,
                                  @RequestParam String vehicleType,
                                  @RequestParam String workshopName) {

        String serverPort = env.getProperty("servers.port." + workshopName);
        String serverHost = env.getProperty("servers.host." + workshopName);
        String serverGetAddress = env.getProperty("servers.address.get." + workshopName);
        String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName);
        String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName);
        // Parse the string into a LocalDateTime object
        LocalDateTime dateTime = LocalDateTime.parse(beginTime);

        // Extract the date part as a string
        String beginDate = dateTime.toLocalDate().toString();

        // Add one day to the date part
        String endDate = dateTime.toLocalDate().plusDays(1).toString();


        // Construct the full URL for both XML and JSON responses.
        String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginDate + "&until=" + endDate;
        String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginDate;

        // Since get and put requests need Id-s, it is needed to gather the timeslots of the picked day to find the one corresponding to the needed time.
        List<TireReplacementTimeSlot> pickedDayTimeSlots = sendGetRequest(urlXML,urlJSON,workshopName,endDate);
        for (TireReplacementTimeSlot timeSlot : pickedDayTimeSlots){

            if (areSameMoment(timeSlot.getTireReplacementTime().toString(),beginTime)) {
                if (getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    String response = sendUpdateRequest(workshopName,timeSlot.getId(),env);
                    return ResponseEntity.ok(response);
                }
                if (!getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This workshop does not service the vehicle type of " +  vehicleType);
                }


            }

        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No such available timeslot exists");
        // Implement booking logic here

    }
    private static String sendUpdateRequest(String workshopName, String id, Environment env){
        // Construct the URL based on the properties
        String serverPort = env.getProperty("servers.port." + workshopName);
        String serverHost = env.getProperty("servers.host." + workshopName);
        String serverBookAddress = env.getProperty("servers.address.book." + workshopName);
        // Construct the full URL for the PUT request
        String url = serverPort + serverHost + id + "/" + serverBookAddress;
        String bookMethod = env.getProperty("servers.bookingMethod." + workshopName);
        RestTemplate restTemplate = new RestTemplate();
        String bookingResponse = null;

        if (Objects.equals(bookMethod, "PUT")){
            bookingResponse = restTemplate.exchange(url, HttpMethod.PUT, null, String.class).getBody();
        }
        if (Objects.equals(bookMethod, "POST")){
            bookingResponse = restTemplate.exchange(url, HttpMethod.POST, null, String.class).getBody();
        }

        return bookingResponse;

    }
    public static boolean areSameMoment(String offsetDateTimeStr, String localDateTimeStr) {
        // Return true, if the two Strings refer to the same moment. Otherwise, return false.

        // Parse the first string to LocalDateTime (assumed to be in local system time)
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Parse the second string to OffsetDateTime (UTC time)
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(offsetDateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Extract the time zone offset from the OffsetDateTime
        ZoneOffset offset = offsetDateTime.getOffset();

        // Convert the LocalDateTime to OffsetDateTime with the extracted offset
        OffsetDateTime localDateTimeWithOffset = localDateTime.atOffset(offset);

        // Compare the two OffsetDateTime objects
        return localDateTimeWithOffset.equals(offsetDateTime);
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


    private List<TireReplacementTimeSlot> parseXML(String workshopName, String urlString)  {

        // Make the HTTP GET request

        List<TireReplacementTimeSlot> timeSlotsList = new ArrayList<>();

            // Parse the XML response
            try {
                URL getURL = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
                connection.setRequestMethod("GET");
                try (InputStream inputStream = connection.getInputStream()) {


                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();// Making parser to read XML.
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(inputStream);// Parsing XML to get it readable for code.

                    NodeList timeslotNodes = document.getElementsByTagName("availableTime");// Separating data by different stations.
                    for (int i = 0; i < timeslotNodes.getLength(); i++) {// Convert the parsed data to TireReplacementTimeSlot objects

                        Element timeslotElement = (Element) timeslotNodes.item(i);// Get element with index i in a station.
                        timeSlotsList.add(new TireReplacementTimeSlot(
                                workshopName,
                                env.getProperty("servers.physicalAddress." + workshopName),
                                trygetTextContent(timeslotElement, "uuid"),
                                trygetTextContent(timeslotElement, "time"),
                                env.getProperty("servers.allowedVehicles." + workshopName)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return timeSlotsList;

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
    private static String trygetTextContent(Element element, String tagName) {
        Node node = element.getElementsByTagName(tagName).item(0);
        return (node != null) ? node.getTextContent() : null;
    }

}
