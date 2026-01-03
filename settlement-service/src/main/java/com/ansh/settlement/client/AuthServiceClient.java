package com.ansh.settlement.client;

import com.ansh.settlement.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Auth Service.
 * Used to fetch user details for balance calculation responses.
 */
@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    /**
     * Get user by ID
     * Calls Auth Service endpoint: GET /users/{id}
     */
    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable("id") String id);
}
