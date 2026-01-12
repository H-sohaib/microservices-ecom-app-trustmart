package tech.sohaib_tarek.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Product Microservice - Main Application
 * 
 * Manages product catalog with CRUD operations and stock management.
 * Integrates with Eureka for service discovery in the microservices architecture.
 */
@SpringBootApplication
@EnableDiscoveryClient // Registers with Eureka for dynamic service discovery
@EnableScheduling // Enables background scheduled tasks
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
