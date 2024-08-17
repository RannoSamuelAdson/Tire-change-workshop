package com.example.demo.models;

import java.time.OffsetDateTime;

public class TireReplacementTimeSlot {
    private String workshopName;
    private String workshopAddress;
    private boolean availability;
    private String id;
    private OffsetDateTime tireReplacementTime;

    public TireReplacementTimeSlot(String workshopName, String workshopAddress, boolean availability, String id,String timeString) {
        this.workshopName = workshopName;
        this.workshopAddress = workshopAddress;
        this.availability = availability;
        this.id = id;
        this.tireReplacementTime = OffsetDateTime.parse(timeString + "+00:00");
    }

    public OffsetDateTime getTireReplacementTime() {
        return tireReplacementTime;
    }


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

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTireReplacementTime(OffsetDateTime tireReplacementTime) {
        this.tireReplacementTime = tireReplacementTime;
    }

}
