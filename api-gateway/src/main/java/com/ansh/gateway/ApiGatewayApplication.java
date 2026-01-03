package com.ansh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * API Gateway Application
 * Routes all client requests to appropriate microservices
 * Handles JWT authentication at gateway level
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ansh.gateway", "com.ansh.common"})
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
