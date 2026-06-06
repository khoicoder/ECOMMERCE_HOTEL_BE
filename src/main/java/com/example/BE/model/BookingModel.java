package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Entity
@Data
@Table(name ="bookings")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name  = "Check_in_date", nullable = false)
    private LocalDate checkInDate;
    @Column(name ="check_out_date", nullable = false)
    private LocalDate checkOutDate;
    @Column(name ="total_price",nullable = false)
    private double totalPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private BookingStatus status =  BookingStatus.PENDING;
    // Mối quan hệ: Nhiều lượt đặt phòng thuộc về một phòng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomModel room;
    // Mối quan hệ: Nhiều lượt đặt phòng thuộc về một khách hàng
    // Bạn cần thay đổi "UserModel" thành tên Class User thực tế trong dự án của bạn)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
}
