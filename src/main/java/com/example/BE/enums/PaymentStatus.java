package com.example.BE.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),
    PENDING("Chờ thanh toán"),
    PARTIALLY_PAID("Thanh toán một phần"),
    PAID("Đã thanh toán đủ"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonCreator
    public static PaymentStatus fromString(String val) {
        if (val == null || val.isBlank()) return UNPAID;
        String upper = val.trim().toUpperCase();
        return switch (upper) {
            case "PAID", "SUCCESS" -> PAID;
            case "UNPAID" -> UNPAID;
            case "PARTIALLY_PAID", "PARTIAL" -> PARTIALLY_PAID;
            case "PENDING" -> PENDING;
            case "FAILED" -> FAILED;
            case "REFUNDED" -> REFUNDED;
            default -> {
                try {
                    yield PaymentStatus.valueOf(upper);
                } catch (Exception e) {
                    yield UNPAID;
                }
            }
        };
    }
}
