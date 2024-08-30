package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
class HTTPFrontendRequestControllerTest {
    @Mock
    private Environment env;

    private ServerCommunicationController serverCommunicationControllerSpy;

    @InjectMocks
    private HTTPFrontendRequestController frontendController;

    @BeforeEach
    void setUp() {
        ServerCommunicationController serverCommunicationController = new ServerCommunicationController(env);
        serverCommunicationControllerSpy = spy(serverCommunicationController);

        // Inject the spy into the frontendController manually
        frontendController.setServerCommunicationController(serverCommunicationControllerSpy);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void test_handlePostRequest_successfulBooking(){
        // Arrange
        String workshopName = "london";
        when(env.getProperty("servers.contactInformation")).thenReturn("Tires Global");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");

        when(env.getProperty("servers.port." + workshopName)).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host." + workshopName)).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.book." + workshopName)).thenReturn("booking");
        when(env.getProperty("servers.bookingMethod." + workshopName)).thenReturn("PUT");
        when(env.getProperty("servers.address.get." + workshopName)).thenReturn("available");
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("1A Gunton Rd, London");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("car");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("XML");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");


        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        // Act
        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"car",workshopName);

        assertFalse(response.getBody().isEmpty());
        assertEquals(response.getBody(),"Time booked successfully");
        assertEquals(response.getStatusCode(),HttpStatus.OK);

    }
    @Test
    void test_handlePostRequest_wrongVehicle(){
        // Arrange
        String workshopName = "london";

        when(env.getProperty("servers.port." + workshopName)).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host." + workshopName)).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get." + workshopName)).thenReturn("available");
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("1A Gunton Rd, London");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("car");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("XML");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");


        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","car",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        // Act
        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"truck",workshopName);

        assertFalse(response.getBody().isEmpty());
        assertEquals(response.getBody(),"This workshop does not service the vehicle type of truck");
        assertEquals(response.getStatusCode(),HttpStatus.BAD_REQUEST);

    }
    @Test
    void test_handlePostRequest_unavailableTime(){
        // Arrange
        String workshopName = "london";
        when(env.getProperty("servers.contactInformation")).thenReturn("Tires Global");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");

        when(env.getProperty("servers.port." + workshopName)).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host." + workshopName)).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.book." + workshopName)).thenReturn("booking");
        when(env.getProperty("servers.bookingMethod." + workshopName)).thenReturn("PUT");
        when(env.getProperty("servers.address.get." + workshopName)).thenReturn("available");
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("1A Gunton Rd, London");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("car");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("XML");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");


        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        // Act
        frontendController.handlePostRequest(availableTime,"car",workshopName); // Book an available time
        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"car",workshopName); // Book that time again

        assertFalse(response.getBody().isEmpty());
        assertEquals(response.getBody(),"No such available timeslot exists");
        assertEquals(response.getStatusCode(),HttpStatus.NOT_FOUND);

    }


    //areSameMoment(String offsetDateTimeStr1, String offsetDateTimeStr2)
	/*
	1. (offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00", offsetDateTimeStr1 = "2023-08-21T14:30:00+02:00"): return true
	2. (offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00", offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00"): return true
	3. (offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00", offsetDateTimeStr1 = "2023-08-21T13:30:00+00:00): return false
	*/

    @Test
    void test_areSameMoment_DifferentFormatSameMoment() {

        // Same Moment, Different Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T14:30:00+02:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");
}

    @Test
    void test_areSameMoment_SameFormatSameMoment() {
        // Same Moment, Same Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T12:30:00+00:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");}
    @Test
    void test_areSameMoment_SameFormatDifferentMoment() {
        // Different Moment, Different Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T13:30:00+00:00";
        assertFalse(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered different moments");
    }







    //handleGetRequest(String beginTime, String endTime, String vehicleTypes, String workshopPick)
	/*
	1. (beginTime = "2023-08-21T12:30:00+00:00", endTime = "2023-08-21T14:30:00+00:00", vehicleTypes = "any", workshopPick = "any"):
	return ResponseEntity(OK, {(workshop "london", vehicleType "car,truck"),(workshop "london", vehicleType "car,truck"),(workshop "manchester", vehicleType "truck")})

	2. (offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00", offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00"): return true
	3. (offsetDateTimeStr1 = "2023-08-21T12:30:00+00:00", offsetDateTimeStr1 = "2023-08-21T13:30:00+00:00): return false
	*/

    @Test
    void test_handleGetRequest_AnyWorkshopAnyVehicle() {
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "any";
        String workshopPick = "any";


        // Configuring the env to return added specific values.
        when(env.getProperty("servers.list")).thenReturn("london,manchester");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");
        // Setup for London server properties
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.localTimezoneOffset.london")).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat.london")).thenReturn("XML");
        when(env.getProperty("servers.physicalAddress.london")).thenReturn("1A Gunton Rd, London");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");


        // Setup for Manchester server properties
        when(env.getProperty("servers.port.manchester")).thenReturn("http://localhost:9004/");
        when(env.getProperty("servers.host.manchester")).thenReturn("api/v2/tire-change-times/");
        when(env.getProperty("servers.address.get.manchester")).thenReturn("");
        when(env.getProperty("servers.physicalAddress.manchester")).thenReturn("14 Bury New Rd, Manchester");
        when(env.getProperty("servers.allowedVehicles.manchester")).thenReturn("car,truck");
        when(env.getProperty("servers.localTimezoneOffset.manchester")).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat.manchester")).thenReturn("JSON");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.manchester")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.manchester")).thenReturn("0");


        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> response = frontendController.handleGetRequest(beginTime, endTime, vehicleTypes, workshopPick);


        // Optionally, you can also assert the response to check if it has the expected data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Check if there are elements with "workshopName" values of "london" and "manchester"
        List<TireReplacementTimeSlot> timeSlots = response.getBody();
        assertNotNull(timeSlots);

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));
        boolean hasCarVehicle = timeSlots.stream().anyMatch(slot -> "car".equals(slot.getVehicleTypesServiced()));
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        assertTrue(hasLondon, "Expected a time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertTrue(hasCarVehicle, "Expected a time slot with only 'car' being serviced");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");


    }
    @Test
    void test_handleGetRequest_AnyWorkshopTruckVehicle() {
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "truck";
        String workshopPick = "any";

        // Configures the env to return properties for url creation.
        when(env.getProperty("servers.list")).thenReturn("london,manchester");
        // Setup for London server properties
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Setup for Manchester server properties
        when(env.getProperty("servers.port.manchester")).thenReturn("http://localhost:9004/");
        when(env.getProperty("servers.host.manchester")).thenReturn("api/v2/tire-change-times/");
        when(env.getProperty("servers.address.get.manchester")).thenReturn("");
        when(env.getProperty("servers.physicalAddress.manchester")).thenReturn("14 Bury New Rd, Manchester");
        when(env.getProperty("servers.allowedVehicles.manchester")).thenReturn("car,truck");
        when(env.getProperty("servers.localTimezoneOffset.manchester")).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat.manchester")).thenReturn("JSON");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.manchester")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.manchester")).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> response = frontendController.handleGetRequest(beginTime, endTime, vehicleTypes, workshopPick);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<TireReplacementTimeSlot> timeSlots = response.getBody();
        assertNotNull(timeSlots);

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));
        boolean hasCarVehicle = timeSlots.stream().anyMatch(slot -> "car".equals(slot.getVehicleTypesServiced()));
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        assertFalse(hasLondon, "Expected no time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertFalse(hasCarVehicle, "Expected a time slot with only 'car' not being serviced");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");


    }
    @Test
    void test_handleGetRequest_manchesterWorkshopAnyVehicle() {
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "any";
        String workshopPick = "manchester";
        // Configures the env to return properties for url creation.
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");
        // Setup for Manchester server properties
        when(env.getProperty("servers.port.manchester")).thenReturn("http://localhost:9004/");
        when(env.getProperty("servers.host.manchester")).thenReturn("api/v2/tire-change-times/");
        when(env.getProperty("servers.address.get.manchester")).thenReturn("");
        when(env.getProperty("servers.physicalAddress.manchester")).thenReturn("14 Bury New Rd, Manchester");
        when(env.getProperty("servers.allowedVehicles.manchester")).thenReturn("car,truck");
        when(env.getProperty("servers.localTimezoneOffset.manchester")).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat.manchester")).thenReturn("JSON");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.manchester")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.manchester")).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> response = frontendController.handleGetRequest(beginTime, endTime, vehicleTypes, workshopPick);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<TireReplacementTimeSlot> timeSlots = response.getBody();
        assertNotNull(timeSlots);

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));;
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        assertFalse(hasLondon, "Expected no time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");


    }
    @Test
    void test_handleGetRequest_londonWorkshopTruckVehicle() {
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "truck";
        String workshopPick = "london";

        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> response = frontendController.handleGetRequest(beginTime, endTime, vehicleTypes, workshopPick);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());

        List<TireReplacementTimeSlot> timeSlots = response.getBody();
        assertTrue(timeSlots.isEmpty());

    }







    //isDateBeforeOrEqualDateTime(String dateStr, String dateTimeStr)
	/*
	1. (dateStr = 2023-08-20, dateTimeStr = "2023-08-21T12:30:00+00:00"): return true
	2. (dateStr = 2023-08-21, dateTimeStr = "2023-08-21T12:30:00+00:00"): return true
	3. (dateStr = 2023-08-22, dateTimeStr = "2023-08-21T12:30:00+00:00"): return false
	*/


    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsBefore() {
        String dateStr = "2023-08-20";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";
        assertTrue(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));

    }
    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsEqual() {
        String dateStr = "2023-08-21";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";
        assertTrue(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));
    }
    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsAfter() {
        String dateStr = "2023-08-22";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";
        assertFalse(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));
    }



    //getListFromEnvironmentProperties(Environment env, String ConfProperties)
	/*
	1. (env contains the value "car,truck,motorcycle", list2 = "servers.allowedVehicles.london"): return {car,truck,motorcycle}
	2. (env contains the value "car", list2 = "servers.allowedVehicles.london"): return {car}
	3. (env contains the value "", list2 = "servers.allowedVehicles.london"): return {} (empty list)
	*/

    @Test
    void test_getListFromEnvironmentProperties_LongList(){

        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car,truck,motorcycle");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Arrays.asList("car","truck","motorcycle");

        assertEquals(list1,list2);
    }
    @Test
    void test_getListFromEnvironmentProperties_SingletonList(){
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Collections.singletonList("car");

        assertEquals(list1,list2);
    }
    @Test
    void test_getListFromEnvironmentProperties_EmptyList(){
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = new ArrayList<>();

        assertEquals(list1,list2);
    }

//haveCommonElements(List<String> list1, List<String> list2)
	/*
	1. (list1 = {truck, car}, list2 = {car}): return true
	2. (list1 =  {motorcycle}, list2 = {truck, car}): return false
	3. list1 = {truck}, list2 = {car}: return false
	*/
    @Test
    void test_haveCommonElements_OneCommonElement() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("car");
        assertTrue(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
    @Test
    void test_haveCommonElements_NoCommonElementsLargerList() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("motorcycle");
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }




}
