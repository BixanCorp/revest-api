package com.bixan.revest.dao.example;

import com.bixan.revest.dao.AbstractBaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Example DAO implementation showing how to use the database access layer
 * This is a template for creating specific DAOs in your application
 */
@Repository
public class ExampleDao extends AbstractBaseDao {

    /**
     * Example method to get current timestamp from database
     * 
     * @return current timestamp as string
     */
    public String getCurrentTimestamp() {
        return jdbcTemplate.queryForObject("SELECT NOW()", String.class);
    }

    /**
     * Example method to get database version
     * 
     * @return database version string
     */
    public String getDatabaseVersion() {
        return jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
    }

    /**
     * Example method to show how to execute a query with parameters
     * 
     * @param limit the limit for the query
     * @return list of maps containing query results
     */
    public List<Map<String, Object>> getExampleData(int limit) {
        String sql = "SELECT ? as example_value, NOW() as current_time, VERSION() as db_version LIMIT ?";
        return jdbcTemplate.queryForList(sql, "Hello from DAO", limit);
    }

    /**
     * Example method to execute a custom query
     * 
     * @param customSql the SQL query to execute
     * @return list of maps containing query results
     */
    public List<Map<String, Object>> executeCustomQuery(String customSql) {
        return jdbcTemplate.queryForList(customSql);
    }

    /**
     * Check if a table exists in the database
     * 
     * @param tableName the name of the table to check
     * @return true if table exists, false otherwise
     */
    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    }

    /**
     * Get all table names in the current database
     * 
     * @return list of table names
     */
    public List<String> getAllTables() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}