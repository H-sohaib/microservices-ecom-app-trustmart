package tech.sohaib_tarek.commandservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client Configuration
 * 
 * Configures request interceptors to add gateway authentication header
 * for secure inter-service communication.
 */
@Configuration
public class FeignConfig {

  @Value("${gateway.secret:TrustMartGatewaySecretKey2024}")
  private String gatewaySecret;

  @Bean
  public RequestInterceptor gatewaySecretInterceptor() {
    return template -> template.header("X-Gateway-Secret", gatewaySecret);
  }
}
