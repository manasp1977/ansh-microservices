package com.ansh.admin.client;

import com.ansh.admin.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with Auth Service
 */
@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @GetMapping("/users")
    List<UserDTO> getAllUsers();

    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable("id") String id);
}
