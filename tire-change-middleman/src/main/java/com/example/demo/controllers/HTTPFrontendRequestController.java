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

    public void setServerCommunicationController(ServerCommunicationController serverCommunicationController) {
        this.serverCommunicationController = serverCommunicationController;
    }

    @Autowired
    private ServerCommunicationController serverCommunicationController;
    public HTTPFrontendRequestController(Environment env,ServerCommunicationController serverCommunicationController) {
        this.env = env;
        this.serverCommunicationController = serverCommunicationController;
    }

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
        List<TireReplacementTimeSlot> pickedDayTimeSlots = serverCommunicationController.routeGetRequestSending(urlXML,urlJSON,workshopName,endDate);
        for (TireReplacementTimeSlot timeSlot : pickedDayTimeSlots){

            if (areSameMoment(timeSlot.getTireReplacementTime(), beginTime)) {

                if (getListFromEnvironmentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    ResponseEntity<String> response = serverCommunicationController.sendUpdateRequest(workshopName,timeSlot.getId());
                    if (response.getStatusCode().is2xxSuccessful()){
                        return ResponseEntity.status(HttpStatus.OK).body("Time booked successfully");
                    }
                    if (!response.getStatusCode().is2xxSuccessful()){ // If something went wrong(workshop server errors mainly).
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, try again.");
                    }

                }
                if (!getListFromEnvironmentProperties(env,"servers.allowedVehicles." + workshopName).contains(vehicleType)){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This workshop does not service the vehicle type of " +  vehicleType);
                }

            }

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such available timeslot exists");

    }
    static boolean areSameMoment(String TimeString1, String TimeString2) {
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse(TimeString1, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime offsetDateTime2 = OffsetDateTime.parse(TimeString2, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return offsetDateTime1.isEqual(offsetDateTime2);
    }




    @GetMapping("/filter")
    public ResponseEntity<List<TireReplacementTimeSlot>> handleGetRequest(@RequestParam String beginTime,
                                                                          @RequestParam String endTime,
                                                                          @RequestParam String vehicleTypes,
                                                                          @RequestParam String workshopPick) {

        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
        List<String> vehicleTypesList = new ArrayList<>();

        if (vehicleTypes.equals("any"))
            vehicleTypesList = getListFromEnvironmentProperties(env,"servers.allServiceableCarTypes");

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
            List<String> workshopAllowedVehiclesList = getListFromEnvironmentProperties(env,"servers.allowedVehicles." + workshopPick);
            if (!vehicleTypes.equals("any")){
                vehicleTypesList = new ArrayList<>();
                vehicleTypesList.add(vehicleTypes);
            }

            if (haveCommonElements(vehicleTypesList,workshopAllowedVehiclesList)) // If this warehouse can service the needed vehicle
                timeSlots.addAll(serverCommunicationController.routeGetRequestSending(urlXML,urlJSON, workshopPick,endTime));
        }
        if (Objects.equals(workshopPick, "any")){

            List<String> workshopList = getListFromEnvironmentProperties(env,"servers.list");

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
                List<String> workshopAllowedVehiclesList = getListFromEnvironmentProperties(env,"servers.allowedVehicles." + workshopName);

                if (!vehicleTypes.equals("any")){
                    vehicleTypesList = new ArrayList<>();
                    vehicleTypesList.add(vehicleTypes);
                }

                if (!haveCommonElements(vehicleTypesList,workshopAllowedVehiclesList)) // If this warehouse cannot service the needed vehicle
                    timeSlots = new ArrayList<>();
                if (haveCommonElements(vehicleTypesList,workshopAllowedVehiclesList)) // If this warehouse can service the needed vehicle
                    timeSlots.addAll(serverCommunicationController.routeGetRequestSending(urlXML, urlJSON, workshopName, endTime));
            }

        }
        if (timeSlots.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(timeSlots);

        return ResponseEntity.ok(timeSlots);

    }




    /**
     * Determines if the date from the string dateStr is before or on the same day as
     * the date part of the dateTimeStr.
     *
     * @param dateStr     The string representing the date (e.g., "2024-08-21").
     * @param dateTimeStr The string representing the date-time (e.g., "2024-08-21T10:15:30+01:00").
     * @return true if dateStr refers to a time before or on the same day as dateTimeStr, false otherwise.
     */
    static boolean isDateBeforeOrEqualDateTime(String dateStr, String dateTimeStr) {
        // Parse the first date string to LocalDate
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

        // Parse the second date-time string to ZonedDateTime
        ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);

        // Compare the LocalDate to the date part of the ZonedDateTime
        return !date.isAfter(dateTime.toLocalDate());
    }


    static List<String> getListFromEnvironmentProperties(Environment env, String ConfProperties){
        // Retrieve the property value as a comma-separated string
        String workShops = env.getProperty(ConfProperties);

        if (workShops.isEmpty()) // This avoids the potential passing of an empty String as a variable.
            return new ArrayList<>();

        // Convert the comma-separated string to a List
        List<String> propertiesList = Arrays.asList(workShops.split(","));
        return propertiesList;
    }


    static boolean haveCommonElements(List<String> list1, List<String> list2) {
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
    static String tryGetTextContent(Element element, String tagName) {
        Node node = element.getElementsByTagName(tagName).item(0);
        return (node != null) ? node.getTextContent() : null;
    }

}