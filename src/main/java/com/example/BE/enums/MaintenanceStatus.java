package com.example.BE.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MaintenanceStatus {
    PENDING,
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    RESOLVED,
    CANCELLED;

    @JsonCreator
    public static MaintenanceStatus fromString(String val) {
        if (val == null || val.isBlank()) return PENDING;
        String upper = val.trim().toUpperCase();
        if (upper.equals("RESOLVED") || upper.equals("COMPLETED")) return COMPLETED;
        if (upper.equals("OPEN") || upper.equals("PENDING")) return PENDING;
        if (upper.equals("IN_PROGRESS") || upper.equals("PROGRESS")) return IN_PROGRESS;
        if (upper.equals("CANCELLED") || upper.equals("CLOSED")) return CANCELLED;
        try {
            return MaintenanceStatus.valueOf(upper);
        } catch (Exception e) {
            return PENDING;
        }
    }
}
