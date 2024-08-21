package com.example.demo.controllers;

import com.example.demo.models.TireChangeBookingRequest;
import com.example.demo.models.TireReplacementTimeSlot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
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
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(beginTime);


        // Extract the date part as a string
        String beginDate = offsetDateTime.toLocalDate().toString();

        // Add one day to the date part
        String endDate = offsetDateTime.toLocalDate().plusDays(1).toString();


        // Construct the full URL for both XML and JSON responses.
        String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginDate + "&until=" + endDate;
        String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginDate;

        // Since get and put requests need Id-s, it is needed to gather the timeslots of the picked day to find the one corresponding to the needed time.
        List<TireReplacementTimeSlot> pickedDayTimeSlots = sendGetRequest(urlXML,urlJSON,workshopName,endDate);
        for (TireReplacementTimeSlot timeSlot : pickedDayTimeSlots){
            String workshopTimezoneOffsetString = (env.getProperty("servers.localTimezoneOffset." + workshopName));
            int workshopTimezoneOffsetInt = Integer.parseInt(Objects.requireNonNull(workshopTimezoneOffsetString));

            if (areSameMoment(timeSlot.getTireReplacementTime(), beginTime)) {

                if (getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    ResponseEntity<String> response = sendUpdateRequest(workshopName,timeSlot.getId(),env);
                    if (response.getStatusCode().is2xxSuccessful()){
                        return ResponseEntity.ok("Time booked successfully");
                    }
                    if (!response.getStatusCode().is2xxSuccessful()){ // If something went wrong(workshop server errors mainly).
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, try again.");
                    }

                }
                if (!getListFromEnviromentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This workshop does not service the vehicle type of " +  vehicleType);
                }


            }

        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No such available timeslot exists");
        // Implement booking logic here

    }
    public static boolean areSameMoment(String offsetDateTimeStr1, String offsetDateTimeStr2) {
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse(offsetDateTimeStr1, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime offsetDateTime2 = OffsetDateTime.parse(offsetDateTimeStr2, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return offsetDateTime1.isEqual(offsetDateTime2);
    }
    private static ResponseEntity<String> sendUpdateRequest(String workshopName, String id, Environment env){
        // Construct the URL based on the properties
        String serverPort = env.getProperty("servers.port." + workshopName);
        String serverHost = env.getProperty("servers.host." + workshopName);
        String serverBookAddress = env.getProperty("servers.address.book." + workshopName);
        // Construct the full URL for the PUT request
        String url = serverPort + serverHost + id + "/" + serverBookAddress;
        String bookMethod = env.getProperty("servers.bookingMethod." + workshopName);
        RestTemplate restTemplate = new RestTemplate();
        // Send the request
        ResponseEntity<String> bookingResponse = null;


        // Prepare the request body
        TireChangeBookingRequest bookingRequestBody = new TireChangeBookingRequest(env.getProperty("servers.contactInformation"));
        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        if (Objects.equals(bookMethod, "PUT")){
            headers.setContentType(MediaType.APPLICATION_XML);
            // Create the request entity with headers and body
            HttpEntity<TireChangeBookingRequest> requestEntity = new HttpEntity<>(bookingRequestBody, headers);
            bookingResponse = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        }
        if (Objects.equals(bookMethod, "POST")){
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Create the request entity with headers and body
            HttpEntity<TireChangeBookingRequest> requestEntity = new HttpEntity<>(bookingRequestBody, headers);
            bookingResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        }


        return bookingResponse;

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

                    NodeList timeslotNodes = document.getElementsByTagName("availableTime");// Separating data by different timeslots.
                    for (int i = 0; i < timeslotNodes.getLength(); i++) {// Convert the parsed data to TireReplacementTimeSlot objects

                        Element timeslotElement = (Element) timeslotNodes.item(i);// Get element with index i in a station.
                        timeSlotsList.add(new TireReplacementTimeSlot(
                                workshopName,
                                env.getProperty("servers.physicalAddress." + workshopName),
                                trygetTextContent(timeslotElement, "uuid"),
                                trygetTextContent(timeslotElement, "time"),
                                Integer.parseInt(Objects.requireNonNull(env.getProperty("servers.localTimezoneOffset." + workshopName))),
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
                            Integer.parseInt(Objects.requireNonNull(env.getProperty("servers.localTimezoneOffset." + workshopName))),
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
