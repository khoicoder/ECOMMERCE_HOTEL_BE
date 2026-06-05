package com.example.BE.dto.admin.response;

import java.math.BigDecimal;

public interface DashboardStats {
    Long getTotalUsers();

    Long getTotalAdmins();

    Long getActiveSessions();

    Long getTotalBookings();
    Long getTotalTodayBookings();
    Long getTotalHotel();
    Long getPendingBookings();

    Long getConfirmedBookings();

    Long getCancelledBookings();
    Long getCheckIns();
    Long getCheckInsToday();
    Long getCheckOuts();
    Long getCheckOutsToday();

    BigDecimal getTotalRevenue();
}
