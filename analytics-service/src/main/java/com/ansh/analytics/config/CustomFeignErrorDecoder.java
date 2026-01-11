package com.ansh.analytics.config;

import com.ansh.common.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Feign error decoder to handle errors from downstream services.
 * Provides better error messages and graceful degradation.
 */
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CustomFeignErrorDecoder.class);
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.error("Feign client error: method={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        switch (response.status()) {
            case 404:
                return new ResourceNotFoundException("Resource not found in downstream service: " + methodKey);
            case 503:
                return new RuntimeException("Service temporarily unavailable: " + methodKey);
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
}
