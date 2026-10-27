package com.example.BE.controller;

import com.example.BE.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("deploymentMode", TenantContext.isDedicatedMode() ? "DEDICATED" : "SHARED_SAAS");
        response.put("activeTenant", TenantContext.getTenant());

        // Check Database
        Map<String, Object> dbHealth = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            dbHealth.put("status", valid ? "UP" : "DOWN");
            dbHealth.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            response.put("status", "DEGRADED");
        }
        response.put("database", dbHealth);

        // Check Redis (optional fallback)
        Map<String, Object> redisHealth = new HashMap<>();
        try {
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            redisHealth.put("status", "PONG".equalsIgnoreCase(ping) ? "UP" : "DOWN");
        } catch (Exception e) {
            redisHealth.put("status", "OFFLINE_OR_STANDALONE");
            redisHealth.put("note", "Application runs with database-backed session store fallback");
        }
        response.put("redis", redisHealth);

        return ResponseEntity.ok(response);
    }
}
