package com.example.demo.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class HTTPFrontendRequestControllerTest {
    @Mock
    private Environment env;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
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
    void areSameMoment_DifferentFormatSameMoment() {

        // Same Moment, Different Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T14:30:00+02:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");
}

    @Test
    void areSameMoment_SameFormatSameMoment() {
        // Same Moment, Same Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T12:30:00+00:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");}
    @Test
    void areSameMoment_SameFormatDifferentMoment() {
        // Different Moment, Different Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T13:30:00+00:00";
        assertFalse(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered different moments");
    }

    @Test
    void handleGetRequest() {
    }

    @Test
    void isDateBeforeOrEqualDateTime() {
    }



    //getListFromEnviromentProperties(Environment env, String ConfProperties)
	/*
	1. (env contains the value "car,truck,motorcycle", list2 = "servers.allowedVehicles.london"): return {car,truck,motorcycle}
	2. (env contains the value "car", list2 = "servers.allowedVehicles.london): return {car}
	3. (env contains the value "", list2 = "servers.allowedVehicles.london): return {} (empty list)
	*/

    @Test
    void getListFromEnviromentProperties_LongList(){

        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car,truck,motorcycle");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnviromentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Arrays.asList("car","truck","motorcycle");

        assertEquals(list1,list2);
    }
    @Test
    void getListFromEnviromentProperties_SingletonList(){
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("car");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnviromentProperties(env,"servers.allowedVehicles.london");
        List<String> list2 = Collections.singletonList("car");

        assertEquals(list1,list2);
    }
    @Test
    void getListFromEnviromentProperties_EmptyList(){
        when(env.getProperty("servers.allowedVehicles.london")).thenReturn("");
        List<String> list1 = HTTPFrontendRequestController.getListFromEnviromentProperties(env,"servers.allowedVehicles.london");
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
    void haveCommonElements_OneCommonElement() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("car");
        assertTrue(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
    @Test
    void haveCommonElements_NoCommonElementsLargerList() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("motorcycle");
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
    @Test
    void haveCommonElements_NoCommonElementsSingletonList() {
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
    void tryGetTextContent_ElementExists() throws Exception {
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
    void tryGetTextContent_ElementDoesNotExist() throws Exception {
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
    void tryGetTextContent_ElementExistsButNoTextContent() throws Exception {
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
