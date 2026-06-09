package com.studentlife.StudentLifeAPIs.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory, per-IP fixed-window rate limiter for the unauthenticated
 * auth endpoints (login / register / refresh-token) to blunt brute-force and
 * credential-stuffing. Returns HTTP 429 with the standard {@link ApiResponse} body.
 *
 * NOTE: state is per-instance. On a horizontally-scaled deployment this should be
 * backed by a shared store (Redis) so limits are enforced across pods. Constructed
 * directly in SecurityConfig (not a @Component) to avoid servlet auto-registration
 * running it for every request outside the security chain.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private record Limit(int maxRequests, long windowMillis) {}

    // Per-endpoint budgets (per client IP).
    private static final Map<String, Limit> LIMITS = Map.of(
            "/api/v1/auth/login", new Limit(10, 60_000),
            "/api/v1/auth/register", new Limit(5, 60_000),
            "/api/v1/auth/refresh-token", new Limit(30, 60_000)
    );

    // Evict idle keys once the map grows past this, to bound memory from spoofed IPs.
    private static final int MAX_TRACKED_KEYS = 50_000;
    private static final long STALE_MILLIS = 600_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static final class Window {
        long start;
        int count;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Limit limit = LIMITS.get(request.getRequestURI());

        if (limit == null || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        String key = request.getRequestURI() + "|" + clientIp(request);

        boolean allowed;
        long retryAfterSeconds = 0;

        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            if (now - window.start >= limit.windowMillis()) {
                window.start = now;
                window.count = 0;
            }
            if (window.count < limit.maxRequests()) {
                window.count++;
                allowed = true;
            } else {
                allowed = false;
                retryAfterSeconds = Math.max(1, (window.start + limit.windowMillis() - now + 999) / 1000);
            }
        }

        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.values().removeIf(w -> now - w.start >= STALE_MILLIS);
        }

        if (allowed) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rate limit exceeded for {} from IP {}", request.getRequestURI(), clientIp(request));
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        objectMapper.writeValue(
                response.getWriter(),
                new ApiResponse<>(429, false, "Too many requests. Please try again later.", null)
        );
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // First hop is the original client when behind a trusted proxy (e.g. Render).
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
