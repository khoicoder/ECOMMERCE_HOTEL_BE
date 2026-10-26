package com.example.BE.dto.admin.response;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalUsers,
        long totalAdmins,
        long activeSessions,
        long totalBookings,
        long totalTodayBookings,
        long totalHotels,
        long pendingBookings,
        long confirmedBookings,
        long cancelledBookings,
        long checkedInBookings,
        long checkedOutBookings,
        BigDecimal totalRevenue,
        String roleScope,
        Long hotelId,
        String hotelName
) {
    public DashboardResponse(
            long totalUsers,
            long totalAdmins,
            long activeSessions,
            long totalBookings,
            long totalTodayBookings,
            long totalHotels,
            long pendingBookings,
            long confirmedBookings,
            long cancelledBookings,
            long checkedInBookings,
            long checkedOutBookings,
            BigDecimal totalRevenue
    ) {
        this(totalUsers, totalAdmins, activeSessions, totalBookings, totalTodayBookings,
                totalHotels, pendingBookings, confirmedBookings, cancelledBookings,
                checkedInBookings, checkedOutBookings, totalRevenue, "SUPER_ADMIN", null, "Toàn Bộ Hệ Thống");
    }
}
