package com.ansh.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for Analytics Service.
 * Provides analytics and reporting by aggregating data from other services.
 */
@SpringBootApplication(scanBasePackages = {"com.ansh.analytics", "com.ansh.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
