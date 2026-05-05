package com.example.BE.repository;

import com.example.BE.model.RoomModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomModel, Long> {
}