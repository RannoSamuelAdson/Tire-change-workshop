package com.example.demo.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    @Mock
    private Environment env;

    @InjectMocks
    private ConfigController controller;

    @Test
    void test_getConfig() throws IOException {
        // Arrange
        when(env.getProperty("servers.list")).thenReturn("london,manchester");
        when(env.getProperty("servers.allServiceableCarTypes")).thenReturn("car,truck");

        // Act
        Map<String, Object> responseMap = controller.getConfig();

        // Assert: Verify that the map contains the expected values
        assertNotNull(responseMap);  // Ensure the map is not null
        assertEquals(2, responseMap.size()); // Ensure the map contains exactly two entries

        assertTrue(responseMap.containsKey("servers"));
        assertTrue(responseMap.containsKey("carTypes"));

        assertEquals(Arrays.asList("london", "manchester"), responseMap.get("servers"));
        assertEquals(Arrays.asList("car", "truck"), responseMap.get("carTypes"));
    }

    @Test
    void test_getTimezoneOffset_offsetFound() {
        // Arrange
        String workshop = "london";
        when(env.getProperty("servers.localTimezoneOffset." + workshop)).thenReturn("1");

        // Act
        ResponseEntity<Map<String, Integer>> response = controller.getTimezoneOffset(workshop);

        // Assert
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("timezoneOffset"));
        assertEquals(response.getBody().get("timezoneOffset"), Integer.valueOf(1));
    }

    @Test
    void test_getTimezoneOffset_offsetNotFound() {
        // Arrange
        String workshop = "london";
        when(env.getProperty("servers.localTimezoneOffset." + workshop)).thenReturn("");

        // Act
        ResponseEntity<Map<String, Integer>> response = controller.getTimezoneOffset(workshop);

        // Assert
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }
}
