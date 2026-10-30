package com.example.BE.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system/logs")
public class LogStorageController {
    @Value("${logging.logback.rollingpolicy.total-size-cap:1GB}")
    private String totalSizeCapStr;

    private final Path logDir = Paths.get("logs");

    @GetMapping("/usage")
    public Map<String, Object> getLogStorageUsage() throws IOException {
        long totalLogSizeBytes = 0;

        if (Files.exists(logDir)) {
            try (var stream = Files.walk(logDir)) {
                totalLogSizeBytes = stream
                        .filter(p -> p.toFile().isFile())
                        .mapToLong(p -> p.toFile().length())
                        .sum();
            }
        }

        File partition = logDir.toFile().exists() ? logDir.toFile() : new File(".");
        long totalDiskSpace = partition.getTotalSpace(); // Tổng dung lượng phân vùng ổ cứng
        long usableDiskSpace = partition.getUsableSpace(); // Dung lượng còn trống
        double maxLogCapMB = 1024.0;
        double logSizeMB = totalLogSizeBytes / (1024.0 * 1024.0);
        double totalDiskGB = totalDiskSpace / (1024.0 * 1024.0 * 1024.0);
        double freeDiskGB = usableDiskSpace / (1024.0 * 1024.0 * 1024.0);

        Map<String, Object> result = new HashMap<>();

        // 1. Dung lượng thực tế hiện tại của toàn bộ thư mục logs (tính bằng MB)
// Công thức Math.round(val * 100.0) / 100.0 dùng để làm tròn lấy đúng 2 chữ số thập phân
        result.put("current_log_size_mb", Math.round(logSizeMB * 100.0) / 100.0);

// 2. Ngưỡng dung lượng log tối đa được cấu hình trong Logback (1GB = 1024MB)
// Vượt quá mốc này, Logback sẽ tự kích hoạt rolling policy để xóa/nén log cũ
        result.put("max_log_cap_mb", maxLogCapMB);

// 3. Tỷ lệ % dung lượng log hiện tại đã chiếm so với ngưỡng tối đa được cấp (max_log_cap_mb)
// Công thức: (logSizeMB / maxLogCapMB) * 100, sau đó nhân/chia 100.0 để làm tròn 2 số thập phân (* 10000.0 / 100.0)
// Ví dụ: 0.04MB / 1024MB = ~0.0039% (giúp biết khi nào log sắp đầy hạn mức)
        result.put("log_usage_vs_cap_percent", Math.round((logSizeMB / maxLogCapMB) * 10000.0) / 100.0);

// 4. Tổng dung lượng phân vùng ổ cứng (hoặc container volume) nơi ứng dụng đang chạy (tính bằng GB)
        result.put("total_disk_gb", Math.round(totalDiskGB * 100.0) / 100.0);

// 5. Dung lượng ổ cứng còn trống thực tế mà ứng dụng có thể sử dụng (tính bằng GB)
        result.put("free_disk_gb", Math.round(freeDiskGB * 100.0) / 100.0);

// 6. Tỷ lệ % toàn bộ ổ cứng của máy/server đã bị sử dụng
// Công thức: (1 - usableSpace / totalSpace) * 100% -> được làm tròn 2 số thập phân
// Ví dụ: 14.54% nghĩa là ổ đĩa mới dùng 14.54%, còn trống 85.46%
        result.put("disk_usage_percent", Math.round((1 - (double) usableDiskSpace / totalDiskSpace) * 10000.0) / 100.0);

        return result;
    }
}