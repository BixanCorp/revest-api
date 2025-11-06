package com.bixan.revest.controller;

import com.bixan.revest.config.EnvironmentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin
@Slf4j
public class TestController {

    @Autowired
    private EnvironmentConfig environmentConfig;

    /**
     * Simple GET endpoint to test request logging
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(@RequestParam(defaultValue = "World") String name) {
        log.info("Hello endpoint called with name: {}", name);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello " + name + "!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("requestLoggingEnabled", environmentConfig.isRequestLoggingEnabled());

        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint to test request logging with body
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> requestBody) {
        log.info("Echo endpoint called with body size: {} keys", requestBody.size());

        Map<String, Object> response = new HashMap<>();
        response.put("received", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        response.put("requestLoggingEnabled", environmentConfig.isRequestLoggingEnabled());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to toggle request logging dynamically (for testing)
     */
    @PostMapping("/toggle-logging")
    public ResponseEntity<Map<String, Object>> toggleLogging() {
        boolean currentState = environmentConfig.isRequestLoggingEnabled();

        // Set system property to override
        System.setProperty("REQUEST_LOGGING_ENABLED", String.valueOf(!currentState));

        Map<String, Object> response = new HashMap<>();
        response.put("previousState", currentState);
        response.put("newState", !currentState);
        response.put("message", "Request logging toggled");

        log.info("Request logging toggled from {} to {}", currentState, !currentState);

        return ResponseEntity.ok(response);
    }

    /**
     * Get current configuration status
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("activeProfile", environmentConfig.getActiveProfile());
        config.put("requestLoggingEnabled", environmentConfig.isRequestLoggingEnabled());
        config.put("serverPort", environmentConfig.getServerPort());
        config.put("isDevelopmentProfile", environmentConfig.isDevelopmentProfile());

        return ResponseEntity.ok(config);
    }
}