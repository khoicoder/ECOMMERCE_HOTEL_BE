package com.example.BE.repository;

import com.example.BE.enums.EquipmentStatus;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.model.MaintenanceModel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<MaintenanceModel, Long> {
    List<MaintenanceModel> findByHotel_Id(Long hotelId);
    List<MaintenanceModel> findByMaintenanceStatus(MaintenanceStatus maintenanceStatus);
    List<MaintenanceModel> findByEquipment_EquipmentId(Long equipmentId);
    List<MaintenanceModel> findByRoom_Id(Long roomId);
    List<MaintenanceModel> findByAssignedTo_Id(Long userId);



}

