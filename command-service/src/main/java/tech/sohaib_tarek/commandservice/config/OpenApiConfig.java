package tech.sohaib_tarek.commandservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI commandServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Command Service API")
            .version("1.0.0")
            .description("REST API for managing orders/commands in the e-commerce application"));
  }
}
