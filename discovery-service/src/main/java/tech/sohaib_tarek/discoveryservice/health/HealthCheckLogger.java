package net.anassploit.discoveryservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Health check logger for discovery service.
 * Monitors Eureka server health and memory usage.
 */
@Component
@EnableScheduling
public class HealthCheckLogger {

    private static final Logger healthLog = LoggerFactory.getLogger("HEALTH_CHECK");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Log health status every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void logHealthStatus() {
        String timestamp = LocalDateTime.now().format(formatter);

        healthLog.info("HEALTH_CHECK | timestamp={} | service={} | status=UP",
                timestamp, applicationName);
    }

    /**
     * Log detailed health status every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void logDetailedHealthStatus() {
        String timestamp = LocalDateTime.now().format(formatter);

        healthLog.info("DETAILED_HEALTH_CHECK | timestamp={}", timestamp);

        // Get runtime information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        healthLog.info("MEMORY_STATUS | service={} | maxMemoryMB={} | usedMemoryMB={} | freeMemoryMB={}",
                applicationName, maxMemory, usedMemory, freeMemory);

        // Log thread count
        int threadCount = Thread.activeCount();
        healthLog.info("THREAD_STATUS | service={} | activeThreads={}",
                applicationName, threadCount);
    }
}

