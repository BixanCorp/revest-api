# Database Access Layer (DAO) Documentation

## Overview

This package provides a robust database access layer using HikariCP connection pooling with MySQL 8+ database. The implementation includes automatic connection health checks at startup and comprehensive database connection management.

## Architecture

### Core Components

1. **DatabaseConfig** (`com.bixan.revest.dao.config.DatabaseConfig`)
   - Configures HikariCP connection pool
   - Reads database configuration from environment variables
   - Provides optimized MySQL connection settings

2. **DatabaseConnectionManager** (`com.bixan.revest.dao.config.DatabaseConnectionManager`)
   - Handles database health checks and connection validation
   - Provides connection monitoring capabilities

3. **DatabaseStartupHealthCheck** (`com.bixan.revest.dao.config.DatabaseStartupHealthCheck`)
   - Validates database connectivity at application startup
   - Shuts down application if database is not accessible

4. **BaseDao** & **AbstractBaseDao** (`com.bixan.revest.dao`)
   - Provides common database access functionality
   - Base classes for creating specific DAOs

## Configuration

### Environment Variables

The following environment variables can be set in `.env.dev` or system environment:

```bash
# Database Connection
DATABASE_HOST=43.231.232.167
DATABASE_PORT=3306
DATABASE_NAME=revest
DATABASE_USERNAME=bixan_dbuser_dev
DATABASE_PASSWORD=UhZ$dZ6Y55kimlt#gbQ9

# HikariCP Connection Pool Settings
DATABASE_HIKARI_MAXIMUM_POOL_SIZE=10
DATABASE_HIKARI_MINIMUM_IDLE=5
DATABASE_HIKARI_CONNECTION_TIMEOUT=30000
DATABASE_HIKARI_IDLE_TIMEOUT=600000
DATABASE_HIKARI_MAX_LIFETIME=1800000
```

### Application Properties

The configuration is also mapped to `application-dev.properties`:

```properties
# Database Configuration
database.host=${DATABASE_HOST:43.231.232.167}
database.port=${DATABASE_PORT:3306}
database.name=${DATABASE_NAME:revest}
database.username=${DATABASE_USERNAME:bixan_dbuser_dev}
database.password=${DATABASE_PASSWORD:UhZ$dZ6Y55kimlt#gbQ9}

# HikariCP Configuration
database.hikari.maximum-pool-size=${DATABASE_HIKARI_MAXIMUM_POOL_SIZE:10}
database.hikari.minimum-idle=${DATABASE_HIKARI_MINIMUM_IDLE:5}
database.hikari.connection-timeout=${DATABASE_HIKARI_CONNECTION_TIMEOUT:30000}
database.hikari.idle-timeout=${DATABASE_HIKARI_IDLE_TIMEOUT:600000}
database.hikari.max-lifetime=${DATABASE_HIKARI_MAX_LIFETIME:1800000}
```

## Dependencies

The following dependencies were added to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Usage

### Creating a DAO

1. Extend `AbstractBaseDao` for simple implementation:

```java
@Repository
public class UserDao extends AbstractBaseDao {
    
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
    }
    
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }
}
```

2. Or implement `BaseDao` for custom implementations:

```java
@Repository
public class CustomDao implements BaseDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    
    // Custom methods here
}
```

### Database Health Monitoring

The application provides endpoints for monitoring database health:

- `GET /api/db/health` - Comprehensive health check
- `GET /api/db/test` - Simple connection test

## Startup Behavior

The application will:

1. Initialize HikariCP connection pool
2. Perform database connectivity check
3. Execute a simple query (`SELECT 1`) to validate connection
4. **Shutdown with error code 1 if database is not accessible**

## Connection Pool Settings

The HikariCP pool is configured with MySQL-optimized settings:

- **Maximum Pool Size**: 10 connections
- **Minimum Idle**: 5 connections
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

Additional MySQL optimizations include:
- Prepared statement caching
- Server-side prepared statements
- Batch statement rewriting
- Connection metadata caching

## Error Handling

- Database connection failures at startup will terminate the application
- Runtime connection issues are logged and handled gracefully
- Health check endpoints provide detailed error information

## Security Considerations

- Database credentials are read from environment variables
- Connection pooling prevents connection exhaustion
- SSL can be enabled by modifying the JDBC URL in `DatabaseConfig`

## Monitoring

Monitor the database layer using:
- Application logs (HikariCP and Spring JDBC logging)
- Health check endpoints
- JMX metrics (if enabled)
- Database connection pool metrics

## Troubleshooting

### Common Issues

1. **Connection Timeout**: Check network connectivity and database server status
2. **Authentication Failure**: Verify database credentials
3. **Pool Exhaustion**: Monitor connection usage and adjust pool settings
4. **Startup Failure**: Check database server availability and credentials

### Debugging

Enable debug logging for database components:

```properties
logging.level.com.zaxxer.hikari=DEBUG
logging.level.org.springframework.jdbc=DEBUG
logging.level.com.bixan.revest.dao=DEBUG
```