package com.example.BE.repository;

import com.example.BE.dto.admin.response.DashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.BE.model.UserModel;

@Repository
public interface DashboardRepository extends JpaRepository<UserModel, Long> {

    @Query(value = """
        SELECT
            (SELECT COUNT(*) FROM users) AS totalUsers,
            (SELECT COUNT(*) FROM users WHERE role = 'ADMIN') AS totalAdmins,
            (SELECT COUNT(*) FROM user_sessions WHERE revoked_at IS NULL) AS activeSessions,
            (SELECT COUNT(*) FROM bookings) AS totalBookings,
            (SELECT COUNT(*) FROM bookings
             WHERE created_at >= CURRENT_DATE
               AND created_at < CURRENT_DATE + INTERVAL 1 DAY) AS todayBookings,
            (SELECT COUNT(*) FROM hotels) as totalHotels), 
            (SELECT COUNT(*) FROM bookings WHERE status = 'PENDING') AS pendingBookings,
            (SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED') AS confirmedBookings,
            (SELECT COUNT(*) FROM bookings WHERE status = 'CANCELLED') AS cancelledBookings,
            (SELECT COUNT(*) FROM bookings WHERE status = 'CHECKED_IN') AS checkedInBookings,
            (SELECT COUNT(*) FROM bookings WHERE status = 'CHECKED_OUT') AS checkedOutBookings,
            (SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE status = 'CONFIRMED') AS totalRevenue
        """, nativeQuery = true)
    DashboardStats getDashboardStats();
}