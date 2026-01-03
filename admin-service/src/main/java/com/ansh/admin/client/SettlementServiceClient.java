package com.ansh.admin.client;

import com.ansh.admin.dto.SettlementDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with Settlement Service
 */
@FeignClient(name = "SETTLEMENT-SERVICE")
public interface SettlementServiceClient {

    @GetMapping("/settlements")
    List<SettlementDTO> getAllSettlements();

    @DeleteMapping("/settlements/{id}")
    void deleteSettlement(@PathVariable("id") String id);
}
