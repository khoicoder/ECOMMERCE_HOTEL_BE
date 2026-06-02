package com.example.BE.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter

public enum RoomStatus {
    AVAILABLE("Phòng trống"),
    OCCUPIED("Đang có khách"),
    MAINTENANCE("Đang bảo trì"),
    DIRTY("Chưa dọn dẹp"),
    BOOKED("Đã được đặt trước");
    private final String displayValue;
    private RoomStatus(String displayValue) {
        this.displayValue = displayValue;
    }

}
