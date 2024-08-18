package com.example.demo.services;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "tireChangeTimesResponse")
public class TireChangeTimesGetResponseXMLService {
    private List<AvailableTime> availableTimes;

    @XmlElement(name = "availableTime")
    public List<AvailableTime> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<AvailableTime> availableTimes) {
        this.availableTimes = availableTimes;
    }

    public static class AvailableTime {
        private String uuid;
        private String time;

        @XmlElement
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        @XmlElement
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
