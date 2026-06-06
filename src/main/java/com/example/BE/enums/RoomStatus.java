package com.example.BE.enums;

import lombok.Getter;

@Getter

public enum RoomStatus {
    AVAILABLE("Phòng trống"),
    OCCUPIED("Đang có khách"),
    MAINTENANCE("Đang bảo trì"),
    CLEANING("Chưa dọn dẹp"),
    BOOKED("Đã được đặt trước"),
    OUT_OF_SERVICE("Đã dừng phục vụ");

    private final String displayValue;
    private RoomStatus(String displayValue) {
        this.displayValue = displayValue;
    }

}
