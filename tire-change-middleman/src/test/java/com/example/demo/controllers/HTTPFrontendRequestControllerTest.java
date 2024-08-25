package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class HTTPFrontendRequestControllerTest {
    @Mock
    private Environment env;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HTTPFrontendRequestController controller;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
    private void setupServerProperties() {
        // This is a helper method, that configures some of the behaviour of Environment env.
        // It also configures "getListFromEnvironmentProperties" method, for it directly interacts with env.


        // Setup for london variables.
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
        when(env.getProperty("servers.host.london")).thenReturn("api/v1/tire-change-times/");
        when(env.getProperty("servers.address.get.london")).thenReturn("available");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.london")).thenReturn("1000");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.london")).thenReturn("0");

        // Setup for manchester variables.
        when(env.getProperty("servers.port.manchester")).thenReturn("http://localhost:9004/");
        when(env.getProperty("servers.host.manchester")).thenReturn("api/v2/tire-change-times/");
        when(env.getProperty("servers.address.get.manchester")).thenReturn("");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount.manchester")).thenReturn("1000");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount.manchester")).thenReturn("0");
    }

    private boolean listsContainSameElements(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return false;
        }
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    @Test
    void handlePostRequest() {
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
    //routeGetRequestSending(String urlXML, String urlJSON, String workshopName, String endTime)
	/*
	1. (urlXML = (random url), urlJSON = (random url), workshopName = (random name), endTime = (random date)): return {new TireReplacementTimeSlot}
	The aim of this test is to see, that the method "sendGetRequestJSON" is called, when application.properties file calls for it. Thus, because of the
	dynamic coding int this method, the inputs and outputs aren't really relevant.

	2. (urlXML = (random url), urlJSON = (random url), workshopName = (random name), endTime = (random date)): return {new TireReplacementTimeSlot}
	The aim of this test is to see, that the method "sendGetRequestXML" is called, when application.properties file calls for it. Thus, because of the
	dynamic coding int this method, the inputs and outputs aren't really relevant.
	*/
    @Test
    void test_routeGetRequestSending_JSON() {
        // Arrange
        String urlXML = "http://localhost:9003/api/v1/tire-change-times/available?from=2024-08-21&until=2024-08-31";
        String urlJSON = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String workshopName = "manchester";
        String endTime = "2024-08-21";
        HTTPFrontendRequestController controllerSpy = spy(controller);

        // Mock the environment to return JSON for the workshop

        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("JSON");

        // Mock sendGetRequestJSON to return a list of TireReplacementTimeSlot
        doReturn(Collections.singletonList(new TireReplacementTimeSlot())).when(controllerSpy).sendGetRequestJSON(workshopName,urlJSON,endTime);

        // Act
        controllerSpy.routeGetRequestSending(urlXML, urlJSON, workshopName, endTime);

        // Assert
        verify(controllerSpy, never()).sendGetRequestXML(anyString(), anyString()); // Verify that parseXML was not called
        verify(controllerSpy).sendGetRequestJSON(workshopName, urlJSON, endTime); // Verify that parseJSON was called
    }
    @Test
    void test_routeGetRequestSending_XML() {
        // Arrange
        String urlXML = "http://localhost:9003/api/v1/tire-change-times/available?from=2024-08-21&until=2024-08-31";
        String urlJSON = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String workshopName = "london";
        String endTime = "2024-08-21";
        HTTPFrontendRequestController controllerSpy = spy(controller);

        // Mock the environment to return JSON for the workshop

        when(env.getProperty("servers.responseBodyFormat."+ workshopName)).thenReturn("XML");

        // Mock parseJSON to return a list of TireReplacementTimeSlot
        doReturn(Collections.singletonList(new TireReplacementTimeSlot())).when(controllerSpy).sendGetRequestXML(workshopName,urlXML);

        // Act
        controllerSpy.routeGetRequestSending(urlXML, urlJSON, workshopName, endTime);

        // Assert
        verify(controllerSpy, never()).sendGetRequestJSON(anyString(), anyString(),anyString()); // Verify that parseXML was not called
        verify(controllerSpy).sendGetRequestXML(workshopName, urlXML); // Verify that parseJSON was called
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
        String beginTime = "2023-08-21T12:30:00+00:00";
        String endTime = "2023-09-21T14:30:00+00:00";
        String vehicleTypes = "any";
        String workshopPick = "any";
        String urlXML = "http://localhost:9003/api/v1/tire-change-times/available?from=2024-08-21&until=2024-08-31";
        String urlJSON = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";

        HTTPFrontendRequestController controllerSpy = spy(controller);

        setupServerProperties(); // Configures the env to return properties for url creation.






        try (MockedStatic<HTTPFrontendRequestController> mockedStatic = mockStatic(HTTPFrontendRequestController.class)) {
            doReturn(Arrays.asList(
                    new TireReplacementTimeSlot("london","address is irrelevant","1","2023-08-22T15:00:00Z",1,"car,truck"),
                    new TireReplacementTimeSlot("london","address is irrelevant","1","2023-08-22T16:00:00Z",1,"car,truck")
            )).when(controllerSpy).routeGetRequestSending(urlXML, urlJSON, "london", "2023-08-21");

            doReturn(List.of(
                    new TireReplacementTimeSlot("manchester", "address is irrelevant", "b58dbb55-8ea4-484b-917e-abade9c3da1a", "2023-08-22T15:00:00Z", 1, "truck")
            )).when(controllerSpy).routeGetRequestSending(urlXML, urlJSON, "manchester", "2023-08-21");
                // Mock the static method getListFromEnvironmentProperties for different properties
                mockedStatic.when(() -> HTTPFrontendRequestController.getListFromEnvironmentProperties(env, "servers.allServiceableCarTypes"))
                        .thenReturn(Arrays.asList("car", "truck"));

                mockedStatic.when(() -> HTTPFrontendRequestController.getListFromEnvironmentProperties(env, "servers.allowedVehicles.london"))
                        .thenReturn(Arrays.asList("car", "truck"));

                mockedStatic.when(() -> HTTPFrontendRequestController.getListFromEnvironmentProperties(env, "servers.allowedVehicles.manchester"))
                        .thenReturn(List.of("truck"));

            // If the lists in the parameters contain identical elements to these, then this engages.
            when(HTTPFrontendRequestController.haveCommonElements(
                    argThat(list -> listsContainSameElements(list, Arrays.asList("car","truck"))),
                    argThat(list -> listsContainSameElements(list, Arrays.asList("car")))
            )).thenReturn(true);

            when(HTTPFrontendRequestController.haveCommonElements(
                    argThat(list -> listsContainSameElements(list, Arrays.asList("car","truck"))),
                    argThat(list -> listsContainSameElements(list, Arrays.asList("car","truck")))
            )).thenReturn(true);

        ResponseEntity<List<TireReplacementTimeSlot>> response = controllerSpy.handleGetRequest(beginTime,endTime,vehicleTypes,workshopPick);
        }

    }
    /*@Test
    void test_handleGetRequest() {
    }
    @Test
    void test_handleGetRequest() {
    }
    @Test
    void test_handleGetRequest() {
    }
    @Test
    void test_handleGetRequest() {
    }*/
    @Test
    void test_sendGetRequestXML_successfulRetrieval() {
        // Arrange
        String workshopName = "london";

        // Let the time values give a wide range of time for events to happen to ensure finding of elements.
        String url = "http://localhost:9003/api/v1/tire-change-times/available?from=2006-01-02&until=2030-01-02";


        // Mock Environment properties
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("123 London Road");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("0");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("cars,trucks");

        // Act

        List<TireReplacementTimeSlot> result = controller.sendGetRequestXML(workshopName, url);

        // Assert
        assertTrue(!result.isEmpty());

    }
    @Test
    void test_sendGetRequestXML_emptyResponse() {
        // Arrange
        String workshopName = "london";

        // Let the time values give a wide range of time for events to happen to ensure finding of elements.
        String url = "http://localhost:9003/api/v1/tire-change-times/available?from=2006-01-02&until=2006-01-01";

        // Act

        List<TireReplacementTimeSlot> result = controller.sendGetRequestXML(workshopName, url);

        // Assert
        assertTrue(result.isEmpty());

    }


    /*sendGetRequestJSON(String workshopName, String url, String endTime)
        1. (workshopName = "manchester", url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02", endTime = "2030-08-21"):
            Expectation: A list of TireReplacementTimeSlot objects is returned, and it is not empty (indicating successful retrieval of time slots).

        2. (workshopName = "manchester", url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02", endTime = "2006-01-02"):
           Expectation: An empty list is returned (indicating no time slots fall within the given narrow time margin).
    */

    @Test
    void test_sendGetRequestJSON_successfulRetrieval() {
        // Arrange
        String workshopName = "manchester";

        // Let the time values give a wide range of time for events to happen to ensure finding of elements.
        String url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String endTime = "2030-08-21";


        // Mock Environment properties
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("123 Manchester Road");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("0");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("cars");

        // Act

        List<TireReplacementTimeSlot> result = controller.sendGetRequestJSON(workshopName, url, endTime);

        // Assert
        assertTrue(!result.isEmpty());

    }
    @Test
    void test_sendGetRequestJSON_emptyResponse() {
        // Arrange
        String workshopName = "manchester";

        // Let the time values give such a narrow margin of times, that no elements can qualify
        String url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String endTime = "2006-01-02";

        // Act

        List<TireReplacementTimeSlot> result = controller.sendGetRequestJSON(workshopName, url, endTime);

        // Assert
        assertTrue(result.isEmpty());

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
    @Test
    void test_haveCommonElements_NoCommonElementsSingletonList() {
        List<String> list1 = Collections.singletonList("truck");
        List<String> list2 = Collections.singletonList("car");
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }


    //tryGetTextContent(Element element, String tagName)
	/*
	1. (list1 = {truck, car}, list2 = {car}): return true
	2. (list1 =  {motorcycle}, list2 = {truck, car}): return false
	3. list1 = {truck}, list2 = {car}: return false
	*/

    @Test
    void test_tryGetTextContent_ElementExists() throws Exception {
        // Setup a sample XML document for testing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Create a root element
        Element root = document.createElement("root");
        document.appendChild(root);

        // Add a child element with some text content
        Element child = document.createElement("uuid");
        child.setTextContent("3f78fe1c-af46-4afc-873a-f8dad35be6a8");
        root.appendChild(child);

        // Assertion
        assertEquals("3f78fe1c-af46-4afc-873a-f8dad35be6a8", HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }
    @Test
    void test_tryGetTextContent_ElementDoesNotExist() throws Exception {
        // Setup a sample XML document for testing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Create a root element
        Element root = document.createElement("root");
        document.appendChild(root);

        // Assertion
        assertNull(HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }


    @Test
    void test_tryGetTextContent_ElementExistsButNoTextContent() throws Exception {
        // Setup a sample XML document for testing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Create a root element
        Element root = document.createElement("root");
        document.appendChild(root);

        // Add a child element with no text content
        Element emptyChild = document.createElement("uuid");
        root.appendChild(emptyChild);

        // Assertion
        assertEquals("", HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }


}
