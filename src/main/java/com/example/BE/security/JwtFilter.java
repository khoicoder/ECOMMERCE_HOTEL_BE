package com.example.BE.security;

import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
    // path
        String path = request.getServletPath();

        //login
        if (path.startsWith("/api/auth") || path.startsWith("/api/hotels") || path.startsWith("/api/crawl")) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("PATH : " + path);
        String authHeader = request.getHeader("Authorization");

    // 0 co token thi chan
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.validateAccessToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            AuthPrincipal principal = new AuthPrincipal(
                    jwtUtil.extractUserID(token),
                    jwtUtil.extractUsername(token),
                    jwtUtil.extractRole(token),
                    jwtUtil.extractSessionID(token));
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_"+principal.role()));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            System.out.println("JwtFilter chạy:........ " + request.getRequestURI());

        }catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}