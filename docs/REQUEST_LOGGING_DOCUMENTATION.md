# Request and Response Logging Configuration

## Overview
The Revest API includes comprehensive request and response logging functionality that can be controlled via environment variables. This feature logs all incoming HTTP requests and outgoing HTTP responses for debugging and monitoring purposes.

## Configuration

### Environment Variable
The logging is controlled by the `REQUEST_LOGGING_ENABLED` environment variable:

- **Default**: `false` (logging disabled)
- **Values**: `true` or `false`
- **Scope**: Controls both incoming request/response logging and outgoing HTTP client logging

### Environment Files
Add the configuration to your environment-specific `.env` files:

```bash
# .env.dev
REQUEST_LOGGING_ENABLED=true

# .env.test  
REQUEST_LOGGING_ENABLED=false

# .env.prod
REQUEST_LOGGING_ENABLED=false
```

### System Property Override
You can override the environment file setting using system properties:

```bash
# Using Maven
mvn spring-boot:run -Drequest.logging.enabled=true

# Using Java command
java -Drequest.logging.enabled=true -jar revest-api.jar

# Using environment variable
export REQUEST_LOGGING_ENABLED=true
./mvnw spring-boot:run
```

## What Gets Logged

### Incoming Requests
For each incoming HTTP request, the following information is logged:

- HTTP method (GET, POST, etc.)
- Request URI and query parameters
- Remote client address
- Content type and content length
- All request headers (with sensitive headers masked)
- Request body (with sensitive endpoints masked)

### Outgoing Responses
For each outgoing HTTP response, the following information is logged:

- HTTP status code
- Response processing duration
- Content type and content length
- All response headers
- Response body (with sensitive data masked)

### Outgoing HTTP Requests
For HTTP requests made by the application (via RestTemplate), logs include:

- HTTP method and target URI
- Request headers (with sensitive headers masked)
- Request body (with sensitive data masked)
- Response status and headers
- Response body

## Security Features

### Sensitive Data Masking
The logging system automatically masks sensitive information:

- **Headers**: Any header containing "authorization", "password", "token", "secret", or "key"
- **Request Bodies**: Authentication endpoints (`/auth/*`, `/login`)
- **Response Bodies**: Authentication responses and error details (configurable)

### Excluded Paths
The following paths are excluded from logging to reduce noise:

- `/actuator/*`
- `/health`
- `/metrics`
- `/favicon.ico`

## Log Format

### Request Log Example
```
=== INCOMING REQUEST ===
Method: POST
URI: /auth/login
Remote Address: 127.0.0.1
Content Type: application/json
Content Length: 156
Headers:
  accept: application/json
  content-type: application/json
  authorization: ***MASKED***
  user-agent: curl/7.68.0
Body:
***SENSITIVE_DATA_MASKED***
========================
```

### Response Log Example
```
=== OUTGOING RESPONSE ===
Method: POST
URI: /auth/login
Status: 200
Duration: 245 ms
Content Type: application/json
Content Length: 287
Headers:
  content-type: application/json
  content-length: 287
Body:
***SENSITIVE_DATA_MASKED***
=========================
```

## Testing the Logging

### Test Endpoints
The API includes test endpoints to verify logging functionality:

```bash
# Test GET request logging
curl -X GET "https://localhost:8443/api/test/hello?name=Developer"

# Test POST request logging  
curl -X POST "https://localhost:8443/api/test/echo" \
  -H "Content-Type: application/json" \
  -d '{"message": "test", "timestamp": 1234567890}'

# Check current configuration
curl -X GET "https://localhost:8443/api/test/config"

# Toggle logging dynamically (for testing)
curl -X POST "https://localhost:8443/api/test/toggle-logging"
```

### Authentication Endpoint Testing
```bash
# Test authentication logging (will show masked data)
curl -X GET "https://localhost:8443/auth/health"

# Test with request body (will show masked data for sensitive endpoints)
curl -X POST "https://localhost:8443/auth/validate" \
  -H "Content-Type: application/json" \
  -d '{"firebaseToken": "test-token"}'
```

## Performance Considerations

### Impact
- **Memory**: Request/response bodies are cached in memory (limited to 1000 bytes for requests)
- **Performance**: Minimal overhead when disabled, ~5-10ms per request when enabled
- **Storage**: Logs to both console and file appenders

### Production Recommendations
- Keep `REQUEST_LOGGING_ENABLED=false` in production unless debugging
- Monitor log file sizes when enabled
- Consider log rotation policies
- Use for short debugging sessions only

## Configuration Management

### Environment-Specific Settings
```bash
# Development (verbose logging)
REQUEST_LOGGING_ENABLED=true
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_BIXAN=DEBUG

# Testing (limited logging)
REQUEST_LOGGING_ENABLED=false
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_BIXAN=INFO

# Production (minimal logging)
REQUEST_LOGGING_ENABLED=false
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_BIXAN=INFO
```

### Runtime Verification
Check if logging is enabled at runtime:
```bash
curl -X GET "https://localhost:8443/api/test/config" | jq '.requestLoggingEnabled'
```

## Troubleshooting

### Logging Not Working
1. Verify environment variable is set: `echo $REQUEST_LOGGING_ENABLED`
2. Check application logs for configuration output
3. Test with system property override: `-Drequest.logging.enabled=true`
4. Verify filter registration in startup logs

### Too Much Log Output
1. Set `REQUEST_LOGGING_ENABLED=false`
2. Add paths to excluded list in `RequestResponseLoggingFilter.EXCLUDED_PATHS`
3. Adjust log levels in `logback-spring.xml`

### Sensitive Data Exposure
The system masks common sensitive patterns, but review:
1. Custom header patterns in `isSensitiveHeader()`
2. Endpoint patterns in `shouldMaskBody()`
3. Add custom masking rules as needed

## Implementation Details

### Key Components
- **RequestResponseLoggingFilter**: Main filter for request/response logging
- **EnvironmentConfig**: Configuration management
- **RestClientConfiguration**: Outgoing HTTP request logging
- **LoggingConfiguration**: Filter registration

### Customization
To modify logging behavior, edit:
- `RequestResponseLoggingFilter.java` - Core logging logic
- `EXCLUDED_PATHS` - Paths to skip
- Masking methods - Sensitive data patterns
- Log formats and content