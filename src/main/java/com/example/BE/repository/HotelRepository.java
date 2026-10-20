package com.example.BE.repository;

import com.example.BE.model.HotelModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<HotelModel, Long> {
    long count();
    HotelModel getHotelById(Long hotelId);

    boolean existsByNameIgnoreCase(String name);
}