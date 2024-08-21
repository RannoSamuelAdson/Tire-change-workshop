package com.example.demo.models;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TireReplacementTimeSlot {

    private String workshopName;
    private String workshopAddress;
    private String id;
    private String tireReplacementTime;
    private String vehicleTypesServiced;

    // Constructor without Environment parameter
    public TireReplacementTimeSlot(String workshopName, String workshopAddress, String id, String timeString, int localTimeOffsetInt, String vehicleTypesServiced) {
        this.workshopName = workshopName;
        this.workshopAddress = workshopAddress;
        this.id = id;

        // Parse the timeString and adjust to the local time offset
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(timeString)
                .withOffsetSameInstant(ZoneOffset.ofHours(localTimeOffsetInt));
        // Storing tireReplacementTime as an ISO 8601 string
        this.tireReplacementTime = offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.vehicleTypesServiced = vehicleTypesServiced;
    }

    // Getters and setters

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getWorkshopAddress() {
        return workshopAddress;
    }

    public void setWorkshopAddress(String workshopAddress) {
        this.workshopAddress = workshopAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTireReplacementTime() {
        return tireReplacementTime;
    }

    public void setTireReplacementTime(String tireReplacementTime) {
        this.tireReplacementTime = tireReplacementTime;
    }

    public String getVehicleTypesServiced() {
        return vehicleTypesServiced;
    }

    public void setVehicleTypesServiced(String vehicleTypesServiced) {
        this.vehicleTypesServiced = vehicleTypesServiced;
    }
}
