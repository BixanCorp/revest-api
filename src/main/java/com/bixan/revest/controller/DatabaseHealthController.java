package com.bixan.revest.controller;

import com.bixan.revest.dao.config.DatabaseConnectionManager;
import com.bixan.revest.dao.example.ExampleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Database health check controller for monitoring database connectivity
 */
@RestController
@RequestMapping("/api/db")
public class DatabaseHealthController {

    private final DatabaseConnectionManager connectionManager;
    private final ExampleDao exampleDao;

    @Autowired
    public DatabaseHealthController(DatabaseConnectionManager connectionManager, ExampleDao exampleDao) {
        this.connectionManager = connectionManager;
        this.exampleDao = exampleDao;
    }

    /**
     * Check database health status
     * 
     * @return health status response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isHealthy = connectionManager.performHealthCheck();

            response.put("status", isHealthy ? "UP" : "DOWN");
            response.put("timestamp", exampleDao.getCurrentTimestamp());
            response.put("connectionInfo", connectionManager.getConnectionInfo());

            if (isHealthy) {
                response.put("databaseVersion", exampleDao.getDatabaseVersion());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Simple connection test endpoint
     * 
     * @return connection test result
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = connectionManager.validateConnection();
            response.put("connectionValid", isValid);

            if (isValid) {
                response.put("currentTime", exampleDao.getCurrentTimestamp());
                response.put("dbVersion", exampleDao.getDatabaseVersion());
                response.put("sampleData", exampleDao.getExampleData(1));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("connectionValid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}