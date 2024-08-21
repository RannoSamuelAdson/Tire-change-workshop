package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${servers.list}")
    private String serversList;

    @Value("${servers.allServiceableCarTypes}")
    private String allServiceableCarTypes;

    @GetMapping
    public Map<String, List<String>> getConfig() {
        List<String> servers = Arrays.asList(serversList.split(","));
        List<String> carTypes = Arrays.asList(allServiceableCarTypes.split(","));

        return Map.of(
                "servers", servers,
                "carTypes", carTypes
        );
    }
}