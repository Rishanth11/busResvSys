package com.ra.busBooking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ra.busBooking.model.BusData;

public interface BusDataRepository extends JpaRepository<BusData, Integer> {

    @Query("SELECT b FROM BusData b WHERE b.toDestination = :to AND b.fromDestination = :from AND b.filterDate = :date ORDER BY b.filterDate DESC")
    List<BusData> findByToFromAndDate(
            @Param("to") String to,
            @Param("from") String from,
            @Param("date") LocalDate date);
}
