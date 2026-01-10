package net.anassploit.commandservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Health check logger for command service.
 * Monitors database connectivity and service health.
 */
@Component
@EnableScheduling
public class HealthCheckLogger implements HealthIndicator {

    private static final Logger healthLog = LoggerFactory.getLogger("HEALTH_CHECK");
    private static final Logger log = LoggerFactory.getLogger(HealthCheckLogger.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DataSource dataSource;

    @Value("${spring.application.name}")
    private String applicationName;

    private boolean previousDbStatus = true;

    public HealthCheckLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        boolean dbHealthy = isDatabaseHealthy();
        if (dbHealthy) {
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("service", applicationName)
                    .build();
        } else {
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("service", applicationName)
                    .build();
        }
    }

    /**
     * Log health status every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void logHealthStatus() {
        String timestamp = LocalDateTime.now().format(formatter);

        boolean dbHealthy = isDatabaseHealthy();
        String dbStatus = dbHealthy ? "UP" : "DOWN";

        // Log status change
        if (previousDbStatus != dbHealthy) {
            if (dbHealthy) {
                healthLog.info("DATABASE_STATUS_CHANGE | service={} | database=UP", applicationName);
                log.info("Database connection restored for {}", applicationName);
            } else {
                healthLog.error("DATABASE_STATUS_CHANGE | service={} | database=DOWN", applicationName);
                log.error("Database connection lost for {}", applicationName);
            }
        }
        previousDbStatus = dbHealthy;

        healthLog.info("HEALTH_CHECK | timestamp={} | service={} | status={} | database={}",
                timestamp, applicationName, dbHealthy ? "UP" : "DEGRADED", dbStatus);
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

    private boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}

