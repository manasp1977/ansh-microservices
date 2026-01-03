package com.ansh.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Cart Service.
 * Handles shopping cart management for AnshShare platform.
 */
@SpringBootApplication(scanBasePackages = {"com.ansh.cart", "com.ansh.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
