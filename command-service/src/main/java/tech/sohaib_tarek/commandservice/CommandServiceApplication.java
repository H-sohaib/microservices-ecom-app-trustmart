package tech.sohaib_tarek.commandservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Command/Order Microservice - Main Application
 * 
 * Handles order creation, status management, and communicates with Product Service via Feign.
 * Registers with Eureka for service discovery and uses scheduled tasks for health checks.
 */
@SpringBootApplication
@EnableDiscoveryClient // Registers with Eureka service registry
@EnableFeignClients // Enables Feign for inter-service HTTP communication
@EnableScheduling // Enables scheduled task execution for background jobs
public class CommandServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandServiceApplication.class, args);
    }

}
