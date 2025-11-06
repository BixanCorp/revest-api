package com.bixan.revest.dao;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base DAO interface that provides common database access functionality
 */
public interface BaseDao {

    /**
     * Get the JdbcTemplate instance for database operations
     * 
     * @return JdbcTemplate instance
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * Check if the database connection is healthy
     * 
     * @return true if connection is healthy, false otherwise
     */
    default boolean isConnectionHealthy() {
        try {
            getJdbcTemplate().queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}