package com.example.BE.security;

import com.example.BE.model.UserSession;
import com.example.BE.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SessionCacheService sessionCacheService;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        try {
            // Check for Bearer token with exact prefix
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7).trim();
                    if (jwtUtil.validateAccessToken(token)) {
                        UUID sessionId = jwtUtil.extractSessionID(token);

                        boolean sessionValid = false;
                        if (sessionId != null) {
                            SessionCacheStatus cacheStatus = sessionCacheService.isSessionValid(sessionId);

                            if (cacheStatus == SessionCacheStatus.VALID) {
                                sessionValid = true;
                            } else if (cacheStatus == SessionCacheStatus.REVOKED) {
                                log.debug("[JwtFilter] Session {} is revoked in Redis cache", sessionId);
                                sessionValid = false;
                            } else {
                                // Cache MISS or Redis ERROR (connection failure / timeout)
                                // Safe database fallback: MySQL is authoritative
                                try {
                                    Optional<UserSession> sessionOpt = userSessionRepository.findByIdAndRevokedAtIsNull(sessionId);
                                    if (sessionOpt.isPresent()) {
                                        UserSession session = sessionOpt.get();
                                        if (!session.getRefreshTokenExpireAt().isBefore(Instant.now())) {
                                            sessionValid = true;
                                            // If Redis was functioning but missed the key, warm up the cache
                                            if (cacheStatus == SessionCacheStatus.MISS) {
                                                String roleStr = session.getUser().getRole() != null ? session.getUser().getRole().name() : "USER";
                                                String tid = session.getUser().getTenant() != null ? session.getUser().getTenant().getCode() : "DEFAULT";
                                                sessionCacheService.cacheSession(
                                                        sessionId,
                                                        session.getUser().getId(),
                                                        roleStr,
                                                        tid,
                                                        session.getRefreshTokenExpireAt()
                                                );
                                            }
                                        }
                                    }
                                } catch (Exception dbEx) {
                                    log.error("[JwtFilter] Database fallback session check failed for {}: {}", sessionId, dbEx.getMessage());
                                    sessionValid = false;
                                }
                            }
                        }

                        if (sessionValid) {
                            String tenantCode = jwtUtil.extractTenantCode(token);
                            AuthPrincipal principal = new AuthPrincipal(
                                    jwtUtil.extractUserID(token),
                                    jwtUtil.extractUsername(token),
                                    jwtUtil.extractRole(token),
                                    sessionId,
                                    tenantCode);

                            TenantContext.setTenant(tenantCode);

                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + principal.role()));

                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities);
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        } else {
                            SecurityContextHolder.clearContext();
                        }
                    }
                } catch (Exception e) {
                    log.warn("[JwtFilter] Token validation error: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
