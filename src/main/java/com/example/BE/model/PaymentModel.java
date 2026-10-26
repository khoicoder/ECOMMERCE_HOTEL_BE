package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.PaymentMethod;
import com.example.BE.enums.PaymentProvider;
import com.example.BE.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_model")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_provider")
    private PaymentProvider paymentProvider = PaymentProvider.KHAC;

    @Column(name = "transaction_code")
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private BookingModel booking;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "reference_note")
    private String referenceNote;

    @Column(name = "cash_confirmed")
    private Boolean cashConfirmed;
}
