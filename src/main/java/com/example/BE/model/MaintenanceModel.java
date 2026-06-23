package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.MaintenanceType;
import com.sun.tools.javac.Main;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

//Maintenance = ghi nhận sự cố + theo dõi xử lý + cập nhật trạng thái phòng/thiết bị
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "maintenance_tickets")
public class MaintenanceModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="maintenance_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private MaintenanceType maintenanceType;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus maintenanceStatus;
    private LocalDateTime completedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private HotelModel hotel;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="equipment_id")
    private EquipmentModel equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomModel room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private UserModel assignedTo;
    //https://dbdiagram.io/d/6a3a325c5c789b8acbdfe82f
}
