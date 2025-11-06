package com.bixan.revest.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Configuration
@Slf4j
@Getter
public class EnvironmentConfig {

    private final Environment environment;

    // Server Configuration
    @Value("${server.port:8443}")
    private String serverPort;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // Firebase Configuration
    @Value("${firebase.config.json:}")
    private String firebaseConfigJson;

    // Database Configuration
    @Value("${datasource.url:}")
    private String datasourceUrl;

    @Value("${datasource.username:}")
    private String datasourceUsername;

    @Value("${datasource.password:}")
    private String datasourcePassword;

    // CORS Configuration
    @Value("${cors.allowed.origins:}")
    private String corsAllowedOrigins;

    // Logging Configuration
    @Value("${logging.level.root:INFO}")
    private String loggingLevelRoot;

    @Value("${logging.level.com.bixan:DEBUG}")
    private String loggingLevelBixan;

    // Request Logging Configuration
    @Value("${request.logging.enabled:false}")
    private String requestLoggingEnabled;

    // JWT Session Configuration
    @Value("${jwt.secret.salt:}")
    private String jwtSecretSalt;

    @Value("${session.duration.seconds:3600}")
    private String sessionDurationSeconds;

    // Environment-specific properties
    private Properties envProperties;

    public EnvironmentConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadEnvironmentVariables() {
        log.info("Loading environment configuration for profile: {}", activeProfile);

        // Load environment-specific .env file
        loadEnvFile();

        // Override with system environment variables
        loadSystemEnvironmentVariables();

        // Log configuration (without sensitive data)
        logConfiguration();
    }

    private void loadEnvFile() {
        String envFileName = String.format(".env.%s", activeProfile);
        Path envFilePath = Paths.get(envFileName);

        if (!Files.exists(envFilePath)) {
            // Fallback to default .env file
            envFilePath = Paths.get(".env");
        }

        if (Files.exists(envFilePath)) {
            try {
                envProperties = new Properties();
                envProperties.load(new FileInputStream(envFilePath.toFile()));
                log.info("Loaded environment file: {}", envFilePath.getFileName());

                // Set system properties from env file
                envProperties.forEach((key, value) -> {
                    String keyStr = key.toString();
                    String valueStr = value.toString();

                    // Only set if not already defined
                    if (System.getProperty(keyStr) == null && System.getenv(keyStr) == null) {
                        System.setProperty(keyStr, valueStr);
                    }

                    // Also convert environment variable format to property format for specific
                    // prefixes
                    if (keyStr.startsWith("FIREBASE_") ||
                            keyStr.startsWith("DB_") ||
                            keyStr.startsWith("DATABASE_") ||
                            keyStr.startsWith("CORS_") ||
                            keyStr.startsWith("LOGGING_") ||
                            keyStr.startsWith("REQUEST_")) {

                        String propertyKey = convertEnvKeyToPropertyKey(keyStr);
                        if (System.getProperty(propertyKey) == null) {
                            System.setProperty(propertyKey, valueStr);
                            log.debug("Converted env file variable {} to property {}", keyStr, propertyKey);
                        }
                    }
                });

            } catch (IOException e) {
                log.warn("Could not load environment file: {}", envFilePath.getFileName());
            }
        } else {
            log.warn("No environment file found for profile: {}", activeProfile);
            envProperties = new Properties();
        }
    }

