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

        BigDecimal totalRevenue


) {
    //có thể bổ sung thêm:
    //Revenue Today
    //Revenue This Month
    //Top Hotels
    //Top Customers
    //New Users Today
    //Booking Trend 7 ngày gần nhất
}
