package com.example.BE.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Data
public class JwtUtil {
    private final long AccessToken =  1000 * 60 * 15;
    private final long RefreshToken = 1000L * 60 * 60 * 24 * 7; ;

    private final String SECRET = "12345678901234567890123456789012";

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String buildToken(String username, long expiration){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()+ new Date().getTime()))
                .signWith(getKey(),SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .compact();

    }
    public String generateAccessToken(String username){
        return buildToken(username, AccessToken);
    }
    public String generateRefreshToken(String username){
        return buildToken(username, RefreshToken);
    }
    public String extractUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
                    return true;


        }
    catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //debug time sống của Token
    public Date extractExpiration(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
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





