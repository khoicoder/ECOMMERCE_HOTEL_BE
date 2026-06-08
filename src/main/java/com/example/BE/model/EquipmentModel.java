package com.example.BE.model;

import com.example.BE.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="equipment_id")
    private Long equipmentId;

    private String name; //tên thiết bị tv,tủ lạnh..
    private String brand; // mã nhãn hàng
    private String serialNumber; //seri nếu có


    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="hotel_id")
    private HotelModel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomModel room;

}
