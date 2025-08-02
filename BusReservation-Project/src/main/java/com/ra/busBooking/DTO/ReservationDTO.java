package com.ra.busBooking.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReservationDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate filterDate;

    private String to;

    private String from;

    public LocalDate getFilterDate() {
        return filterDate;
    }

    public void setFilterDate(LocalDate filterDate) {
        this.filterDate = filterDate;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
