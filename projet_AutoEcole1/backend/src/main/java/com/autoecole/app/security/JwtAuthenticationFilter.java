package com.autoecole.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.autoecole.app.tenant.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${app.demo-token.enabled:true}")
    private boolean demoTokenEnabled;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        java.util.Map<String, Object> payload = null;
        boolean isDemoToken = false;
        boolean isSignedJwt = false;

        if (demoTokenEnabled && token.endsWith(".demo-signature")) {
            try {
                payload = parseUnsignedPayload(token);
                if (payload != null) {
                    isDemoToken = true;
                    log.debug("Demo token accepted for sub={}", payload.get("sub"));
                }
            } catch (Exception e) {
                log.warn("Failed to parse demo token: {}", e.getMessage());
            }
        }

        if (!isDemoToken) {
            try {
                String userEmail = jwtService.extractUsername(token);
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        Long tenantId = jwtService.extractTenantId(token);
                        if (tenantId == null) {
                            String headerTenant = request.getHeader("X-Tenant-Id");
                            if (headerTenant != null && !headerTenant.isBlank()) {
                                try { tenantId = Long.parseLong(headerTenant.trim()); } catch (Exception ignored) {}
                            }
                        }
                        if (tenantId != null) {
                            TenantContext.setTenantId(tenantId);
                            log.debug("Tenant {} set from signed JWT for user {}", tenantId, userEmail);
                        } else {
                            log.warn("Signed JWT for user {} has no tenant_id claim", userEmail);
                        }
                        isSignedJwt = true;
                    }
                }
            } catch (Exception e) {
                log.debug("Signed JWT path failed, will try demo-token fallback if applicable: {}", e.getMessage());
            }
        }

        if (isDemoToken && payload != null) {
            String sub = payload.get("sub") != null ? String.valueOf(payload.get("sub")) : null;
            if (sub != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<String> authorities = new ArrayList<>();
                Object authsRaw = payload.get("authorities");
                if (authsRaw instanceof List<?> list) {
                    for (Object o : list) if (o != null) authorities.add(String.valueOf(o));
                } else if (payload.get("role") != null) {
                    authorities.add(String.valueOf(payload.get("role")));
                }
                if (authorities.isEmpty()) {
                    authorities.add("ADMIN");
                }

                List<SimpleGrantedAuthority> granted = authorities.stream()
                        .map(a -> a.startsWith("ROLE_") ? a : a)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UserDetails principal = new User(sub, "demo", granted);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        granted
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Long tenantId = null;
                Object tid = payload.get("tenant_id");
                if (tid instanceof Number num) tenantId = num.longValue();
                else if (tid != null) {
                    try { tenantId = Long.parseLong(String.valueOf(tid)); } catch (Exception ignored) {}
                }
                if (tenantId == null) {
                    String headerTenant = request.getHeader("X-Tenant-Id");
                    if (headerTenant != null && !headerTenant.isBlank()) {
                        try { tenantId = Long.parseLong(headerTenant.trim()); } catch (Exception ignored) {}
                    }
                }
                if (tenantId == null) {
                    tenantId = 1L;
                    log.debug("Demo token: fallback tenant_id=1 (no claim, no X-Tenant-Id)");
                }
                TenantContext.setTenantId(tenantId);
                log.debug("Demo token — Tenant {} set for sub={}, authorities={}", tenantId, sub, authorities);
            }
        }

        if (!isSignedJwt && !isDemoToken) {
            log.warn("Bearer token present but could not be authenticated (not signed JWT, not demo token)");
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Map<String, Object> parseUnsignedPayload(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null;
        String payloadB64 = parts[1]
                .replace('-', '+')
                .replace('_', '/');
        switch (payloadB64.length() % 4) {
            case 2 -> payloadB64 += "==";
            case 3 -> payloadB64 += "=";
            default -> {}
        }
        byte[] bytes = Base64.getDecoder().decode(payloadB64);
        String json = new String(bytes, StandardCharsets.UTF_8);
        return MAPPER.readValue(json, new TypeReference<>() {});
    }
}
