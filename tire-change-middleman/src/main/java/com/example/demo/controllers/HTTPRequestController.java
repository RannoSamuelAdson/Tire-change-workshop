package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HTTPRequestController {

    @PostMapping("/book")
    public void handlePostRequest(@RequestParam String beginTime,
                                  @RequestParam String vehicleType,
                                  @RequestParam String workshopName) {
        System.out.println(beginTime);
        System.out.println(vehicleType);
        System.out.println(workshopName);
        // Implement booking logic here
    }

    @GetMapping("/filter")
    public List<TireReplacementTimeSlot> handleGetRequest(@RequestParam String beginTime,
                                                          @RequestParam String endTime,
                                                          @RequestParam String vehicleType,
                                                          @RequestParam String workshopName) {
        List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
        // Example data
        timeSlots.add(new TireReplacementTimeSlot("Auto Repair Pro", "123 Main St, Tallinn", true,"2", "2024-08-17T20:00"));
        timeSlots.add(new TireReplacementTimeSlot("Quick Tires", "456 Oak Rd, Tartu", true,"3", "2024-08-17T21:00"));
        timeSlots.add(new TireReplacementTimeSlot("Speedy Service", "789 Elm St, Pärnu", true,"4", "2024-08-17T22:00"));
        return timeSlots;
    }
}
