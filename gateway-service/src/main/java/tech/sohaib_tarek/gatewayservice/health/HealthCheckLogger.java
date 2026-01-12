package net.anassploit.gatewayservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduled health check logger that monitors registered services.
 * Logs health status of all services registered with Eureka.
 */
@Component
@EnableScheduling
public class HealthCheckLogger {

  private static final Logger healthLog = LoggerFactory.getLogger("HEALTH_CHECK");
  private static final Logger log = LoggerFactory.getLogger(HealthCheckLogger.class);
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final DiscoveryClient discoveryClient;

  @Value("${spring.application.name}")
  private String applicationName;

  // Track service status for change detection
  private final Map<String, Boolean> previousServiceStatus = new HashMap<>();

  public HealthCheckLogger(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  /**
   * Log health status every 60 seconds
   */
  @Scheduled(fixedRate = 60000)
  public void logHealthStatus() {
    String timestamp = LocalDateTime.now().format(formatter);

    healthLog.info("HEALTH_CHECK | timestamp={} | service={} | status=UP",
        timestamp, applicationName);

    // Check all registered services
    List<String> services = discoveryClient.getServices();

    healthLog.info("SERVICES_REGISTERED | count={} | services={}",
        services.size(), services);

    for (String serviceName : services) {
      List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

      boolean isUp = !instances.isEmpty();
      Boolean wasUp = previousServiceStatus.get(serviceName);

      // Log status change
      if (wasUp != null && wasUp != isUp) {
        if (isUp) {
          healthLog.info("SERVICE_STATUS_CHANGE | service={} | status=UP | instances={}",
              serviceName, instances.size());
          log.info("Service {} is now UP with {} instances", serviceName, instances.size());
        } else {
          healthLog.warn("SERVICE_STATUS_CHANGE | service={} | status=DOWN", serviceName);
          log.warn("Service {} is now DOWN", serviceName);
        }
      }

      previousServiceStatus.put(serviceName, isUp);

      // Log instance details
      for (ServiceInstance instance : instances) {
        healthLog.info("SERVICE_INSTANCE | service={} | instanceId={} | host={} | port={} | status=UP",
            serviceName,
            instance.getInstanceId(),
            instance.getHost(),
            instance.getPort());
      }
    }
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
