package com.example.BE.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * HotelBusinessMetrics collects essential business metrics for the hotel management system.
 * Tag cardinality is strictly controlled: tags are discrete low-cardinality values
 * (e.g., status, payment_method) and NEVER include high-cardinality IDs (bookingId, userId, roomId).
 */
@Component
public class HotelBusinessMetrics {

    private final Counter bookingSuccessCounter;
    private final Counter bookingFailedCounter;
    private final Counter checkInCounter;
    private final Counter checkOutCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailedCounter;

    public HotelBusinessMetrics(MeterRegistry registry) {
        this.bookingSuccessCounter = Counter.builder("hotel.bookings.total")
                .description("Total hotel bookings created successfully")
                .tag("status", "SUCCESS")
                .register(registry);

        this.bookingFailedCounter = Counter.builder("hotel.bookings.total")
                .description("Total hotel bookings that failed creation")
                .tag("status", "FAILED")
                .register(registry);

        this.checkInCounter = Counter.builder("hotel.guest.checkins.total")
                .description("Total guest check-in events executed")
                .register(registry);

        this.checkOutCounter = Counter.builder("hotel.guest.checkouts.total")
                .description("Total guest check-out events executed")
                .register(registry);

        this.paymentSuccessCounter = Counter.builder("hotel.payments.processed.total")
                .description("Total receptionist or guest payments processed successfully")
                .tag("result", "SUCCESS")
                .register(registry);

        this.paymentFailedCounter = Counter.builder("hotel.payments.processed.total")
                .description("Total receptionist or guest payments that encountered errors")
                .tag("result", "FAILED")
                .register(registry);
    }

    public void recordBookingSuccess() {
        bookingSuccessCounter.increment();
    }

    public void recordBookingFailed() {
        bookingFailedCounter.increment();
    }

    public void recordCheckIn() {
        checkInCounter.increment();
    }

    public void recordCheckOut() {
        checkOutCounter.increment();
    }

    public void recordPaymentSuccess() {
        paymentSuccessCounter.increment();
    }

    public void recordPaymentFailed() {
        paymentFailedCounter.increment();
    }
}
