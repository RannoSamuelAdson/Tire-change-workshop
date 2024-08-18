package com.example.demo.controllers;

import com.example.demo.models.TireReplacementTimeSlot;
import com.example.demo.services.TireChangeTimesGetResponseXMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class HTTPFrontendRequestController {
    @Autowired
    private Environment env;

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
        if (!Objects.equals(workshopName, "any")){
            // Fetching property values within the method
            String ServerPort = env.getProperty("servers.port." + workshopName);
            String ServerHost = env.getProperty("servers.host." + workshopName);
            String ServerGetAddress = env.getProperty("servers.address.get." + workshopName);


            // Construct the full URL
            String url = ServerPort + ServerHost + ServerGetAddress + "?from=" + beginTime + "&until=" + endTime;

            // Make the HTTP GET request
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            if (Objects.equals(env.getProperty("servers.responseBodyFormat." + workshopName), "XML")) {// If the format is XML
                timeSlots = parseXML(response,workshopName);

        }


    }
        return timeSlots;
    }
    private List<TireReplacementTimeSlot> parseXML(String XMLData, String workshopName){

            List<TireReplacementTimeSlot> timeSlots = new ArrayList<>();
            // Parse the XML response
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(TireChangeTimesGetResponseXMLService.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                TireChangeTimesGetResponseXMLService tireChangeTimesResponse =
                        (TireChangeTimesGetResponseXMLService) unmarshaller.unmarshal(new StringReader(XMLData));

                // Convert the parsed data to TireReplacementTimeSlot objects
                for (TireChangeTimesGetResponseXMLService.AvailableTime availableTime : tireChangeTimesResponse.getAvailableTimes()) {
                    timeSlots.add(new TireReplacementTimeSlot(
                            workshopName,
                            env.getProperty("servers.physicalAddress." + workshopName), // Get physical address from properties
                            availableTime.getUuid(),
                            availableTime.getTime(),
                            env.getProperty("servers.allowedVehicles." + workshopName)
                    ));
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return timeSlots;

}

}
