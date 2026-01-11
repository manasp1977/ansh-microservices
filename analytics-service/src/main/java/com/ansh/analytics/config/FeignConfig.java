package com.ansh.analytics.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign client configuration for Analytics Service.
 * Provides error handling, logging, and retry logic for service-to-service communication.
 */
@Configuration
public class FeignConfig {

    /**
     * Configure Feign logger level for debugging
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Configure request timeouts
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                10, TimeUnit.SECONDS,  // Connect timeout
                60, TimeUnit.SECONDS,  // Read timeout
                true                    // Follow redirects
        );
    }

    /**
     * Configure retry logic
     * Retries up to 3 times with exponential backoff
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100,   // Initial interval (ms)
                1000,  // Max interval (ms)
                3      // Max attempts
        );
    }

    /**
     * Custom error decoder to handle Feign errors gracefully
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}
