package com.example.BE.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * LogStorageMetrics collects and registers metrics regarding application log storage:
 * - app.log.storage.used.bytes: Total current bytes consumed by logs
 * - app.log.storage.cap.bytes: Configured cap limit in bytes (from logging.logback.rollingpolicy.total-size-cap)
 * - app.log.storage.usage.ratio: Ratio of used bytes to cap limit (0.0 to 1.0+)
 *
 * Implements MeterBinder for Micrometer lifecycle integration and uses periodic sampling
 * to avoid excessive I/O on every metric scrape.
 */
@Slf4j
@Component
public class LogStorageMetrics implements MeterBinder {

    private static final Pattern SIZE_PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]*)\\s*$");
    private static final long DEFAULT_CAP_BYTES = 1024L * 1024L * 1024L; // 1GB default

    private final Path logDirectory;
    private final long capBytes;

    private final AtomicLong currentLogSizeBytes = new AtomicLong(0L);
    private final AtomicReference<Double> currentUsageRatio = new AtomicReference<>(0.0);

    public LogStorageMetrics(
            @Value("${logging.file.name:logs/application.log}") String logFileName,
            @Value("${logging.logback.rollingpolicy.total-size-cap:1GB}") String totalSizeCapStr) {

        Path configuredPath = Paths.get(logFileName);
        Path parent = configuredPath.getParent();
        this.logDirectory = (parent != null) ? parent : Paths.get("logs");

        this.capBytes = parseSizeToBytes(totalSizeCapStr, DEFAULT_CAP_BYTES);

        // Initial scan on startup
        refreshLogMetrics();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("app.log.storage.used.bytes", currentLogSizeBytes, AtomicLong::get)
                .description("Total bytes consumed by application logs in the log directory")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder("app.log.storage.cap.bytes", () -> capBytes)
                .description("Configured log storage total size cap in bytes")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder("app.log.storage.usage.ratio", currentUsageRatio, AtomicReference::get)
                .description("Ratio of current log storage usage to the configured cap (0.0 to 1.0+)")
                .register(registry);
    }

    /**
     * Refreshes log directory size periodically (every 30 seconds).
     * Prevents high disk I/O on rapid Prometheus scrapes.
     */
    @Scheduled(fixedRateString = "${management.metrics.log-storage.refresh-rate-ms:30000}", initialDelay = 5000)
    public void refreshLogMetrics() {
        try {
            long usedBytes = calculateLogDirectorySize(logDirectory);
            currentLogSizeBytes.set(usedBytes);

            double ratio = (capBytes > 0) ? ((double) usedBytes / (double) capBytes) : 0.0;
            currentUsageRatio.set(ratio);
        } catch (Exception e) {
            log.warn("Failed to refresh log storage metrics for directory {}: {}", logDirectory, e.getMessage());
        }
    }

    /**
     * Calculates total bytes safely across files in the directory without loading content into memory.
     * Handles missing directory, permissions, deleted files, and skips symbolic links.
     */
    long calculateLogDirectorySize(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return 0L;
        }

        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(path -> {
                        try {
                            return Files.isRegularFile(path) && !Files.isSymbolicLink(path);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            // File might have been rotated or deleted during walk
                            return 0L;
                        }
                    })
                    .sum();
        } catch (Exception e) {
            log.debug("Error traversing log directory {}: {}", dir, e.getMessage());
            return 0L;
        }
    }

    /**
     * Parses size strings like "1GB", "500MB", "1024KB", "10000B" case-insensitively with optional whitespace.
     * Falls back safely to default value on null, blank, or malformed inputs.
     */
    public static long parseSizeToBytes(String sizeStr, long defaultBytes) {
        if (sizeStr == null || sizeStr.isBlank()) {
            return defaultBytes;
        }

        Matcher matcher = SIZE_PATTERN.matcher(sizeStr.trim());
        if (!matcher.matches()) {
            log.warn("Invalid size cap format '{}', using default {} bytes", sizeStr, defaultBytes);
            return defaultBytes;
        }

        try {
            double value = Double.parseDouble(matcher.group(1));
            if (value < 0) {
                log.warn("Negative size cap '{}' is invalid, using default {} bytes", sizeStr, defaultBytes);
                return defaultBytes;
            }

            String unit = matcher.group(2).toUpperCase();
            long multiplier = switch (unit) {
                case "GB", "G" -> 1024L * 1024L * 1024L;
                case "MB", "M" -> 1024L * 1024L;
                case "KB", "K" -> 1024L;
                case "B", "" -> 1L;
                default -> {
                    log.warn("Unknown size unit '{}' in '{}', using default {} bytes", unit, sizeStr, defaultBytes);
                    yield -1L;
                }
            };

            if (multiplier == -1L) {
                return defaultBytes;
            }

            return (long) (value * multiplier);
        } catch (Exception e) {
            log.warn("Error parsing size cap '{}': {}, using default {} bytes", sizeStr, e.getMessage(), defaultBytes);
            return defaultBytes;
        }
    }

    public long getCurrentLogSizeBytes() {
        return currentLogSizeBytes.get();
    }

    public long getCapBytes() {
        return capBytes;
    }

    public double getCurrentUsageRatio() {
        return currentUsageRatio.get();
    }

    public Path getLogDirectory() {
        return logDirectory;
    }
}
