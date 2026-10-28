package com.example.BE.security;

import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Data
public class JwtUtil {
    @Value("${jwt.secret:12345678901234567890123456789012}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserModel user, UUID sessionId) {
        String tenantCode = user.getTenant() != null ? user.getTenant().getCode() : "DEFAULT";
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("type", "access")
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .claim("sid", sessionId.toString())
                .claim("tid", tenantCode)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String extractType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserID(String token) {
        return parseClaims(token).get("uid", Long.class);
    }

    public Role extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        if (role == null) {
            throw new RuntimeException("Role claim is missing");
        }
        return Role.fromString(role);
    }

    public UUID extractSessionID(String token) {
        String sid = parseClaims(token).get("sid", String.class);
        return UUID.fromString(sid);
    }

    public String extractTenantCode(String token) {
        try {
            String tid = parseClaims(token).get("tid", String.class);
            return (tid != null && !tid.isBlank()) ? tid : "DEFAULT";
        } catch (Exception e) {
            return "DEFAULT";
        }
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public String hashtoken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("hashtoken error: " + e.getMessage());
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public UUID extractSessionIDAllowExpired(String token) {
        try {
            Claims claims = parseClaimsAllowExpired(token);
            String sid = claims.get("sid", String.class);
            return sid != null ? UUID.fromString(sid) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserIDAllowExpired(String token) {
        try {
            Claims claims = parseClaimsAllowExpired(token);
            return claims.get("uid", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            String type = extractType(token);
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            String type = extractType(token);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    public String getRemainingTimeFormatted(String token) {
        long remaining = getRemainingTime(token);
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return hours + "h "
                + (minutes % 60) + "m "
                + (seconds % 60) + "s";
    }
}
