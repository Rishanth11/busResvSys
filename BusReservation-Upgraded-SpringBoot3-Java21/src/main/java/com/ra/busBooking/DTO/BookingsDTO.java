
package com.ra.busBooking.DTO;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class BookingsDTO {

    private String busName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate filterDate;
    
    private String fromDestination;
    private String toDestination;
    private int noOfPersons;
    private String time;
    private double totalCalculated;
    private double price; // ✅ Needed for calculation
    private Long id;
    private String tripStatus;

    // Getters and Setters

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

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }
}
