package com.ansh.wishhub.client;

import com.ansh.wishhub.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE", url = "http://localhost:8081")
public interface AuthServiceClient {

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") String id);

    @GetMapping("/users")
    Map<String, Object> getAllUsers();
}
