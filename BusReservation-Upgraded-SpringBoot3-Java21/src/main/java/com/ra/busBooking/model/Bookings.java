package com.ra.busBooking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double price;
    private String busName;
    private LocalDate filterDate;
    private String fromDestination;
    private String toDestination;
    private int noOfPersons;
    private String time;
    private double totalCalculated;
    private boolean tripStatus;
    private int userId;
    private String fileName;

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

    public int getNoOfPersons() { return noOfPersons; }
    public void setNoOfPersons(int noOfPersons) { this.noOfPersons = noOfPersons; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getTotalCalculated() { return totalCalculated; }
    public void setTotalCalculated(double totalCalculated) { this.totalCalculated = totalCalculated; }

    public boolean isTripStatus() { return tripStatus; }
    public void setTripStatus(boolean tripStatus) { this.tripStatus = tripStatus; }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}