package com.example.BE.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

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
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String buildToken(String username, long expiration,String type){


        return Jwts.builder()
                .setSubject(username)
                .claim("type", type)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(getKey(),SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .compact();

    }
    public String generateAccessToken(String username){
        return buildToken(username, accessTokenExpiration,"access");
    }
    public String generateRefreshToken(String username){
        return buildToken(username, refreshTokenExpiration,"refresh");
    }
    public String extractType(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token).getBody().get("type", String.class);
    }
    public String extractUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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

//    public boolean validateToken(String token){
//        try{
//            Jwts.parserBuilder()
//                    .setSigningKey(getKey())
//                    .build()
//                    .parseClaimsJws(token);
//                    return true;
//
//
//        }
//    catch (Exception e) {
//            System.out.println(e.getMessage());
//            return false;
//        }
//    }

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





