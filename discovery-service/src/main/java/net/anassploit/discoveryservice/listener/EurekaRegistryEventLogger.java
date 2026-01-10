package net.anassploit.discoveryservice.listener;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Listener for Eureka registry events.
 * Logs service registration, deregistration, and renewal events.
 */
@Component
public class EurekaRegistryEventLogger {

    private static final Logger registryLog = LoggerFactory.getLogger("REGISTRY_LOG");
    private static final Logger log = LoggerFactory.getLogger(EurekaRegistryEventLogger.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log when a new instance registers with Eureka
     */
    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        String timestamp = LocalDateTime.now().format(formatter);

        registryLog.info("SERVICE_REGISTERED | timestamp={} | appName={} | instanceId={} | host={} | port={} | status={}",
                timestamp,
                instanceInfo.getAppName(),
                instanceInfo.getInstanceId(),
                instanceInfo.getHostName(),
                instanceInfo.getPort(),
                instanceInfo.getStatus());

        log.info("Service registered: {} ({}:{})",
                instanceInfo.getAppName(),
                instanceInfo.getHostName(),
                instanceInfo.getPort());
    }

    /**
     * Log when an instance renews its lease (heartbeat)
     * Note: This can be very verbose, so only log at DEBUG level
     */
    @EventListener
    public void onInstanceRenewed(EurekaInstanceRenewedEvent event) {
        if (registryLog.isDebugEnabled()) {
            String timestamp = LocalDateTime.now().format(formatter);

            registryLog.debug("SERVICE_HEARTBEAT | timestamp={} | appName={} | serverId={}",
                    timestamp,
                    event.getAppName(),
                    event.getServerId());
        }
    }

    /**
     * Log when Eureka registry is available
     */
    @EventListener
    public void onRegistryAvailable(EurekaRegistryAvailableEvent event) {
        String timestamp = LocalDateTime.now().format(formatter);

        registryLog.info("REGISTRY_AVAILABLE | timestamp={}", timestamp);
        log.info("Eureka registry is now available");
    }

    /**
     * Log when Eureka server is started
     */
    @EventListener
    public void onServerStarted(EurekaServerStartedEvent event) {
        String timestamp = LocalDateTime.now().format(formatter);

        registryLog.info("EUREKA_SERVER_STARTED | timestamp={}", timestamp);
        log.info("Eureka server started successfully");
    }
}
