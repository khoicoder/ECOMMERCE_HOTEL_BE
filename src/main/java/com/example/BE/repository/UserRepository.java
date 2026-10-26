package com.example.BE.repository;

import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUsername(String username);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findById(Long id);

    long countByRole(Role role);
    long countByRoleIn(Collection<Role> roles);

    @Query("""
        SELECT u FROM UserModel u
        WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:role IS NULL OR u.role = :role)
        AND (:active IS NULL OR u.active = :active)
    """)
    Page<UserModel> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT b.user FROM BookingModel b
        WHERE b.room.hotel.id = :hotelId
    """)
    List<UserModel> findUsersByHotelBookings(@Param("hotelId") Long hotelId);
}
