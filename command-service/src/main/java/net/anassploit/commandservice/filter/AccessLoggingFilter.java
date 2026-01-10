package net.anassploit.commandservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for logging API access with user identification and request tracing.
 * Logs include:
 * - Request method and path
 * - User ID and roles (from gateway headers)
 * - Response status code
 * - Request duration
 * - Trace ID for request correlation
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLoggingFilter extends OncePerRequestFilter {

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY_LOG");
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Get or generate trace ID
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Extract user information from gateway headers
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String userRoles = request.getHeader("X-User-Roles");

        // Set MDC context for logging
        MDC.put("traceId", traceId);
        MDC.put("userId", userId != null ? userId : "anonymous");
        MDC.put("userName", username != null ? username : "anonymous");
        MDC.put("userRoles", userRoles != null ? userRoles : "none");

        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? path + "?" + queryString : path;
        String clientIp = getClientIp(request);

        // Skip logging for health checks and actuator endpoints to reduce noise
        boolean isHealthCheck = path.contains("/actuator") || path.contains("/health");

        if (!isHealthCheck) {
            accessLog.info("REQUEST_START | method={} | path={} | clientIp={} | user={} | roles={}",
                    method, fullPath, clientIp, username != null ? username : "anonymous",
                    userRoles != null ? userRoles : "none");
        }

        // Wrap response to capture status code
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // Add trace ID to response
            response.setHeader(TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = responseWrapper.getStatus();

            if (!isHealthCheck) {
                // Determine log level based on status code
                if (statusCode >= 500) {
                    accessLog.error("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                            method, fullPath, statusCode, duration, username != null ? username : "anonymous");
                } else if (statusCode >= 400) {
                    accessLog.warn("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                            method, fullPath, statusCode, duration, username != null ? username : "anonymous");

                    // Log security events for unauthorized/forbidden access
                    if (statusCode == 401 || statusCode == 403) {
                        securityLog.warn("SECURITY_DENIED | method={} | path={} | status={} | user={} | clientIp={}",
                                method, fullPath, statusCode, username != null ? username : "anonymous", clientIp);
                    }
                } else {
                    accessLog.info("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                            method, fullPath, statusCode, duration, username != null ? username : "anonymous");
                }
            }

            // Copy body to response
            responseWrapper.copyBodyToResponse();

            // Clear MDC
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

