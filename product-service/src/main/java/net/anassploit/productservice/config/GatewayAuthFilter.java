package net.anassploit.productservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class GatewayAuthFilter implements Filter {

    @Value("${gateway.secret:TrustMartGatewaySecretKey2024}")
    private String expectedSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow Swagger/OpenAPI documentation
        String path = httpRequest.getRequestURI();
        if (path.contains("/swagger") || path.contains("/v3/api-docs") || path.contains("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // Check for gateway secret header
        String gatewaySecret = httpRequest.getHeader("X-Gateway-Secret");

        if (gatewaySecret == null || !gatewaySecret.equals(expectedSecret)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Direct access not allowed. Please use the API gateway.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}

