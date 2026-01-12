package net.anassploit.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class AccessLoggingFilter implements GlobalFilter, Ordered {

  private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");
  private static final Logger securityLog = LoggerFactory.getLogger("SECURITY_LOG");
  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String START_TIME_ATTR = "startTime";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    // Generate trace ID for request correlation
    String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
    if (traceId == null || traceId.isEmpty()) {
      traceId = UUID.randomUUID().toString().substring(0, 8);
    }

    // Extract user information from headers (set by JwtUserInfoFilter)
    String userId = request.getHeaders().getFirst("X-User-Id");
    String username = request.getHeaders().getFirst("X-User-Name");
    String userRoles = request.getHeaders().getFirst("X-User-Roles");

    // Set MDC context for logging
    MDC.put("traceId", traceId);
    MDC.put("userId", userId != null ? userId : "anonymous");
    MDC.put("userName", username != null ? username : "anonymous");
    MDC.put("userRoles", userRoles != null ? userRoles : "none");

    // Store start time
    long startTime = Instant.now().toEpochMilli();
    exchange.getAttributes().put(START_TIME_ATTR, startTime);

    String finalTraceId = traceId;
    String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
    String path = request.getURI().getPath();
    String clientIp = getClientIp(request);
    String finalUsername = username;
    String finalUserRoles = userRoles;

    // Add trace ID to the request for downstream services
    ServerHttpRequest modifiedRequest = request.mutate()
        .header(TRACE_ID_HEADER, finalTraceId)
        .build();

    // Add trace ID to response headers BEFORE the response is committed
    ServerHttpResponse response = exchange.getResponse();
    response.getHeaders().add(TRACE_ID_HEADER, finalTraceId);

    ServerWebExchange modifiedExchange = exchange.mutate()
        .request(modifiedRequest)
        .response(response)
        .build();

    accessLog.info("REQUEST_START | method={} | path={} | clientIp={} | user={} | roles={}",
        method, path, clientIp, finalUsername != null ? finalUsername : "anonymous",
        finalUserRoles != null ? finalUserRoles : "none");

    // Log security-relevant requests
    if (isSecurityRelevantRequest(path)) {
      securityLog.info("SECURITY_ACCESS | method={} | path={} | user={} | clientIp={}",
          method, path, finalUsername != null ? finalUsername : "anonymous", clientIp);
    }

    return chain.filter(modifiedExchange)
        .doFinally(signalType -> {
          // Log response after completion
          Long start = exchange.getAttribute(START_TIME_ATTR);
          long duration = start != null ? Instant.now().toEpochMilli() - start : 0;

          int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

          // Determine log level based on status code
          if (statusCode >= 500) {
            accessLog.error("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                method, path, statusCode, duration, finalUsername != null ? finalUsername : "anonymous");
          } else if (statusCode >= 400) {
            accessLog.warn("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                method, path, statusCode, duration, finalUsername != null ? finalUsername : "anonymous");

            // Log security events for unauthorized/forbidden access
            if (statusCode == 401 || statusCode == 403) {
              securityLog.warn("SECURITY_DENIED | method={} | path={} | status={} | user={} | clientIp={}",
                  method, path, statusCode, finalUsername != null ? finalUsername : "anonymous", clientIp);
            }
          } else {
            accessLog.info("REQUEST_END | method={} | path={} | status={} | duration={}ms | user={}",
                method, path, statusCode, duration, finalUsername != null ? finalUsername : "anonymous");
          }

          // Clear MDC
          MDC.clear();
        });
  }

  @Override
  public int getOrder() {
    // Run early in the filter chain
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }

  private String getClientIp(ServerHttpRequest request) {
    String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeaders().getFirst("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
  }

  private boolean isSecurityRelevantRequest(String path) {
    return path.contains("/users") ||
        path.contains("/auth") ||
        path.contains("/admin") ||
        path.contains("/login") ||
        path.contains("/logout");
  }
}