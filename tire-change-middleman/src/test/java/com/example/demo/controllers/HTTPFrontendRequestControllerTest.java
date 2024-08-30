package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"car",workshopName);

        // Assert
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

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","car",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"truck",workshopName);

        //Assert
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

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String availableTime = timeSlot.getTireReplacementTime();

        frontendController.handlePostRequest(availableTime,"car",workshopName); // Book an available time.

        ResponseEntity<String> response = frontendController.handlePostRequest(availableTime,"car",workshopName); // Book that time again.

        // Assert
        assertFalse(response.getBody().isEmpty());
        assertEquals(response.getBody(),"No such available timeslot exists");
        assertEquals(response.getStatusCode(),HttpStatus.NOT_FOUND);
    }

    @Test
    void test_areSameMoment_DifferentFormatSameMoment() {
        // Arrange
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T14:30:00+02:00";

        // Act & Assert
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");
    }

    @Test
    void test_areSameMoment_SameFormatSameMoment() {
        // Arrange
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T12:30:00+00:00";

        // Act & Assert
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");
    }

    @Test
    void test_areSameMoment_SameFormatDifferentMoment() {
        // Arrange
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T13:30:00+00:00";

        // Act & Assert
        assertFalse(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered different moments");
    }

    @Test
    void test_handleGetRequest_AnyWorkshopAnyVehicle() {
        // Arrange
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "any";
        String workshopPick = "any";

        when(env.getProperty("servers.list")).thenReturn("london,manchester");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");

        // Setup for London server properties.
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.localTimezoneOffset.london")).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat.london")).thenReturn("XML");
        when(env.getProperty("servers.physicalAddress.london")).thenReturn("1A Gunton Rd, London");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Setup for Manchester server properties.
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
        List<TireReplacementTimeSlot> timeSlots = response.getBody();

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));
        boolean hasCarVehicle = timeSlots.stream().anyMatch(slot -> "car".equals(slot.getVehicleTypesServiced()));
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(hasLondon, "Expected a time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertTrue(hasCarVehicle, "Expected a time slot with only 'car' being serviced");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");
    }

    @Test
    void test_handleGetRequest_AnyWorkshopTruckVehicle() {
        // Arrange
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "truck";
        String workshopPick = "any";

        when(env.getProperty("servers.list")).thenReturn("london,manchester");

        // Setup for London server properties.
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Setup for Manchester server properties.
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
        List<TireReplacementTimeSlot> timeSlots = response.getBody();

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));
        boolean hasCarVehicle = timeSlots.stream().anyMatch(slot -> "car".equals(slot.getVehicleTypesServiced()));
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(hasLondon, "Expected no time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertFalse(hasCarVehicle, "Expected a time slot with only 'car' not being serviced");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");
    }

    @Test
    void test_handleGetRequest_manchesterWorkshopAnyVehicle() {
        // Arrange
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "any";
        String workshopPick = "manchester";

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
        List<TireReplacementTimeSlot> timeSlots = response.getBody();

        boolean hasLondon = timeSlots.stream().anyMatch(slot -> "london".equals(slot.getWorkshopName()));
        boolean hasManchester = timeSlots.stream().anyMatch(slot -> "manchester".equals(slot.getWorkshopName()));;
        boolean hasCarAndTruckVehicle = timeSlots.stream().anyMatch(slot -> "car,truck".equals(slot.getVehicleTypesServiced()));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(hasLondon, "Expected no time slot with workshopName 'london'");
        assertTrue(hasManchester, "Expected a time slot with workshopName 'manchester'");
        assertTrue(hasCarAndTruckVehicle, "Expected a time slot with 'car' and 'truck' being serviced");
    }

    @Test
    void test_handleGetRequest_londonWorkshopTruckVehicle() {
        //Arrange
        String beginTime = "2006-08-21";
        String endTime = "2030-09-21";
        String vehicleTypes = "truck";
        String workshopPick = "london";

        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Values that are needed for mocking, but are otherwise irrelevant.
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Act
        ResponseEntity<List<TireReplacementTimeSlot>> response = frontendController.handleGetRequest(beginTime, endTime, vehicleTypes, workshopPick);
        List<TireReplacementTimeSlot> timeSlots = response.getBody();

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertTrue(timeSlots.isEmpty());
    }

    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsBefore() {
        // Arrange
        String dateStr = "2023-08-20";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";

        // Act & Assert
        assertTrue(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));

    }

    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsEqual() {
        // Arrange
        String dateStr = "2023-08-21";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";

        // Act & Assert
        assertTrue(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));
    }

    @Test
    void test_isDateBeforeOrEqualDateTime_DateIsAfter() {
        // Arrange
        String dateStr = "2023-08-22";
        String dateTimeStr = "2023-08-21T12:30:00+00:00";

        // Act & Assert
        assertFalse(HTTPFrontendRequestController.isDateBeforeOrEqualDateTime(dateStr,dateTimeStr));
    }

    @Test
    void test_getListFromEnvironmentProperties_LongList(){
        // Arrange
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car,truck,motorcycle");

        // Act
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Arrays.asList("car","truck","motorcycle");

        // Assert
        assertEquals(list1,list2);
    }

    @Test
    void test_getListFromEnvironmentProperties_SingletonList(){
        // Arrange
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");

        // Act
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Collections.singletonList("car");

        // Assert
        assertEquals(list1,list2);
    }

    @Test
    void test_getListFromEnvironmentProperties_EmptyList(){
        // Arrange
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("");

        // Act
        List<String> list1 = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = new ArrayList<>();

        // Assert
        assertEquals(list1,list2);
    }

    @Test
    void test_haveCommonElements_OneCommonElement() {
        // Arrange
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("car");

        // Act & Assert
        assertTrue(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }

    @Test
    void test_haveCommonElements_NoCommonElementsLargerList() {
        // Arrange
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("motorcycle");

        // Act & Assert
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }

    @Test
    void test_haveCommonElements_NoCommonElementSingletonList() {
        // Arrange
        List<String> list1 = Collections.singletonList("truck");
        List<String> list2 = Collections.singletonList("motorcycle");

        // Act & Assert
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
}
