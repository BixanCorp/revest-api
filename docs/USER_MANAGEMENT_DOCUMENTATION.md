# User Management System Documentation

## Overview

This document describes the comprehensive User Management System implemented for the Revest API. The system provides complete user lifecycle management including creation, authentication, session management, and database operations.

## Architecture

### Components

1. **Entity Layer**
   - `User` - Core user entity with all user attributes
   - Located in: `com.bixan.revest.entities`

2. **Data Access Layer (DAO)**
   - `UserDao` - Database operations for user management
   - Located in: `com.bixan.revest.dao`
   - Extends `AbstractBaseDao` for common functionality

3. **Service Layer**
   - `UserService` - Business logic and session management
   - Located in: `com.bixan.revest.service`
   - Handles in-memory user sessions and database coordination

4. **Controller Layer**
   - `UserController` - REST API endpoints for user operations
   - `UserTestController` - Test endpoints for system validation
   - Located in: `com.bixan.revest.controller`

5. **DTO Layer**
   - `CreateUserRequest` - Data transfer object for user creation
   - `UserResponse` - Sanitized user response object
   - Located in: `com.bixan.revest.dto`

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(255) UNIQUE NOT NULL,           -- External user identifier (Firebase UID)
    email VARCHAR(255) UNIQUE NOT NULL,         -- User email address
    display_name VARCHAR(255),                  -- Display name
    first_name VARCHAR(255),                    -- First name
    last_name VARCHAR(255),                     -- Last name
    phone_number VARCHAR(20),                   -- Phone number
    profile_picture_url TEXT,                   -- Profile picture URL
    email_verified BOOLEAN DEFAULT FALSE,       -- Email verification status
    active BOOLEAN DEFAULT TRUE,                -- User account status
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,        -- Account creation time
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- Last update time
    last_login_at TIMESTAMP NULL,               -- Last login timestamp
    provider VARCHAR(50),                       -- Authentication provider
    provider_data TEXT,                         -- Provider-specific data (JSON)
    
    -- Indexes for performance
    INDEX idx_uid (uid),
    INDEX idx_email (email),
    INDEX idx_active (active),
    INDEX idx_last_login (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create a new user |
| POST | `/api/users/login` | User login |
| POST | `/api/users/logout` | User logout |
| GET | `/api/users/{uid}` | Get user by UID |
| GET | `/api/users/email/{email}` | Get user by email |
| PUT | `/api/users/{uid}` | Update user information |
| DELETE | `/api/users/{uid}` | Deactivate user account |
| GET | `/api/users` | Get all active users |
| GET | `/api/users/recent/{days}` | Get recently active users |
| GET | `/api/users/{uid}/session` | Check user session status |
| GET | `/api/users/stats` | Get user statistics |
| POST | `/api/users/cleanup-sessions` | Clean up expired sessions |

### Test Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/test/user-flow` | Test complete user workflow |
| GET | `/api/test/database` | Test database operations |

## Usage Examples

### 1. Create User
```json
POST /api/users
{
    "uid": "firebase-uid-123",
    "email": "user@example.com",
    "displayName": "John Doe",
    "firstName": "John",
    "lastName": "Doe",
    "provider": "firebase",
    "emailVerified": true
}
```

**Response:**
```json
{
    "success": true,
    "message": "User created successfully",
    "user": {
        "id": 1,
        "uid": "firebase-uid-123",
        "email": "user@example.com",
        "displayName": "John Doe",
        "firstName": "John",
        "lastName": "Doe",
        "emailVerified": true,
        "active": true,
        "createdAt": "2025-10-30T10:30:00",
        "provider": "firebase"
    }
}
```

### 2. User Login
```json
POST /api/users/login
{
    "uid": "firebase-uid-123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Login successful",
    "user": { /* user object */ },
    "sessionInfo": {
        "active": true,
        "uid": "firebase-uid-123",
        "lastLoginAt": "2025-10-30T10:30:00",
        "sessionExpiry": "2025-10-30T12:30:00",
        "minutesUntilExpiry": 120
    }
}
```

### 3. Get User Information
```json
GET /api/users/firebase-uid-123

Response:
{
    "success": true,
    "user": { /* user object */ },
    "sessionInfo": { /* session information */ }
}
```

### 4. Update User
```json
PUT /api/users/firebase-uid-123
{
    "displayName": "John Smith",
    "phoneNumber": "+1234567890"
}
```

### 5. User Logout
```json
POST /api/users/logout
{
    "uid": "firebase-uid-123"
}
```

## Features

### 1. User Creation
- Creates new user with validation
- Prevents duplicate UIDs and emails
- Automatically sets creation timestamps
- Adds user to active session immediately

### 2. User Authentication
- Login updates `last_login_at` timestamp
- Loads user into memory for session management
- Validates user existence and active status

### 3. Session Management
- **In-Memory Storage**: Active users stored in memory for fast access
- **Session Timeout**: 2-hour configurable timeout
- **Automatic Cleanup**: Expired sessions automatically removed
- **Session Validation**: Check if user session is valid

### 4. Database Operations
- **CRUD Operations**: Complete Create, Read, Update, Delete functionality
- **Soft Delete**: Users are deactivated, not permanently deleted
- **Optimized Queries**: Indexed columns for performance
- **Connection Pooling**: Uses HikariCP for efficient database connections

### 5. Data Management
- **Entity Mapping**: Automatic mapping between database and Java objects
- **Data Sanitization**: Sensitive data excluded from API responses
- **Timestamp Management**: Automatic creation and update timestamps
- **Data Validation**: Required field validation

## Security Features

### 1. Data Protection
- Provider data (potentially sensitive) excluded from API responses
- Input validation on all endpoints
- SQL injection prevention using parameterized queries

### 2. Session Security
- Session timeout enforcement
- Automatic session cleanup
- Session validation on each request

### 3. Database Security
- Connection pooling with timeout configurations
- Prepared statements for all queries
- Index-based optimized queries

## Performance Optimizations

### 1. In-Memory Session Storage
- Fast user data access without database queries
- Reduced database load for frequent operations
- Configurable session timeout

### 2. Database Optimizations
- Indexed columns (uid, email, active, last_login_at)
- Connection pooling with HikariCP
- Prepared statement caching
- Optimized MySQL settings

### 3. Query Optimizations
- Named parameter queries
- Batch operations where applicable
- Efficient row mapping

## Configuration

### Database Configuration
Set in `application-dev.properties` or environment variables:
```properties
database.host=43.231.232.167
database.port=3306
database.name=revest
database.username=bixan_dbuser_dev
database.password=UhZ$dZ6Y55kimlt#gbQ9
```

### Session Configuration
In `UserService.java`:
```java
private static final long SESSION_TIMEOUT_MINUTES = 120; // 2 hours
```

## Error Handling

### Common Error Responses

1. **User Not Found**
```json
{
    "success": false,
    "error": "User not found with UID: firebase-uid-123"
}
```

2. **Duplicate User**
```json
{
    "success": false,
    "error": "User with email user@example.com already exists"
}
```

3. **Validation Error**
```json
{
    "success": false,
    "error": "UID is required"
}
```

4. **Session Expired**
```json
{
    "success": true,
    "sessionValid": false,
    "sessionInfo": {
        "active": false
    }
}
```

## Testing

### Automated Testing
Use the test endpoints to validate the system:

1. **Complete User Flow Test**
   ```
   GET /api/test/user-flow
   ```
   - Creates test user
   - Performs login
   - Validates session
   - Updates user
   - Tests logout
   - Cleans up test data

2. **Database Test**
   ```
   GET /api/test/database
   ```
   - Validates table existence
   - Checks user counts
   - Tests session cleanup

### Manual Testing
1. Use Postman or similar tools to test API endpoints
2. Monitor application logs for detailed operation tracking
3. Check database directly for data persistence verification

## Monitoring and Maintenance

### Logging
- All operations are logged with appropriate levels
- User operations include UID and email for tracking
- Error logging includes stack traces for debugging

### Maintenance Tasks
1. **Session Cleanup**: Automatically removes expired sessions
2. **Database Monitoring**: Check connection pool health
3. **User Analytics**: Track user activity and growth

### Health Checks
- Database health check endpoints available
- Session status monitoring
- User statistics endpoints for monitoring

## Integration Points

### Firebase Integration
- Supports Firebase UID as primary identifier
- Compatible with Firebase authentication flows
- Stores Firebase-specific data in provider_data field

### Future Enhancements
- Role-based access control (RBAC)
- User preferences and settings
- Activity logging and audit trails
- Password reset functionality (for email-based auth)
- Two-factor authentication support

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Check database configuration
   - Verify network connectivity
   - Check connection pool settings

2. **Session Issues**
   - Verify session timeout settings
   - Check in-memory storage capacity
   - Monitor session cleanup logs

3. **User Creation Issues**
   - Validate required fields
   - Check for duplicate UIDs/emails
   - Verify database table schema

### Debug Mode
Enable debug logging in `application-dev.properties`:
```properties
logging.level.com.bixan.revest.service.UserService=DEBUG
logging.level.com.bixan.revest.dao.UserDao=DEBUG
```