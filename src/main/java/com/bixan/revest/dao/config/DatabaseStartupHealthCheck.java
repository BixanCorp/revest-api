package com.bixan.revest.dao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Database startup health check that validates database connectivity when the
 * application starts.
 * If the database is not accessible, the application will be shut down with an
 * error.
 */
@Component
public class DatabaseStartupHealthCheck implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStartupHealthCheck.class);

    private final DatabaseConnectionManager connectionManager;

    @Autowired
    public DatabaseStartupHealthCheck(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("========================================");
        logger.info("Starting database connectivity check...");
        logger.info("========================================");

        try {
            // Log connection information
            String connectionInfo = connectionManager.getConnectionInfo();
            logger.info("Database connection info: {}", connectionInfo);

            // Perform comprehensive health check
            boolean isHealthy = connectionManager.performHealthCheck();

            if (isHealthy) {
                logger.info("========================================");
                logger.info("✅ Database connectivity check PASSED");
                logger.info("✅ Application startup completed successfully");
                logger.info("========================================");
            } else {
                logger.error("========================================");
                logger.error("❌ Database connectivity check FAILED");
                logger.error("❌ Shutting down application due to database connectivity issues");
                logger.error("========================================");

                // Shutdown the application context
                ConfigurableApplicationContext context = (ConfigurableApplicationContext) event.getApplicationContext();

                // Exit with error code
                System.exit(1);
            }

        } catch (Exception e) {
            logger.error("========================================");
            logger.error("❌ Unexpected error during database connectivity check: {}", e.getMessage(), e);
            logger.error("❌ Shutting down application due to database connectivity check failure");
            logger.error("========================================");

            // Exit with error code
            System.exit(1);
        }
    }
}