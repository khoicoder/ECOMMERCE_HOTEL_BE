package com.example.BE.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET = "12345678901234567890123456789012";
    private Key getkey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    public String createToken(String username){
        return Jwts.builder().setSubject(username).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+86400000)).signWith(getkey(),SignatureAlgorithm.HS256).compact() ;
    }
    public String extractUsername(String token){
        return Jwts.parserBuilder().setSigningKey(getkey()).build().parseClaimsJws(token).getBody().getSubject();
    }
    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60))
                .signWith(getkey(),SignatureAlgorithm.HS256)
                .compact();

    }


}
