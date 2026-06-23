package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.PaymentMethod;
import com.example.BE.enums.PaymentProvider;
import com.example.BE.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;



import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentModel extends BaseAuditable {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(value = EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(value = EnumType.STRING)
    private PaymentProvider  paymentProvider;

    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private BookingModel booking;
    private LocalDateTime paidAt;
}
