package com.ansh.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with Cart Service
 */
@FeignClient(name = "CART-SERVICE")
public interface CartServiceClient {

    @DeleteMapping("/cart/user/{userId}")
    void clearUserCart(@PathVariable("userId") String userId);
}
