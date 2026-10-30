package com.example.BE.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LogStorageMetricsTest {

    @Test
    @DisplayName("1. parseSizeToBytes: Phân tích chuẩn xác các đơn vị GB, MB, KB, B và khoảng trắng")
    void testParseSizeToBytesValidUnits() {
        long defaultBytes = 1024L * 1024L * 1024L;

        assertEquals(1024L * 1024L * 1024L, LogStorageMetrics.parseSizeToBytes("1GB", defaultBytes));
        assertEquals(2L * 1024L * 1024L * 1024L, LogStorageMetrics.parseSizeToBytes("2 gb", defaultBytes));
        assertEquals(500L * 1024L * 1024L, LogStorageMetrics.parseSizeToBytes("500MB", defaultBytes));
        assertEquals(250L * 1024L * 1024L, LogStorageMetrics.parseSizeToBytes("  250 mb  ", defaultBytes));
        assertEquals(1024L * 1024L, LogStorageMetrics.parseSizeToBytes("1024KB", defaultBytes));
        assertEquals(1048576L, LogStorageMetrics.parseSizeToBytes("1048576B", defaultBytes));
        assertEquals(2048L, LogStorageMetrics.parseSizeToBytes("2048", defaultBytes));
    }

    @Test
    @DisplayName("2. parseSizeToBytes: Xử lý an toàn khi đầu vào null, rỗng, số âm hoặc sai định dạng")
    void testParseSizeToBytesInvalidInputs() {
        long defaultBytes = 500L;

        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes(null, defaultBytes));
        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes("", defaultBytes));
        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes("   ", defaultBytes));
        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes("-10GB", defaultBytes));
        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes("abcGB", defaultBytes));
        assertEquals(defaultBytes, LogStorageMetrics.parseSizeToBytes("100TB_UNKNOWN", defaultBytes));
    }

    @Test
    @DisplayName("3. calculateLogDirectorySize: Tính toán an toàn với thư mục không tồn tại, rỗng, có file và rotation")
    void testCalculateLogDirectorySize(@TempDir Path tempDir) throws IOException {
        LogStorageMetrics metrics = new LogStorageMetrics(tempDir.resolve("app.log").toString(), "1GB");

        // 1. Empty dir
        assertEquals(0L, metrics.calculateLogDirectorySize(tempDir));

        // 2. Non-existent dir
        Path nonExistent = tempDir.resolve("missing-folder");
        assertEquals(0L, metrics.calculateLogDirectorySize(nonExistent));

        // 3. Dir with files
        Path logFile1 = tempDir.resolve("app.log");
        Path logFile2 = tempDir.resolve("app-archived.log.gz");
        Files.writeString(logFile1, "Hello Spring Boot Actuator Observability");
        Files.writeString(logFile2, "Archived Log Content");

        long expectedSize = Files.size(logFile1) + Files.size(logFile2);
        assertTrue(expectedSize > 0);
        assertEquals(expectedSize, metrics.calculateLogDirectorySize(tempDir));

        // 4. Test periodic refresh updates AtomicLong and ratio
        metrics.refreshLogMetrics();
        assertEquals(expectedSize, metrics.getCurrentLogSizeBytes());
        assertTrue(metrics.getCurrentUsageRatio() > 0.0);
        assertTrue(metrics.getCurrentUsageRatio() < 1.0);
    }
}
