package com.ansh.analytics.controller;

import com.ansh.analytics.dto.response.PlatformAnalyticsResponse;
import com.ansh.analytics.dto.response.UserAnalyticsResponse;
import com.ansh.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for analytics endpoints.
 * Note: API Gateway strips /api prefix, so routes here don't have /api
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * GET /analytics/user/{userId} - Get user analytics
     * Accessed via API Gateway as: GET /api/analytics/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable String userId) {
        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("analytics", analytics);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /analytics/platform - Get platform-wide analytics
     * Accessed via API Gateway as: GET /api/analytics/platform
     */
    @GetMapping("/platform")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics() {
        PlatformAnalyticsResponse analytics = analyticsService.getPlatformAnalytics();

        Map<String, Object> response = new HashMap<>();
        response.put("platform", "AnshShare");
        response.put("analytics", analytics);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /analytics/top-sellers - Get top sellers
     * Accessed via API Gateway as: GET /api/analytics/top-sellers?limit=10
     */
    @GetMapping("/top-sellers")
    public ResponseEntity<Map<String, Object>> getTopSellers(
            @RequestParam(defaultValue = "10") int limit) {

        List<UserAnalyticsResponse> topSellers = analyticsService.getTopSellers(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("topSellers", topSellers);
        response.put("count", topSellers.size());
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }
}
