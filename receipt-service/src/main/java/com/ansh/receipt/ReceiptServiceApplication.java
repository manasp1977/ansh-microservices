package com.ansh.receipt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Receipt Service Application
 * Handles receipt upload, storage, and management
 */
@SpringBootApplication(scanBasePackages = {"com.ansh.receipt", "com.ansh.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class ReceiptServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptServiceApplication.class, args);
    }
}
