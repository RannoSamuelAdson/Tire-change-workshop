package com.example.demo.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void haveCommonElements() {
    }
}