package com.example.BE.repository;

import com.example.BE.enums.EquipmentStatus;
import com.example.BE.model.MaintenanceModel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<MaintenanceModel, Long> {
    List<MaintenanceModel> findByHotelId(Long hotelId);

    List<MaintenanceModel> findByRoomId(Long roomId);
    List<MaintenanceModel> findByHotelIdAndStatus(Long hotelId, EquipmentStatus status);


}

