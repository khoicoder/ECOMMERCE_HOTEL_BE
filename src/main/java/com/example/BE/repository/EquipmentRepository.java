package com.example.BE.repository;

import com.example.BE.enums.EquipmentStatus;
import com.example.BE.model.EquipmentModel;
import com.example.BE.model.HotelModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<EquipmentModel,Long> {
    List<EquipmentModel> findByHotel(Long hotelId);
    List<EquipmentModel> findByRoomId(Long roomId);
    List<EquipmentModel> findByStatus(EquipmentStatus status);

}