    private void loadSystemEnvironmentVariables() {
        // Override with system environment variables
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("FIREBASE_") ||
                    key.startsWith("DB_") ||
                    key.startsWith("DATABASE_") ||
                    key.startsWith("CORS_") ||
                    key.startsWith("LOGGING_") ||
                    key.startsWith("REQUEST_")) {

                // Convert environment variable format to property format
                String propertyKey = convertEnvKeyToPropertyKey(key);
                System.setProperty(propertyKey, value);
                log.debug("Loaded system environment variable: {}", key);
            }
        });
    }

    private String convertEnvKeyToPropertyKey(String envKey) {
        return envKey.toLowerCase().replace("_", ".");
    }

    private void logConfiguration() {
        log.info("=== Environment Configuration ===");
        log.info("Active Profile: {}", activeProfile);
        log.info("Server Port: {}", serverPort);
        log.info("Firebase Config: {}", firebaseConfigJson.isEmpty() ? "Not configured" : "Configured");
        log.info("JWT Secret Salt: {}", getJwtSecretSalt().isEmpty() ? "Not configured" : "Configured");
        log.info("Session Duration: {} seconds", getSessionDurationSeconds());
        log.info("CORS Origins: {}", corsAllowedOrigins);
        log.info("Logging Level (Root): {}", loggingLevelRoot);
        log.info("Logging Level (Bixan): {}", loggingLevelBixan);
        log.info("Request Logging Enabled: {}", isRequestLoggingEnabled());

        if (!datasourceUrl.isEmpty()) {
            log.info("Database URL: {}", datasourceUrl);
            log.info("Database Username: {}", datasourceUsername);
        }

        log.info("=== End Configuration ===");
    }

    // Utility methods to get configuration values
    public String getFirebaseConfig() {
        // Try multiple sources for Firebase config

        // First check system environment variables
        String config = System.getenv("FIREBASE_CONFIG_JSON");
        if (config != null && !config.isEmpty()) {
            log.debug("Found Firebase config in system env: FIREBASE_CONFIG_JSON");
            return config;
        }

        config = System.getenv("FIREBASE_CONFIG_JSON_" + activeProfile.toUpperCase());
        if (config != null && !config.isEmpty()) {
            log.debug("Found Firebase config in system env: FIREBASE_CONFIG_JSON_{}", activeProfile.toUpperCase());
            return config;
        }

        // Then check system properties (loaded from .env files)
        config = System.getProperty("FIREBASE_CONFIG_JSON");
        if (config != null && !config.isEmpty()) {
            log.debug("Found Firebase config in system properties: FIREBASE_CONFIG_JSON");
            return config;
        }

        config = System.getProperty("FIREBASE_CONFIG_JSON_" + activeProfile.toUpperCase());
        if (config != null && !config.isEmpty()) {
            log.debug("Found Firebase config in system properties: FIREBASE_CONFIG_JSON_{}",
                    activeProfile.toUpperCase());
            return config;
        }

        // Finally check the Spring property
        if (firebaseConfigJson != null && !firebaseConfigJson.isEmpty()) {
            log.debug("Found Firebase config in Spring property: firebase.config.json");
            return firebaseConfigJson;
        }

        log.warn("No Firebase configuration found in any source");
        return firebaseConfigJson;
    }

    public String[] getCorsOrigins() {
        String origins = corsAllowedOrigins;
        if (origins == null || origins.isEmpty()) {
            origins = "http://localhost:3000,http://localhost:5173";
        }
        return origins.split(",");
    }

    public boolean isProductionProfile() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }

    public boolean isDevelopmentProfile() {
        return "dev".equalsIgnoreCase(activeProfile) || "development".equalsIgnoreCase(activeProfile);
    }

    public boolean isTestProfile() {
        return "test".equalsIgnoreCase(activeProfile);
    }

    public boolean isRequestLoggingEnabled() {
        String enabled = getProperty("REQUEST_LOGGING_ENABLED", requestLoggingEnabled);
        log.debug("Request logging enabled check: property={}, default={}, result={}", enabled, requestLoggingEnabled,
                "true".equalsIgnoreCase(enabled));
        return "true".equalsIgnoreCase(enabled);
    }

    public String getJwtSecretSalt() {
        String salt = getProperty("JWT_SECRET_SALT", jwtSecretSalt);
        if (salt == null || salt.trim().isEmpty()) {
            log.warn("JWT_SECRET_SALT is not configured. Using default (not secure for production)");
            return "default-insecure-salt-change-in-production";
        }
        return salt;
    }

    public int getSessionDurationSeconds() {
        String duration = getProperty("SESSION_DURATION_SECONDS", sessionDurationSeconds);
        try {
            return Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            log.warn("Invalid SESSION_DURATION_SECONDS value: {}. Using default 3600 seconds", duration);
            return 3600;
        }
    }

    // Get property with fallback
    public String getProperty(String key, String defaultValue) {
        // Check system properties first
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        // Check environment variables
        value = System.getenv(key);
        if (value != null) {
            return value;
        }

        // Check env file properties
        if (envProperties != null) {
            value = envProperties.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        // Check Spring environment
        value = environment.getProperty(key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }
}