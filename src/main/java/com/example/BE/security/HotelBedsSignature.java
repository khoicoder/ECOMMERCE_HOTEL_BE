package com.example.BE.security;

import com.example.BE.config.HotelBedsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class HotelBedsSignature {
    private final HotelBedsConfig config;

    public String generateSignature() {
        long timestamp = System.currentTimeMillis() / 1000;
        String raw = config.getApiKey() + config.getSecret() + timestamp;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());

        }
    }
}