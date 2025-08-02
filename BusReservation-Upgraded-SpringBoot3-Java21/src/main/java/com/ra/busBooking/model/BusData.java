package com.ra.busBooking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class BusData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String busName;
    private LocalDate filterDate;
    private String fromDestination;
    private String toDestination;
    private double price;
    private String time;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public LocalDate getFilterDate() { return filterDate; }
    public void setFilterDate(LocalDate filterDate) { this.filterDate = filterDate; }

    public String getFromDestination() { return fromDestination; }
    public void setFromDestination(String fromDestination) { this.fromDestination = fromDestination; }

    public String getToDestination() { return toDestination; }
    public void setToDestination(String toDestination) { this.toDestination = toDestination; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
