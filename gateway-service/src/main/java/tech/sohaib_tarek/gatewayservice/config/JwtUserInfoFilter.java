package net.anassploit.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtUserInfoFilter implements GlobalFilter, Ordered {

  @Value("${gateway.secret:TrustMartGatewaySecretKey2024}")
  private String gatewaySecret;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .map(authentication -> (JwtAuthenticationToken) authentication)
        .map(jwtAuth -> {
          Jwt jwt = jwtAuth.getToken();

          String userId = jwt.getSubject();
          String username = jwt.getClaimAsString("preferred_username");

          // Extract roles from realm_access.roles
          List<String> roles = extractRoles(jwt);
          String rolesString = String.join(",", roles);

          // Add user info headers and gateway secret to the request
          ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
              .header("X-User-Id", userId != null ? userId : "")
              .header("X-User-Name", username != null ? username : "")
              .header("X-User-Roles", rolesString)
              .header("X-Gateway-Secret", gatewaySecret)
              .build();

          return exchange.mutate().request(modifiedRequest).build();
        })
        .defaultIfEmpty(addGatewaySecretHeader(exchange))
        .flatMap(chain::filter);
  }

  private ServerWebExchange addGatewaySecretHeader(ServerWebExchange exchange) {
    // For unauthenticated requests, still add the gateway secret
    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
        .header("X-Gateway-Secret", gatewaySecret)
        .build();
    return exchange.mutate().request(modifiedRequest).build();
  }

  private List<String> extractRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null && realmAccess.containsKey("roles")) {
      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) realmAccess.get("roles");
      return roles != null ? roles : List.of();
    }
    return List.of();
  }

  @Override
  public int getOrder() {
    return -1; // Run before other filters
  }
}
