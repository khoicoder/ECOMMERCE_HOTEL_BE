package com.example.BE.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ActuatorAndMetricsIntegrationTest {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("1. Actuator Health endpoint hoạt động và trả về trạng thái UP")
    void testActuatorHealthIsUp() {
        assertNotNull(healthEndpoint);
        var health = healthEndpoint.health();
        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    @DisplayName("2. MeterRegistry thu thập đầy đủ JVM, HikariCP, Log Storage và Custom Hotel Business Metrics")
    void testMeterRegistryHasCustomAndSystemMetrics() {
        assertNotNull(meterRegistry);

        // System / JVM Metrics
        assertNotNull(meterRegistry.find("jvm.memory.used").meter(), "JVM memory used metric must be present");
        assertNotNull(meterRegistry.find("jvm.gc.pause").meter(), "JVM GC metric must be present");
        assertNotNull(meterRegistry.find("process.cpu.usage").meter(), "Process CPU metric must be present");

        // Custom Log Storage Metrics
        assertNotNull(meterRegistry.find("app.log.storage.used.bytes").gauge(), "app.log.storage.used.bytes must be registered");
        assertNotNull(meterRegistry.find("app.log.storage.cap.bytes").gauge(), "app.log.storage.cap.bytes must be registered");
        assertNotNull(meterRegistry.find("app.log.storage.usage.ratio").gauge(), "app.log.storage.usage.ratio must be registered");

        // Custom Business Metrics
        assertNotNull(meterRegistry.find("hotel.bookings.total").counter(), "hotel.bookings.total counter must be registered");
        assertNotNull(meterRegistry.find("hotel.guest.checkins.total").counter(), "hotel.guest.checkins.total counter must be registered");
        assertNotNull(meterRegistry.find("hotel.guest.checkouts.total").counter(), "hotel.guest.checkouts.total counter must be registered");
        assertNotNull(meterRegistry.find("hotel.payments.processed.total").counter(), "hotel.payments.processed.total counter must be registered");
    }

    @Test
    @DisplayName("3. Prometheus Scrape format xuất chuẩn xác các metrics")
    void testPrometheusScrapeOutput() {
        assertTrue(meterRegistry instanceof PrometheusMeterRegistry, "Registry must be an instance of PrometheusMeterRegistry");
        PrometheusMeterRegistry prometheusRegistry = (PrometheusMeterRegistry) meterRegistry;
        String scrape = prometheusRegistry.scrape();

        assertNotNull(scrape);
        assertTrue(scrape.contains("app_log_storage_used_bytes"), "Prometheus scrape must contain app_log_storage_used_bytes");
        assertTrue(scrape.contains("app_log_storage_cap_bytes"), "Prometheus scrape must contain app_log_storage_cap_bytes");
        assertTrue(scrape.contains("hotel_bookings_total"), "Prometheus scrape must contain hotel_bookings_total");
    }
}
