package com.example.demo.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "london.tireChangeBookingRequest")
public class TireChangeBookingRequest {

    private String contactInformation;

    public TireChangeBookingRequest(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    @XmlElement(name = "contactInformation")
    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }
}