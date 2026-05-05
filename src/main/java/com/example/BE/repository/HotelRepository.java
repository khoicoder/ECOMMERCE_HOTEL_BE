package com.example.BE.repository;

import com.example.BE.model.HotelModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<HotelModel, Long> {
}