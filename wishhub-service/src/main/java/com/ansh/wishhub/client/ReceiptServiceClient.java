package com.ansh.wishhub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "RECEIPT-SERVICE", url = "http://localhost:8082")
public interface ReceiptServiceClient {

    @DeleteMapping("/receipts/by-wish/{wishId}")
    Map<String, Object> deleteReceiptsByWishId(@PathVariable("wishId") String wishId);
}
