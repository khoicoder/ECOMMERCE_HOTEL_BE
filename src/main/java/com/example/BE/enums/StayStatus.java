package com.example.BE.enums;

import lombok.Getter;

@Getter
public enum StayStatus {
    NOT_CHECKED_IN("Chưa nhận phòng"),
    CHECKED_IN("Đang lưu trú"),
    CHECKED_OUT("Đã trả phòng");

    private final String displayValue;

    StayStatus(String displayValue) {
        this.displayValue = displayValue;
    }
}
