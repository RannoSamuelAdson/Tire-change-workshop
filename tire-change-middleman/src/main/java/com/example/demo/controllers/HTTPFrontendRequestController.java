package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class HTTPFrontendRequestController {

    @Autowired
    private Environment env;

    @Autowired
    private ServerCommunicationController serverCommunicationController;

    public HTTPFrontendRequestController(Environment env, ServerCommunicationController serverCommunicationController) {
        this.env = env;
        this.serverCommunicationController = serverCommunicationController;
    }

    public void setServerCommunicationController(ServerCommunicationController serverCommunicationController) {
        this.serverCommunicationController = serverCommunicationController;
    }

    /**
     * Handles a POST request to book a tire replacement time slot.
     * Sends a GET request to a server to find the time slot picked.
     * If compatible and found, sends an update request to the server
     *
     * @param beginTime   The beginning time of the time slot.
     * @param vehicleType The type of vehicle.
     * @param workshopName The name of the workshop.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/book")
    public ResponseEntity<String> handlePostRequest(@RequestParam String beginTime,
                                                    @RequestParam String vehicleType,
                                                    @RequestParam String workshopName) {

        String serverPort = env.getProperty("servers.port." + workshopName);
        String serverHost = env.getProperty("servers.host." + workshopName);
        String serverGetAddress = env.getProperty("servers.address.get." + workshopName);
        String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName);
        String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName);

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(beginTime);
        String beginDate = offsetDateTime.toLocalDate().toString();
        String endDate = offsetDateTime.toLocalDate().plusDays(1).toString();

        String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginDate + "&until=" + endDate;
        String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginDate;

        // Gets the timeslots of that day.
        List<TireReplacementTimeSlot> pickedDayTimeSlots = serverCommunicationController.routeGetRequestSending(urlXML, urlJSON, workshopName, endDate);

        if (!getListFromEnvironmentProperties(env, "servers.allowedVehicles." + workshopName).contains(vehicleType))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This workshop does not service the vehicle type of " + vehicleType);

        // Cycles through the time slots gathered.
        for (TireReplacementTimeSlot timeSlot : pickedDayTimeSlots) {

            if (areSameMoment(timeSlot.getTireReplacementTime(), beginTime)) {

                ResponseEntity<String> response = serverCommunicationController.sendUpdateRequest(workshopName, timeSlot.getId());

                if (response.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(HttpStatus.OK).body("Time booked successfully");
                }

                if (!response.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, try again.");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such available timeslot exists");
    }

    /**
     * Checks if two date-time strings represent the same moment.
     *
     * @param TimeString1 The first date-time string.
     * @param TimeString2 The second date-time string.
     * @return true if they represent the same moment, false otherwise.
     */
    static boolean areSameMoment(String TimeString1, String TimeString2) {
        OffsetDateTime offsetDateTime1 = OffsetDateTime.parse(TimeString1, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime offsetDateTime2 = OffsetDateTime.parse(TimeString2, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return offsetDateTime1.isEqual(offsetDateTime2);
    }

    /**
     * Handles a GET request to filter tire replacement time slots.
     * Routes differently if "vehicleTypes" or "workshopPick" is one specific element or "any".
     * Ensures only picked workshops with compatible vehicle types are queried.
     *
     * @param beginTime    The beginning time for filtering.
     * @param endTime      The ending time for filtering.
     * @param vehicleTypes The types of vehicles to filter by. Can be a specific type or "any".
     * @param workshopPick The workshop(s) to filter by. Can be a specific workshop or "any".
     * @return ResponseEntity with a list of TireReplacementTimeSlot objects.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TireReplacementTimeSlot>> handleGetRequest(@RequestParam String beginTime,
                                                                          @RequestParam String endTime,
                                                                          @RequestParam String vehicleTypes,
                                                                          @RequestParam String workshopPick) {

        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
        List<String> vehicleTypesList = new ArrayList<>();

        if (vehicleTypes.equals("any"))
            vehicleTypesList = getListFromEnvironmentProperties(env, "servers.allServiceableCarTypes");

        if (!Objects.equals(workshopPick, "any")) {
            String serverPort = env.getProperty("servers.port." + workshopPick);
            String serverHost = env.getProperty("servers.host." + workshopPick);
            String serverGetAddress = env.getProperty("servers.address.get." + workshopPick);
            String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopPick);
            String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopPick);

            String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
            String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;
            List<String> workshopAllowedVehiclesList = getListFromEnvironmentProperties(env, "servers.allowedVehicles." + workshopPick);

            if (!vehicleTypes.equals("any")) {
                vehicleTypesList = new ArrayList<>();
                vehicleTypesList.add(vehicleTypes);
            }

            if (haveCommonElements(vehicleTypesList, workshopAllowedVehiclesList))
                timeSlots.addAll(serverCommunicationController.routeGetRequestSending(urlXML, urlJSON, workshopPick, endTime));
        }

        if (Objects.equals(workshopPick, "any")) {
            List<String> workshopList = getListFromEnvironmentProperties(env, "servers.list");

            for (String workshopName : workshopList) {
                String serverPort = env.getProperty("servers.port." + workshopName);
                String serverHost = env.getProperty("servers.host." + workshopName);
                String serverGetAddress = env.getProperty("servers.address.get." + workshopName);
                String pageAmount = env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName);
                String pageSkipAmount = env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName);

                String urlXML = serverPort + serverHost + serverGetAddress + "?from=" + beginTime + "&until=" + endTime;
                String urlJSON = serverPort + serverHost + serverGetAddress + "?amount=" + pageAmount + "&page=" + pageSkipAmount + "&from=" + beginTime;
                List<String> workshopAllowedVehiclesList = getListFromEnvironmentProperties(env, "servers.allowedVehicles." + workshopName);

                if (!vehicleTypes.equals("any")) {
                    vehicleTypesList = new ArrayList<>();
                    vehicleTypesList.add(vehicleTypes);
                }

                if (haveCommonElements(vehicleTypesList, workshopAllowedVehiclesList))
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
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        return !date.isAfter(dateTime.toLocalDate());
    }

    /**
     * Retrieves a list of properties from the environment as a list of strings.
     *
     * @param env           The Environment object.
     * @param ConfProperties The property name to retrieve.
     * @return A list of strings containing the properties.
     */
    static List<String> getListFromEnvironmentProperties(Environment env, String ConfProperties) {
        String workShops = env.getProperty(ConfProperties);

        if (workShops.isEmpty())
            return new ArrayList<>();

        List<String> propertiesList = Arrays.asList(workShops.split(","));
        return propertiesList;
    }

    /**
     * Determines if two lists have any common elements.
     *
     * @param list1 The first list of strings.
     * @param list2 The second list of strings.
     * @return true if there are common elements, false otherwise.
     */
    static boolean haveCommonElements(List<String> list1, List<String> list2) {
        Set<String> set = new HashSet<>(list1);

        for (String element : list2) {
            if (set.contains(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to get the text content from an XML element by tag name.
     *
     * @param element The XML element.
     * @param tagName The tag name to search for.
     * @return The text content of the element, or null if not found.
     */
    static String tryGetTextContent(Element element, String tagName) {
        Node node = element.getElementsByTagName(tagName).item(0);
        return (node != null) ? node.getTextContent() : null;
    }
}
