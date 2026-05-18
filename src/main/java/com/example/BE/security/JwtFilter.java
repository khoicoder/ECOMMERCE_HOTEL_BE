package com.example.BE.security;
import com.example.BE.enums.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;


    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
    // path
        String path = request.getServletPath();

        //login
        if (path.startsWith("/api/auth") || path.startsWith("/api/hotels")) {
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
            Claims claims = jwtUtil.parseClaims(token);
            Long userId = (Long) claims.get("id");
            String username =  claims.getSubject();
            String roleStr = (String) claims.get("role",String.class);
            String sessionIdStr = (String) claims.get("sid",String.class);
            Role role = Role.valueOf(roleStr);
            UUID sessionId = UUID.fromString(sessionIdStr);
            AuthPrincipal authPrincipal = new AuthPrincipal(
                    userId,
                    username,
                    role,
                    sessionId);
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE"+role.name()));
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authPrincipal,
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