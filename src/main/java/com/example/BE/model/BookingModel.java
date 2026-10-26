package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.PaymentStatus;
import com.example.BE.enums.StayStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_dates", columnList = "check_in_date, check_out_date"),
        @Index(name = "idx_booking_status", columnList = "booking_status"),
        @Index(name = "idx_payment_status", columnList = "payment_status")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Column(name = "extra_charges")
    private BigDecimal extraCharges = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "stay_status")
    private StayStatus stayStatus = StayStatus.NOT_CHECKED_IN;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_deadline")
    private Instant paymentDeadline;

    @Column(name = "actual_check_in_time")
    private Instant actualCheckInTime;

    @Column(name = "actual_check_out_time")
    private Instant actualCheckOutTime;

    @Column(name = "checked_in_by")
    private String checkedInBy;

    @Column(name = "checked_out_by")
    private String checkedOutBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomModel room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @PrePersist
    public void calculateInitialFinancials() {
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.extraCharges == null) {
            this.extraCharges = BigDecimal.ZERO;
        }
        if (this.totalPrice != null && this.remainingAmount == null) {
            this.remainingAmount = this.totalPrice.add(this.extraCharges).subtract(this.paidAmount);
        }
        if (this.stayStatus == null) {
            this.stayStatus = StayStatus.NOT_CHECKED_IN;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = (this.paidAmount != null && this.totalPrice != null && this.paidAmount.compareTo(this.totalPrice) >= 0)
                    ? PaymentStatus.PAID : PaymentStatus.UNPAID;
        }
    }
}
