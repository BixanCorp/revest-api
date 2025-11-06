package com.bixan.revest.dao.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Database configuration class for HikariCP connection pooling
 * Only active when not running tests
 */
@Configuration
@Profile("!test")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${database.host:43.231.232.167}")
    private String dbHost;

    @Value("${database.port:3306}")
    private String dbPort;

    @Value("${database.name:revest}")
    private String dbName;

    @Value("${database.username:bixan_dbuser_dev}")
    private String dbUsername;

    @Value("${database.password:UhZ$dZ6Y55kimlt#gbQ9}")
    private String dbPassword;

    @Value("${database.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${database.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${database.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${database.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${database.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Bean
    public DataSource dataSource() {
        // Log the configuration values being used
        logger.info("=== Database Configuration ===");
        logger.info("Host: {}", dbHost);
        logger.info("Port: {}", dbPort);
        logger.info("Database: {}", dbName);
        logger.info("Username: {}", dbUsername);
        logger.info("Password: {}", dbPassword != null && !dbPassword.isEmpty() ? "***CONFIGURED***" : "***NOT SET***");
        logger.info("===============================");

        HikariConfig config = new HikariConfig();

        // Database connection properties
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                dbHost, dbPort, dbName);

        logger.info("JDBC URL: {}", jdbcUrl);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // HikariCP specific settings
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);

        // Connection pool settings
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("HikariPool-Revest");

        // Additional MySQL optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}