package com.example.demo.models;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TireReplacementTimeSlot {
    private String workshopName;
    private String workshopAddress;

    private String id;
    private OffsetDateTime tireReplacementTime;
    private String vehicleTypesServiced;

    public TireReplacementTimeSlot(String workshopName, String workshopAddress, String id,String timeString,String vehicleTypesServiced) {
        this.workshopName = workshopName;
        this.workshopAddress = workshopAddress;
        this.id = id;
        this.tireReplacementTime = OffsetDateTime.parse(timeString);
        this.vehicleTypesServiced = vehicleTypesServiced;
    }


    public OffsetDateTime getTireReplacementTime() {
        return tireReplacementTime;
    }

    public String getWorkshopName() {return workshopName;}

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getWorkshopAddress() {
        return workshopAddress;
    }
    public String getVehicleTypesServiced() {
        return vehicleTypesServiced;
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
    public void setTireReplacementTime(OffsetDateTime tireReplacementTime) {
        this.tireReplacementTime = tireReplacementTime;
    }

    public void setVehicleTypesServiced(String vehicleTypesServiced) {
        this.vehicleTypesServiced = vehicleTypesServiced;
    }

}
