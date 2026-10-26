package com.example.BE.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN,
    HOTEL_OWERS,
    RECEPTIONIST;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return USER;
        }
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(value.trim())) {
                return r;
            }
        }
        return USER;
    }
}
