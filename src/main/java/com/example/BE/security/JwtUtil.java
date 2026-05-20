package com.example.BE.security;

import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import io.jsonwebtoken.Claims;
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
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;


    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserModel user, UUID sessionId) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("type","access")
                .claim("uid",user.getId())
                .claim("role",user.getRole().name())
                .claim("sid",sessionId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+accessTokenExpiration))
                .signWith(getKey(),SignatureAlgorithm.HS256)
                .compact();

    }
    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String extractType(String token){
        return parseClaims(token).get("type",String.class);
    }
    public String extractUsername(String token){
        return parseClaims(token).getSubject();
    }

    public Long extractUserID(String token){
        return parseClaims(token).get("uid",Long.class);
    }

    public Role extractRole(String token){
        String role = parseClaims(token).get("role", String.class);
        if (role == null) {
            throw new RuntimeException("Role claim is missing");
        }
        return Role.valueOf(role);
    }
    public UUID extractSessionID(String token){
        String sid = parseClaims(token).get("sid",String.class);
        return UUID.fromString(sid);
    }
    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }


    public String hashtoken(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch(Exception e){
           throw new RuntimeException("hashtoken error"+e.getMessage());
        }

    }
    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public boolean validateAccessToken(String token){
        try{
            String type = extractType(token);
            return type.equals("access");

        }catch(Exception e){
            return false;
        }
    }
    public boolean validateRefreshToken(String token){
        try{
            String type = extractType(token);
            return type.equals("refresh");

    }catch(Exception e){
            return false;
        }

    }


    //debug time sống của Token

    public long getRemainingTime(String token){
        System.out.println("TOKEN = " + token);

        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
    public String getRemainingTimeFormatted(String token){

        long remaining = getRemainingTime(token);
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return hours + "h "
                + (minutes % 60) + "m "
                + (seconds % 60) + "s";
    }

    }





