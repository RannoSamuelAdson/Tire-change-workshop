package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import com.example.demo.services.TireChangeTimesGetResponseXMLService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
public class HTTPFrontendRequestController {
    @Autowired
    private Environment env;

    @PostMapping("/book")
    public void handlePostRequest(@RequestParam String beginTime,
                                  @RequestParam String vehicleType,
                                  @RequestParam String workshopName) {
        System.out.println(beginTime);
        System.out.println(vehicleType);
        System.out.println(workshopName);
        // Implement booking logic here
    }

    @GetMapping("/filter")
    public List<TireReplacementTimeSlot> handleGetRequest(@RequestParam String beginTime,
                                                          @RequestParam String endTime,
                                                          @RequestParam String vehicleType,
                                                          @RequestParam String workshopName) {

        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();


        if (!Objects.equals(workshopName, "any")){
            // Fetching property values within the method
            String serverPort = env.getProperty("servers.port." + workshopName);
            String serverHost = env.getProperty("servers.host." + workshopName);
            String serverGetAddress = env.getProperty("servers.address.get." + workshopName);
            String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName);
            String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName);
            // Construct the full URL for both XML and JSON responses.
            String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
            String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;

            timeSlots.addAll(sendGetRequest(urlXML,urlJSON,workshopName));
        }
        if (Objects.equals(workshopName, "any")){
            // Retrieve the property value as a comma-separated string
            String workShops = env.getProperty("servers.list");
            // Convert the comma-separated string to a List
            List<String> workshopList = Arrays.asList(workShops.split(","));

            for (String workshop: workshopList){
                // Fetching property values within the method
                String serverPort = env.getProperty("servers.port." + workshop);
                String serverHost = env.getProperty("servers.host." + workshop);
                String serverGetAddress = env.getProperty("servers.address.get." + workshop);
                String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshop);
                String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshop);
                // Construct the full URL for both XML and JSON responses.
                String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
                String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;
                timeSlots.addAll(sendGetRequest(urlXML,urlJSON,workshop));
            }

        }
        return timeSlots;

    }
    private List<TireReplacementTimeSlot> sendGetRequest(String urlXML, String urlJSON, String workshopName){
        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
        if (Objects.equals(env.getProperty("servers.responseBodyFormat." + workshopName), "XML")) {// If the format is XML
            timeSlots = parseXML(workshopName,urlXML);

        }

        if (Objects.equals(env.getProperty("servers.responseBodyFormat." + workshopName), "JSON")) {// If the format is JSON
            timeSlots = parseJSON(workshopName,urlJSON);
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
    private List<TireReplacementTimeSlot> parseJSON(String workshopName, String url) {
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
                if (available) {
                    String id = node.get("id").asText();
                    String time = node.get("time").asText();

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

}
