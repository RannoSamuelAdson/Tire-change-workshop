package com.example.demo.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ConfigControllerTest {
    @Mock
    private Environment env;
    @InjectMocks
    private ConfigController controller;

    /*
    * getConfig()
    * 1. (Environment returns server list of "")
    * */
    @Test
    void test_getConfig() {
        try (MockedStatic<HTTPFrontendRequestController> mockedStatic = mockStatic(HTTPFrontendRequestController.class)) {
            mockedStatic.when(() -> HTTPFrontendRequestController.getListFromEnvironmentProperties(env, "servers.list"))
                    .thenReturn(Arrays.asList("london", "manchester"));

            mockedStatic.when(() -> HTTPFrontendRequestController.getListFromEnvironmentProperties(env, "servers.allServiceableCarTypes"))
                    .thenReturn(Arrays.asList("car", "truck"));

            Map<String, List<String>> responseMap = controller.getConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void getTimezoneOffset() {
        when(env.getProperty("servers.port.london")).thenReturn("http://localhost:9003/");
    }
}