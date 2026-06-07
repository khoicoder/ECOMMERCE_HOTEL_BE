package com.example.BE.enums;

public enum MaintenanceStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
}

//PENDING → IN_PROGRESS
//Ticket đã được nhận xử lý.
//
//PENDING → CANCELLED
//Ticket bị hủy ngay từ đầu.
//
//IN_PROGRESS → COMPLETED
//Đã xử lý xong.
//
//IN_PROGRESS → CANCELLED
//Dừng xử lý vì lý do nào đó.
//
//COMPLETED / CANCELLED → không cho đổi nữa`