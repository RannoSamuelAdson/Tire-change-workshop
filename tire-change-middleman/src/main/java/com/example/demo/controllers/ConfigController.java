package com.example.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConfigController {

    @Autowired
    private Environment env;

    /**
     * Retrieves the server configurations and serviceable car types from the environment properties.
     *
     * @return A map containing the list of servers and car types.
     * @throws IOException if an error occurs during property retrieval.
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() throws IOException {
        // Retrieve property values.
        String serversList = env.getProperty("servers.list");
        String allServiceableCarTypes = env.getProperty("servers.allServiceableCarTypes");

        // Convert the comma-separated strings into lists.
        List<String> servers = Arrays.asList(serversList.split(","));
        List<String> carTypes = Arrays.asList(allServiceableCarTypes.split(","));

        // Return the lists in a map.
        return Map.of(
                "servers", servers,
                "carTypes", carTypes
        );
    }

    /**
     * Retrieves the timezone offset for a specific workshop.
     *
     * @param workshop The name of the workshop.
     * @return A ResponseEntity containing the timezone offset or a 404 status if not found.
     */
    @GetMapping("/timezone-offset")
    public ResponseEntity<Map<String, Integer>> getTimezoneOffset(@RequestParam String workshop) {
        // Construct the property key to retrieve the offset.
        String propertyKey = "servers.localTimezoneOffset." + workshop;
        String offsetStr = env.getProperty(propertyKey);

        // Check if the property value exists and is not empty.
        if (offsetStr != null && !offsetStr.isEmpty()) {
            int offset = Integer.parseInt(offsetStr);
            Map<String, Integer> response = new HashMap<>();
            response.put("timezoneOffset", offset);
            return ResponseEntity.ok(response); // Return the offset in the response body.
        }

        // Return 404 status if the offset is not found.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
