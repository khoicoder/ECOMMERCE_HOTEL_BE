package com.example.BE.repository;

import com.example.BE.model.RoomModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomModel, Long> {
    List<RoomModel> findByHotel_Id(Long hotelId);
    long countByHotel_Id(Long hotelId);

    @Query(value = "SELECT * FROM rooms WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<RoomModel> findByIdWithLock(@Param("id") Long id);
}
