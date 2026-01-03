package com.ansh.customer.controller;

import com.ansh.customer.dto.SupportEmailRequest;
import com.ansh.customer.dto.SupportEmailResponse;
import com.ansh.customer.service.CustomerSupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/support")
public class CustomerSupportController {

    @Autowired
    private CustomerSupportService customerSupportService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "customer-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email")
    public ResponseEntity<SupportEmailResponse> sendSupportEmail(
            @Valid @RequestBody SupportEmailRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

        // Set user info from headers if not provided in request
        if (request.getUserId() == null && userId != null) {
            request.setUserId(userId);
        }
        if (request.getUserEmail() == null && userEmail != null) {
            request.setUserEmail(userEmail);
        }

        SupportEmailResponse response = customerSupportService.sendSupportEmail(request);
        return ResponseEntity.ok(response);
    }
}
