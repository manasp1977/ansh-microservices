package com.ansh.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Settlement Service.
 * Handles settlements and balance calculations for AnshShare platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ansh.settlement", "com.ansh.common"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.ansh\\.common\\.security\\..*"
        ))
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class SettlementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementServiceApplication.class, args);
    }
}
