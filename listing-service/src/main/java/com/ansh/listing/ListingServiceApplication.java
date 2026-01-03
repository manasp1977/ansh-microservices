package com.ansh.listing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Listing Service.
 * Handles marketplace listing management for AnshShare platform.
 */
@SpringBootApplication(scanBasePackages = {"com.ansh.listing", "com.ansh.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class ListingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListingServiceApplication.class, args);
    }
}
