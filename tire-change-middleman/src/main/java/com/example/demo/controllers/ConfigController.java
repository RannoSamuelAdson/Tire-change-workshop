package com.example.demo.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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

    @Autowired
    private ObjectMapper objectMapper; // Injecting ObjectMapper

    @GetMapping("/config")
    public Map<String, List<String>> getConfig() throws IOException {
        String serversList = env.getProperty("servers.list");
        String allServiceableVehicleTypes = env.getProperty("servers.allServiceableCarTypes");


        List<String> servers = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,serversList);
        List<String> carTypes = HTTPFrontendRequestController.getListFromEnvironmentProperties(env,allServiceableVehicleTypes);

        HashMap<String,List<String>> responseMap = new HashMap<>();
        responseMap.put("servers", servers);
        responseMap.put("carTypes", carTypes);
        return responseMap;
        /*return Map.of(
                "servers", servers,
                "carTypes", carTypes
        );*/
    }
    @GetMapping("/timezone-offset")
    public ResponseEntity<Map<String, Integer>> getTimezoneOffset(@RequestParam String workshop) {
        String propertyKey = "servers.localTimezoneOffset." + workshop;
        String offsetStr = env.getProperty(propertyKey);

        if (offsetStr != null) {
            int offset = Integer.parseInt(offsetStr);
            Map<String, Integer> response = new HashMap<>();
            response.put("timezoneOffset", offset);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
