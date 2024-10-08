package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerCommunicationControllerTest {
    @Mock
    private Environment env;

    @InjectMocks
    private ServerCommunicationController controller;

    @Test
    void test_routeGetRequestSending_JSON() {
        // Arrange
        String urlXML = "http://localhost:9003/api/v1/tire-change-times/available?from=2024-08-21&until=2024-08-31";
        String urlJSON = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String workshopName = "manchester";
        String endTime = "2024-08-21";
        ServerCommunicationController controllerSpy = spy(controller);

        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("JSON");
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
        ServerCommunicationController controllerSpy = spy(controller);

        when(env.getProperty("servers.responseBodyFormat."+ workshopName)).thenReturn("XML");
        doReturn(Collections.singletonList(new TireReplacementTimeSlot())).when(controllerSpy).sendGetRequestXML(workshopName,urlXML);

        // Act
        controllerSpy.routeGetRequestSending(urlXML, urlJSON, workshopName, endTime);

        // Assert
        verify(controllerSpy, never()).sendGetRequestJSON(anyString(), anyString(),anyString()); // Verify that parseXML was not called
        verify(controllerSpy).sendGetRequestXML(workshopName, urlXML); // Verify that parseJSON was called
    }

    @Test
    void test_sendUpdateRequest_withPutMethod() {
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

        // Getting a viable element for updating.
        HTTPFrontendRequestController frontendController = new HTTPFrontendRequestController(env,controller);
        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String id = timeSlot.getId();

        // Act
        ResponseEntity<String> putResponse = controller.sendUpdateRequest(workshopName, id);

        // Assert
        assertTrue(putResponse.getHeaders().containsKey("X-Put-Method-Executed"));
        assertEquals("true", putResponse.getHeaders().get("X-Put-Method-Executed").get(0));
        assertFalse(putResponse.getHeaders().containsKey("X-Post-Method-Executed"));
    }

    @Test
    void test_sendUpdateRequest_withPostMethod() {
        // Arrange
        String workshopName = "manchester";
        when(env.getProperty("servers.contactInformation")).thenReturn("Tires Global");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");

        when(env.getProperty("servers.port." + workshopName)).thenReturn("http://localhost:9004/");
        when(env.getProperty("servers.host." + workshopName)).thenReturn("api/v2/tire-change-times/");
        when(env.getProperty("servers.address.book." + workshopName)).thenReturn("booking");
        when(env.getProperty("servers.bookingMethod." + workshopName)).thenReturn("POST");
        when(env.getProperty("servers.address.get." + workshopName)).thenReturn("");
        when(env.getProperty("servers.physicalAddress." + workshopName)).thenReturn("14 Bury New Rd, Manchester");
        when(env.getProperty("servers.allowedVehicles." + workshopName)).thenReturn("car,truck");
        when(env.getProperty("servers.localTimezoneOffset." + workshopName)).thenReturn("1");
        when(env.getProperty("servers.responseBodyFormat." + workshopName)).thenReturn("JSON");
        when(env.getProperty("servers.getQuery.responseElements.pageAmount." + workshopName)).thenReturn("1500");
        when(env.getProperty("servers.getQuery.responseElements.pageSkipAmount." + workshopName)).thenReturn("0");

        // Getting a viable element for updating.
        HTTPFrontendRequestController frontendController = new HTTPFrontendRequestController(env,controller);
        ResponseEntity<List<TireReplacementTimeSlot>> getResponse = frontendController.handleGetRequest("2006-08-21","2030-09-21","any",workshopName);
        TireReplacementTimeSlot timeSlot = getResponse.getBody().getFirst();
        String id = timeSlot.getId();

        // Act
        ResponseEntity<String> putResponse = controller.sendUpdateRequest(workshopName, id);

        // Assert
        assertTrue(putResponse.getHeaders().containsKey("X-Post-Method-Executed"));
        assertEquals("true", putResponse.getHeaders().get("X-Post-Method-Executed").get(0));
        assertFalse(putResponse.getHeaders().containsKey("X-Put-Method-Executed"));
    }

    @Test
    void test_sendGetRequestXML_successfulRetrieval() {
        // Arrange
        String workshopName = "london";

        // Let the time values give a wide range of time for events to happen to ensure finding of elements.
        String url = "http://localhost:9003/api/v1/tire-change-times/available?from=2006-01-02&until=2030-01-02";

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

        // Let the time values give such a narrow margin of times, that no elements can qualify.
        String url = "http://localhost:9003/api/v1/tire-change-times/available?from=2006-01-02&until=2006-01-02";

        // Act
        List<TireReplacementTimeSlot> result = controller.sendGetRequestXML(workshopName, url);

        // Assert
        assertTrue(result.isEmpty());

    }

    @Test
    void test_sendGetRequestJSON_successfulRetrieval() {
        // Arrange
        String workshopName = "manchester";

        // Let the time values give a wide range of time for events to happen to ensure finding of elements.
        String url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String endTime = "2030-08-21";

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

        // Let the time values give such a narrow margin of times, that no elements can qualify.
        String url = "http://localhost:9004/api/v2/tire-change-times?amount=1500&page=0&from=2006-01-02";
        String endTime = "2006-01-02";

        // Act
        List<TireReplacementTimeSlot> result = controller.sendGetRequestJSON(workshopName, url, endTime);

        // Assert
        assertTrue(result.isEmpty());

    }

    @Test
    void test_tryGetTextContent_ElementExists() throws Exception {
        // Arrange a sample XML document for testing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("root");
        document.appendChild(root);

        // Adding a child element with some text content.
        Element child = document.createElement("uuid");
        child.setTextContent("3f78fe1c-af46-4afc-873a-f8dad35be6a8");
        root.appendChild(child);

        // Act & Assert
        assertEquals("3f78fe1c-af46-4afc-873a-f8dad35be6a8", HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }

    @Test
    void test_tryGetTextContent_ElementDoesNotExist() throws Exception {
        // Arrange a sample XML document for testing.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("root");
        document.appendChild(root);

        // Act & Assert
        assertNull(HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }

    @Test
    void test_tryGetTextContent_ElementExistsButNoTextContent() throws Exception {
        // Arrange a sample XML document for testing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("root");
        document.appendChild(root);

        // Adding a child element with no text content.
        Element emptyChild = document.createElement("uuid");
        root.appendChild(emptyChild);

        // Act & Assert
        assertEquals("", HTTPFrontendRequestController.tryGetTextContent(root, "uuid"));
    }
}