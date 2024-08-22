package com.example.demo.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HTTPFrontendRequestControllerTest {

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
    void areSameMomentDifferentFormatSameMoment() {

        // Same Moment, Different Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T14:30:00+02:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");
}

    @Test
    void areSameMomentSameFormatSameMoment() {
        // Same Moment, Same Format
        String dateTimeStr1 = "2023-08-21T12:30:00+00:00";
        String dateTimeStr2 = "2023-08-21T12:30:00+00:00";
        assertTrue(HTTPFrontendRequestController.areSameMoment(dateTimeStr1, dateTimeStr2), "Expected the two times to be considered the same moment");}
    @Test
    void areSameMomentSameFormatDifferentMoment() {
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


    //haveCommonElements(List<String> list1, List<String> list2)
	/*
	1. (list1 = {truck, car}, list2 = {car}): return true
	2. (list1 =  {motorcycle}, list2 = {truck, car}): return false
	3. list1 = {truck}, list2 = {car}: return false
	*/

    @Test
    void haveCommonElementsOneCommonElement() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("car");
        assertTrue(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
    @Test
    void haveCommonElementsNoCommonElementsLargerList() {
        List<String> list1 = Arrays.asList("truck", "car");
        List<String> list2 = Collections.singletonList("motorcycle");
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
    @Test
    void haveCommonElementsNoCommonElementsSingeltonList() {
        List<String> list1 = Collections.singletonList("truck");
        List<String> list2 = Collections.singletonList("car");
        assertFalse(HTTPFrontendRequestController.haveCommonElements(list1, list2));
    }
}