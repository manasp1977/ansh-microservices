package com.ansh.wishhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.ansh.wishhub", "com.ansh.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class WishHubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WishHubServiceApplication.class, args);
    }
}
