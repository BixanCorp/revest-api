# Development Environment Setup

This document provides instructions for setting up the Revest API development environment with VS Code.

## Prerequisites

- Java 8 or higher
- Maven 3.6+
- VS Code with Java Extension Pack
- Firebase project(s) set up

## Environment Configuration

The API supports three environments: **development**, **test**, and **production**.

### 1. Create Environment Files

Copy the example files and create your actual environment configuration files:

```bash
# Development environment
cp .env.dev.example .env.dev

# Test environment
cp .env.test.example .env.test

# Production environment
cp .env.prod.example .env.prod
```

### 2. Configure Firebase

For each environment, you'll need to:

1. Go to Firebase Console (https://console.firebase.google.com)
2. Select your project
3. Go to Project Settings > Service Accounts
4. Click "Generate new private key"
5. Download the JSON file
6. Copy the JSON content to the appropriate `FIREBASE_CONFIG_JSON_*` variable in your environment file

### 3. Update Environment Files

Edit each `.env.*` file and replace the placeholder values:

#### .env.dev
```bash
# Replace with your development Firebase configuration
FIREBASE_CONFIG_JSON_DEV={"type":"service_account","project_id":"your-dev-project",...}

# Update CORS origins for your development setup
CORS_ALLOWED_ORIGINS=https://localhost:5173,http://localhost:5173
```

#### .env.test
```bash
# Replace with your test Firebase configuration
FIREBASE_CONFIG_JSON_TEST={"type":"service_account","project_id":"your-test-project",...}

# Update CORS origins for your test environment
CORS_ALLOWED_ORIGINS=https://test.yourdomain.com
```

#### .env.prod
```bash
# Replace with your production Firebase configuration
FIREBASE_CONFIG_JSON_PROD={"type":"service_account","project_id":"your-prod-project",...}

# Update CORS origins for your production environment
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## VS Code Launch Configurations

The project includes VS Code launch configurations for easy debugging:

### Available Configurations

1. **Revest API - Development**
   - Runs on port 8443
   - Uses development Firebase project
   - Profile: `dev`

2. **Revest API - Test**
   - Runs on port 8444
   - Uses test Firebase project
   - Profile: `test`

3. **Revest API - Production**
   - Runs on port 8080
   - Uses production Firebase project
   - Profile: `prod`

4. **Debug Revest API - Development**
   - Same as development but with debug port 5005 enabled
   - Allows remote debugging

### How to Use

1. Open the project in VS Code
2. Go to Run and Debug (Ctrl+Shift+D)
3. Select the desired configuration from the dropdown
4. Press F5 to start debugging

## Manual Run Commands

If you prefer running from the command line:

```bash
# Development
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Test
./mvnw spring-boot:run -Dspring.profiles.active=test

# Production
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `PORT` | Server port | `8443` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `FIREBASE_CONFIG_JSON_*` | Firebase service account JSON | `{"type":"service_account",...}` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `https://localhost:5173` |
| `LOGGING_LEVEL_ROOT` | Root logging level | `INFO` |
| `LOGGING_LEVEL_COM_BIXAN` | Application logging level | `DEBUG` |

## Testing the Setup

1. Start the application using one of the launch configurations
2. Test the health endpoint: `GET http://localhost:8443/auth/health`
3. Expected response:
   ```json
   {
     "success": true,
     "message": "Authentication service is healthy",
     "timestamp": "2025-10-27T15:30:45.123Z",
     "firebase": "initialized"
   }
   ```

## Troubleshooting

### Firebase Initialization Errors

- **Error**: "Firebase is not properly configured"
- **Solution**: Check that `FIREBASE_CONFIG_JSON_*` environment variable is set correctly

### Port Already in Use

- **Error**: "Port 8443 is already in use"
- **Solution**: Change the `PORT` variable in your environment file or stop the conflicting process

### CORS Errors

- **Error**: CORS policy blocks requests
- **Solution**: Add your frontend URL to `CORS_ALLOWED_ORIGINS` in the environment file

### Authentication Errors

- **Error**: "Invalid Firebase token"
- **Solution**: Ensure your client is using the correct Firebase project configuration

## Security Notes

- **Never commit** `.env.*` files (except `.example` files) to version control
- **Use different Firebase projects** for different environments
- **Rotate Firebase service account keys** regularly
- **Use HTTPS** in production environments
- **Limit CORS origins** to only necessary domains

## Additional Resources

- [Firebase Admin SDK Documentation](https://firebase.google.com/docs/admin/setup)
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [VS Code Java Debugging](https://code.visualstudio.com/docs/java/java-debugging)