package com.bixan.revest.dao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager that handles database health checks and
 * connection validation
 */
@Component
public class DatabaseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseConnectionManager(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates database connectivity by executing a simple query
     * 
     * @return true if database is accessible, false otherwise
     */
    public boolean validateConnection() {
        try {
            logger.info("Validating database connection...");

            // First try to get a connection from the pool
            try (Connection connection = dataSource.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    logger.error("Failed to obtain database connection");
                    return false;
                }

                // Test connection with a simple query
                Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                if (result != null && result == 1) {
                    logger.info("Database connection validation successful");
                    return true;
                } else {
                    logger.error("Database connection validation failed - unexpected query result: {}", result);
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection validation failed - SQL Exception: {}", e.getMessage(), e);
            return false;
        } catch (DataAccessException e) {
            logger.error("Database connection validation failed - Data Access Exception: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Database connection validation failed - Unexpected Exception: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Performs a more comprehensive database health check
     * 
     * @return true if database is healthy, false otherwise
     */
    public boolean performHealthCheck() {
        try {
            logger.info("Performing comprehensive database health check...");

            // Check basic connectivity
            if (!validateConnection()) {
                return false;
            }

            // Check if we can execute a more complex query (optional)
            try {
                jdbcTemplate.queryForObject("SELECT NOW()", String.class);
                logger.info("Database health check passed");
                return true;
            } catch (Exception e) {
                logger.error("Database health check failed during NOW() query: {}", e.getMessage(), e);
                return false;
            }

        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets database connection information for logging/monitoring
     * 
     * @return connection info string
     */
    public String getConnectionInfo() {
        try (Connection connection = dataSource.getConnection()) {
            return String.format("Database: %s, URL: %s, AutoCommit: %s",
                    connection.getCatalog(),
                    connection.getMetaData().getURL(),
                    connection.getAutoCommit());
        } catch (SQLException e) {
            logger.error("Failed to get connection info: {}", e.getMessage());
            return "Connection info unavailable";
        }
    }
}