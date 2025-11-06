# Revest API Documentation

## Overview
The Revest API is a comprehensive retirement investment analysis service that provides endpoints for market analysis, investment scenarios, retirement planning simulations, and complete user management operations.

**Base URL:** `http://localhost:8080`  
**API Version:** 0.9  
**Content Type:** `application/json`

## Key Features
- **Market Analysis**: Historical market data analysis and performance tracking
- **Investment Scenarios**: Best/worst period analysis for investment strategies  
- **Retirement Planning**: Monte Carlo simulations and retirement projections
- **User Management**: Complete user lifecycle management with authentication
- **Session Management**: In-memory session handling with automatic cleanup
- **Database Operations**: HikariCP connection pooling with MySQL 8+ support
- **Health Monitoring**: Database and system health check endpoints

## Authentication
The API now supports Firebase-based authentication for secure access to protected endpoints. Authentication is optional for public endpoints but required for user-specific operations.

### Authentication Flow
1. Authenticate with Firebase on the client side
2. Obtain Firebase ID token
3. Send token to `/auth/login` endpoint
4. Receive user details and access token
5. Use access token for subsequent authenticated requests

## CORS
All endpoints support Cross-Origin Resource Sharing (CORS) and can be called from web browsers.

---

## Authentication Controller
Provides Firebase-based authentication services for secure API access.

### POST /auth/login
Authenticates a user using Firebase ID token and returns user details with access token.

**Request Body:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyN..."
}
```

**Request Fields:**
- `firebaseToken` (string, required) - Firebase ID token obtained from client-side authentication

**Response (Success):**
```json
{
  "success": true,
  "message": "Authentication successful",
  "userDetails": {
    "uid": "firebase-user-unique-id",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://example.com/profile.jpg",
    "emailVerified": true,
    "phoneNumber": "+1234567890",
    "providerId": "your-firebase-project-id"
  },
  "accessToken": "uuid-based-access-token-firebase-uid"
}
```

**Response (Failure):**
```json
{
  "success": false,
  "message": "Invalid or expired Firebase token",
  "userDetails": null,
  "accessToken": null
}
```

**User Details Fields:**
- `uid` (string) - Firebase user unique identifier
- `email` (string) - User's email address
- `name` (string) - User's display name (may be null)
- `picture` (string) - User's profile picture URL (may be null)
- `emailVerified` (boolean) - Whether email is verified
- `phoneNumber` (string) - User's phone number (may be null)
- `providerId` (string) - Firebase project identifier

### POST /auth/validate
Validates if a Firebase ID token is still valid and not expired.

**Request Body:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyN..."
}
```

**Response (Valid Token):**
```json
{
  "success": true,
  "message": "Token is valid"
}
```

**Response (Invalid Token):**
```json
{
  "success": false,
  "message": "Token is invalid or expired"
}
```

### GET /auth/health
Health check endpoint for the authentication service.

**Response:**
```json
{
  "success": true,
  "message": "Authentication service is healthy",
  "timestamp": "2025-10-27T15:30:45.123Z",
  "firebase": "initialized"
}
```

---

## User Management Controller
Provides comprehensive user management operations including authentication, profile management, and session handling.

### POST /api/users
Creates a new user account in the system.

**Request Body:**
```json
{
  "uid": "firebase-user-unique-id",
  "email": "user@example.com",
  "displayName": "John Doe",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "profilePictureUrl": "https://example.com/profile.jpg",
  "emailVerified": true,
  "provider": "firebase"
}
```

**Request Fields:**
- `uid` (string, required) - Unique user identifier
- `email` (string, required) - User's email address
- `displayName` (string, optional) - User's display name
- `firstName` (string, optional) - User's first name
- `lastName` (string, optional) - User's last name
- `phoneNumber` (string, optional) - User's phone number
- `profilePictureUrl` (string, optional) - URL to profile picture
- `emailVerified` (boolean, optional) - Email verification status
- `provider` (string, optional) - Authentication provider

**Response (Success):**
```json
{
  "success": true,
  "user": {
    "uid": "firebase-user-unique-id",
    "email": "user@example.com",
    "displayName": "John Doe",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profilePictureUrl": "https://example.com/profile.jpg",
    "emailVerified": true,
    "active": true,
    "createdAt": "2025-10-30T10:30:45.123Z",
    "updatedAt": "2025-10-30T10:30:45.123Z",
    "lastLoginAt": null,
    "provider": "firebase"
  }
}
```

### POST /api/users/login
Authenticates a user and creates an active session.

**Request Body:**
```json
{
  "uid": "firebase-user-unique-id"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "uid": "firebase-user-unique-id",
    "email": "user@example.com",
    "displayName": "John Doe",
    "lastLoginAt": "2025-10-30T10:35:12.456Z"
  },
  "sessionInfo": {
    "isLoggedIn": true,
    "loginTime": "2025-10-30T10:35:12.456Z",
    "sessionDuration": 0
  }
}
```

### POST /api/users/logout
Logs out a user and removes their active session.

**Request Body:**
```json
{
  "uid": "firebase-user-unique-id"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

### GET /api/users/{uid}
Retrieves user information by unique identifier.

**Parameters:**
- `uid` (path, string) - User's unique identifier

**Response:**
```json
{
  "success": true,
  "user": {
    "uid": "firebase-user-unique-id",
    "email": "user@example.com",
    "displayName": "John Doe",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profilePictureUrl": "https://example.com/profile.jpg",
    "emailVerified": true,
    "active": true,
    "createdAt": "2025-10-30T10:30:45.123Z",
    "updatedAt": "2025-10-30T10:30:45.123Z",
    "lastLoginAt": "2025-10-30T10:35:12.456Z",
    "provider": "firebase"
  },
  "sessionInfo": {
    "isLoggedIn": true,
    "loginTime": "2025-10-30T10:35:12.456Z",
    "sessionDuration": 25
  }
}
```

### GET /api/users/email/{email}
Retrieves user information by email address.

**Parameters:**
- `email` (path, string) - User's email address

**Response:** Same format as GET /api/users/{uid}

### PUT /api/users/{uid}
Updates an existing user's information.

**Parameters:**
- `uid` (path, string) - User's unique identifier

**Request Body:** Same format as POST /api/users (all fields optional except uid)

**Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "user": {
    /* updated user object */
  }
}
```

### DELETE /api/users/{uid}
Deactivates a user account (soft delete).

**Parameters:**
- `uid` (path, string) - User's unique identifier

**Response:**
```json
{
  "success": true,
  "message": "User deactivated successfully"
}
```

### GET /api/users
Retrieves all active users in the system.

**Response:**
```json
{
  "success": true,
  "users": [
    {
      /* user object */
    }
  ],
  "count": 5
}
```

### GET /api/users/recent/{days}
Retrieves users who have logged in within the specified number of days.

**Parameters:**
- `days` (path, integer) - Number of days to look back

**Response:**
```json
{
  "success": true,
  "users": [
    {
      /* user object */
    }
  ],
  "count": 3,
  "days": 7
}
```

### GET /api/users/{uid}/session
Retrieves session information for a specific user.

**Parameters:**
- `uid` (path, string) - User's unique identifier

**Response:**
```json
{
  "success": true,
  "sessionInfo": {
    "isLoggedIn": true,
    "loginTime": "2025-10-30T10:35:12.456Z",
    "sessionDuration": 45
  },
  "user": {
    "uid": "firebase-user-unique-id",
    "email": "user@example.com",
    "displayName": "John Doe"
  }
}
```

### GET /api/users/stats
Retrieves user statistics and system metrics.

**Response:**
```json
{
  "success": true,
  "stats": {
    "totalUsers": 150,
    "activeUsers": 145,
    "inactiveUsers": 5,
    "activeSessionsCount": 23,
    "usersLoggedInToday": 45,
    "usersLoggedInThisWeek": 89,
    "averageSessionDuration": 67
  }
}
```

### POST /api/users/cleanup-sessions
Manually triggers cleanup of expired user sessions.

**Response:**
```json
{
  "success": true,
  "message": "Session cleanup completed",
  "removedSessions": 5
}
```

---

## Database Health Controller
Provides database connectivity and health monitoring endpoints.

### GET /api/db/health
Comprehensive database health check including connection pool status.

**Response:**
```json
{
  "status": "UP",
  "database": "MySQL",
  "connectionPool": {
    "active": 2,
    "idle": 8,
    "max": 10,
    "min": 5
  },
  "lastChecked": "2025-10-30T10:45:23.789Z",
  "responseTime": 15
}
```

**Response Fields:**
- `status` (string) - "UP" or "DOWN"
- `database` (string) - Database type and version
- `connectionPool` (object) - HikariCP connection pool metrics
- `lastChecked` (string) - Timestamp of health check
- `responseTime` (integer) - Response time in milliseconds

### GET /api/db/test
Tests database connectivity and basic operations.

**Response:**
```json
{
  "success": true,
  "message": "Database connectivity test passed",
  "tests": {
    "connection": "PASS",
    "query": "PASS",
    "userTable": "EXISTS"
  },
  "timestamp": "2025-10-30T10:45:30.123Z"
}
```

---

## Test Controller
Provides testing endpoints for development and debugging.

### GET /api/test/user-flow
Tests the complete user workflow including creation, login, and session management.

**Response:**
```json
{
  "success": true,
  "message": "User workflow test completed successfully",
  "steps": {
    "createUser": "PASS",
    "loginUser": "PASS",
    "getUserInfo": "PASS",
    "sessionInfo": "PASS",
    "logoutUser": "PASS",
    "cleanup": "PASS"
  },
  "testUser": {
    "uid": "test-user-12345",
    "email": "test@example.com",
    "createdAt": "2025-10-30T10:46:15.234Z"
  },
  "duration": 234
}
```

### GET /api/test/database
Tests database operations and data integrity.

**Response:**
```json
{
  "success": true,
  "message": "Database test completed successfully",
  "tests": {
    "tableExists": "PASS",
    "createOperation": "PASS",
    "readOperation": "PASS",
    "updateOperation": "PASS",
    "deleteOperation": "PASS"
  },
  "duration": 156
}
```

---

### GET /account/list
Returns a simple test response.

**Response:**
```json
{
  "result": "Hello World"
}
```

---

## Scenario Controller
Provides market analysis and investment scenario endpoints.

### GET /account/best/{num}/{market}
Get the best performing years for a specific market.

**Parameters:**
- `num` (path, integer) - Number of best years to return
- `market` (path, string) - Market index identifier

**Supported Markets:**
- `sp500`, `sp`, `s&p`, `s&p500` - S&P 500 (default)
- `russel3000`, `russel3k` - Russell 3000
- `russel2000`, `russel2k` - Russell 2000  
- `dow`, `dowjones` - Dow Jones

**Example Request:**
```
GET /account/best/5/sp500
```

**Response:**
```json
{
  "market": "SP500",
  "best": [
    {
      "Year": 2021,
      "Return": 26.89,
      "endAmount": 126890.00
    }
  ]
}
```

### GET /account/worst/{num}/{market}
Get the worst performing years for a specific market.

**Parameters:**
- `num` (path, integer) - Number of worst years to return
- `market` (path, string) - Market index identifier (same options as above)

**Example Request:**
```
GET /account/worst/3/dow
```

**Response:**
```json
{
  "market": "DOWJONES",
  "worst": [
    {
      "Year": 2008,
      "Return": -33.84,
      "endAmount": 66160.00
    }
  ]
}
```

### POST /account/bestperiod/{market}
Find the best investment period for a given investment strategy.

**Parameters:**
- `market` (path, string) - Market index identifier

**Request Body (Investment object):**
```json
{
  "period": "15",
  "currency": "USD", 
  "startAmount": "100000.00",
  "additionalAmount": "5000.00",
  "periodType": "Month",
  "startDepositPeriod": "0",
  "endDepositPeriod": "10"
}
```

**Investment Object Fields:**
- `period` (string) - Investment period length
- `currency` (string) - Currency code (e.g., "USD")
- `startAmount` (string) - Initial investment amount
- `additionalAmount` (string) - Additional periodic contribution
- `periodType` (string) - Period type (e.g., "Month", "Year")
- `startDepositPeriod` (string) - When to start additional deposits
- `endDepositPeriod` (string) - When to end additional deposits

**Response:**
```json
{
  "market": "SP500",
  "investment": { /* echoed investment object */ },
  "best_period": [
    {
      "Year": 1995,
      "Return": 37.58,
      "endAmount": 275800.00
    }
  ]
}
```

### POST /account/worstperiod/{market}
Find the worst investment period for a given investment strategy.

**Parameters:**
- `market` (path, string) - Market index identifier

**Request Body:** Same as `/bestperiod/{market}`

**Response:**
```json
{
  "market": "SP500", 
  "investment": { /* echoed investment object */ },
  "worst_period": [
    {
      "Year": 2000,
      "Return": -9.10,
      "endAmount": 90900.00
    }
  ]
}
```

---

## What-If Controller
Provides retirement planning and simulation endpoints.

### POST /whatif/scenario
Run a retirement scenario projection based on household data.

**Request Body (HouseholdRetirement object):**
```json
{
  "persons": [
    {
      "name": "John Doe",
      "dob": "29836800",
      "retireAge": "62", 
      "retireWithdraw": "100000.00"
    },
    {
      "name": "Jane Doe",
      "dob": "84870000",
      "retireAge": "62",
      "retireWithdraw": "50000.00"
    }
  ],
  "accounts": [
    {
      "name": "ira1",
      "owner": "John Doe", 
      "balance": "563045.76",
      "retirement": "true"
    },
    {
      "name": "brokerage1",
      "balance": "123238.53",
      "retirement": "false"
    }
  ],
  "rate": {
    "inflation": "3.5",
    "earlyYear": "7.5", 
    "midYear": "5.5",
    "endYear": "4.5"
  },
  "withdrawals": [
    {
      "year": "2022",
      "amount": "60000.00",
      "name": "College year 1 for daughter 1"
    }
  ]
}
```

**HouseholdRetirement Object Fields:**

**Persons Array:**
- `name` (string) - Person's name (placeholder)
- `dob` (string) - Date of birth in UTC timestamp
- `retireAge` (string) - Retirement age
- `retireWithdraw` (string) - First year retirement withdrawal amount

**Accounts Array:**
- `name` (string) - Account name (placeholder)
- `owner` (string) - Account owner name (optional)
- `balance` (string) - Current account balance in dollars
- `retirement` (string) - "true" if IRA/401k, "false" for taxable accounts

**Rate Object:**
- `inflation` (string) - Annual inflation rate percentage
- `earlyYear` (string) - Expected return rate for early retirement years
- `midYear` (string) - Expected return rate for middle retirement years  
- `endYear` (string) - Expected return rate for late retirement years

**Withdrawals Array (optional):**
- `year` (string) - Year of withdrawal
- `amount` (string) - Withdrawal amount
- `name` (string) - Description/purpose of withdrawal

**Response:**
```json
{
  "meta": {
    "lastFundedYear": 2045,
    "lastFundedAge": 85,
    "finalBalance": "0.00",
    "maxAge": 100
  },
  "years": [
    {
      "year": 2025,
      "persons": [
        {
          "name": "John Doe",
          "age": 65
        }
      ],
      "summary": {
        "beginBalance": 563045.76,
        "endBalance": 548230.45,
        "targetWithdrawal": 100000.00,
        "actualWithdrawal": 100000.00,
        "deposit": 0.00,
        "rate": 0.075
      },
      "accounts": [
        {
          "name": "ira1",
          "beginBalance": "563045.76",
          "withdrawal": "100000.00", 
          "deposit": "0.00",
          "endBalance": "548230.45"
        }
      ]
    }
  ]
}
```

### POST /whatif/simulate
Run Monte Carlo simulation for retirement scenario.

**Request Body:** Same as `/whatif/scenario`

**Response:**
```json
{
  "meta": {
    "run": 1000,
    "market": "SP500",
    "marketAverage": 0.098,
    "success": 847,
    "maxAge": 100
  },
  "runs": [
    {
      "index": 1,
      "fundedYear": 25,
      "endBalance": "125430.50",
      "averageRate": 0.087
    }
  ]
}
```

**Simulation Response Fields:**

**Meta Object:**
- `run` (integer) - Total number of simulation runs
- `market` (string) - Market index used for simulation
- `marketAverage` (number) - Historical market average return
- `success` (integer) - Number of successful runs (money lasted)
- `maxAge` (integer) - Maximum age considered in simulation

**Runs Array:**
- `index` (integer) - Simulation run number
- `fundedYear` (integer) - Last year with remaining funds
- `endBalance` (string) - Final account balance
- `averageRate` (number) - Average return rate for this simulation

---

## Error Handling

All endpoints may return the following error responses:

**Authentication Errors (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Authentication required",
  "timestamp": "2025-10-27T15:30:45.123+00:00"
}
```

**Firebase Authentication Errors (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Firebase token verification failed: TOKEN_EXPIRED",
  "timestamp": "2025-10-27T15:30:45.123+00:00"
}
```

**Firebase Configuration Errors (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "Firebase is not properly configured",
  "timestamp": "2025-10-27T15:30:45.123+00:00"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2025-10-23T15:30:45.123+00:00",
  "status": 500,
  "error": "Internal Server Error", 
  "message": "An error occurred processing your request",
  "path": "/account/best/5/sp500"
}
```

**400 Bad Request:**
```json
{
  "timestamp": "2025-10-23T15:30:45.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters",
  "path": "/whatif/scenario"
}
```

---

## Usage Examples

### User Management Examples

#### Create a New User
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "uid": "firebase-user-123",
    "email": "john.doe@example.com",
    "displayName": "John Doe",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "emailVerified": true,
    "provider": "firebase"
  }'
```

#### User Login
```bash
curl -X POST "http://localhost:8080/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{
    "uid": "firebase-user-123"
  }'
```

#### Get User Information
```bash
curl -X GET "http://localhost:8080/api/users/firebase-user-123"
```

#### Update User
```bash
curl -X PUT "http://localhost:8080/api/users/firebase-user-123" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jonathan",
    "phoneNumber": "+1987654321"
  }'
```

#### Get User Session Info
```bash
curl -X GET "http://localhost:8080/api/users/firebase-user-123/session"
```

#### Get All Active Users
```bash
curl -X GET "http://localhost:8080/api/users"
```

#### Get Recent Users (last 7 days)
```bash
curl -X GET "http://localhost:8080/api/users/recent/7"
```

#### User Logout
```bash
curl -X POST "http://localhost:8080/api/users/logout" \
  -H "Content-Type: application/json" \
  -d '{
    "uid": "firebase-user-123"
  }'
```

### Database Health Examples

#### Check Database Health
```bash
curl -X GET "http://localhost:8080/api/db/health"
```

#### Test Database Connectivity
```bash
curl -X GET "http://localhost:8080/api/db/test"
```

### Test Endpoints Examples

#### Test Complete User Workflow
```bash
curl -X GET "http://localhost:8080/api/test/user-flow"
```

#### Test Database Operations
```bash
curl -X GET "http://localhost:8080/api/test/database"
```

### Authentication Examples

### Authenticate with Firebase Token
```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyN..."
  }'
```

### Validate Firebase Token
```bash
curl -X POST "http://localhost:8080/auth/validate" \
  -H "Content-Type: application/json" \
  -d '{
    "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyN..."
  }'
```

### Check Authentication Service Health
```bash
curl -X GET "http://localhost:8080/auth/health"
```

### Get Best 10 Years for S&P 500
```bash
curl -X GET "http://localhost:8080/account/best/10/sp500"
```

### Run Investment Analysis
```bash
curl -X POST "http://localhost:8080/account/bestperiod/sp500" \
  -H "Content-Type: application/json" \
  -d '{
    "period": "20",
    "currency": "USD",
    "startAmount": "50000.00", 
    "additionalAmount": "1000.00",
    "periodType": "Month",
    "startDepositPeriod": "0",
    "endDepositPeriod": "15"
  }'
```

### Run Retirement Scenario
```bash
curl -X POST "http://localhost:8080/whatif/scenario" \
  -H "Content-Type: application/json" \
  -d '{
    "persons": [
      {
        "name": "John Smith",
        "dob": "315532800",
        "retireAge": "65",
        "retireWithdraw": "80000.00"
      }
    ],
    "accounts": [
      {
        "name": "401k",
        "balance": "750000.00",
        "retirement": "true"
      }
    ],
    "rate": {
      "inflation": "3.0",
      "earlyYear": "7.0",
      "midYear": "5.0", 
      "endYear": "4.0"
    },
    "withdrawals": []
  }'
```

---

## Notes

- **Firebase Configuration**: Set the `FIREBASE_CONFIG_JSON` environment variable with your Firebase service account JSON
- **Authentication**: Firebase authentication is optional for public endpoints but required for user-specific operations
- **Token Security**: Firebase ID tokens should be transmitted over HTTPS in production
- **Token Lifespan**: Firebase ID tokens have a default lifespan of 1 hour
- All monetary amounts are represented as strings to preserve precision
- Date of birth (`dob`) should be provided as UTC timestamp
- Market identifiers are case-insensitive
- The API uses historical market data for calculations and projections
- Simulation results are probabilistic and should not be considered financial advice

## Environment Setup

### Database Configuration
The application uses MySQL 8+ with HikariCP connection pooling. Configure the following environment variables:

```bash
# Database connection settings
export DB_HOST=43.231.232.167
export DB_PORT=3306
export DB_NAME=bixan_revest_dev
export DB_USERNAME=bixan_dbuser_dev
export DB_PASSWORD=your-database-password

# Connection pool settings (optional)
export DB_POOL_SIZE=10
export DB_POOL_TIMEOUT=30000
export DB_CONNECTION_TIMEOUT=20000
```

Or configure in `application-{profile}.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://${DB_HOST:43.231.232.167}:${DB_PORT:3306}/${DB_NAME:bixan_revest_dev}
spring.datasource.username=${DB_USERNAME:bixan_dbuser_dev}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=${DB_POOL_SIZE:10}
spring.datasource.hikari.connection-timeout=${DB_CONNECTION_TIMEOUT:20000}
spring.datasource.hikari.idle-timeout=${DB_POOL_TIMEOUT:600000}
```

### Firebase Configuration
```bash
# Set Firebase service account JSON as environment variable
export FIREBASE_CONFIG_JSON='{
  "type": "service_account",
  "project_id": "your-firebase-project-id",
  "private_key_id": "key-id",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com",
  "client_id": "client-id",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xxxxx%40your-project.iam.gserviceaccount.com"
}'
```

### Running the Application
```bash
# Development environment
mvn spring-boot:run -Dspring.profiles.active=dev

# Test environment  
mvn spring-boot:run -Dspring.profiles.active=test

# Production environment
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

## Account Controller
Provides REST endpoints for managing financial accounts. All endpoints require valid session authentication.

### POST /api/accounts
Create a new account for the authenticated user.

**Authentication:** Required (Session cookie)

**Request Body:**
```json
{
  "name": "Chase Checking",
  "type": "CASH_EQUIVALENT",
  "balanceCents": 523456,
  "provider": "Chase Bank",
  "accountId": "XXXX-XXXX-1234",
  "creationType": "MANUAL"
}
```

**Request Fields:**
- `name` (string, required) - Account display name
- `type` (enum, required) - Account type: `CASH_EQUIVALENT`, `TAXABLE`, `TAX_DEFERRED`, `TAX_FREE`
- `balanceCents` (integer, optional) - Balance in cents, defaults to 0
- `provider` (string, optional) - Financial institution name
- `accountId` (string, optional) - Provider-specific account identifier
- `creationType` (enum, required) - How account was created: `MANUAL`, `PULLED`

**Response (Success - 201 Created):**
```json
{
  "id": 1,
  "userId": 123,
  "name": "Chase Checking",
  "type": "CASH_EQUIVALENT",
  "balanceCents": 523456,
  "balanceDollars": 5234.56,
  "provider": "Chase Bank",
  "accountId": "XXXX-XXXX-1234",
  "creationType": "MANUAL",
  "created": "2025-11-04T20:00:00",
  "updated": "2025-11-04T20:00:00"
}
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 500 Internal Server Error - Server error

---

### GET /api/accounts
Get all accounts for the authenticated user. Optionally filter by account type.

**Authentication:** Required (Session cookie)

**Query Parameters:**
- `type` (enum, optional) - Filter by account type: `CASH_EQUIVALENT`, `TAXABLE`, `TAX_DEFERRED`, `TAX_FREE`

**Example Request:**
```
GET /api/accounts
GET /api/accounts?type=TAXABLE
```

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "userId": 123,
    "name": "Chase Checking",
    "type": "CASH_EQUIVALENT",
    "balanceCents": 523456,
    "balanceDollars": 5234.56,
    "provider": "Chase Bank",
    "accountId": "XXXX-XXXX-1234",
    "creationType": "MANUAL",
    "created": "2025-11-04T20:00:00",
    "updated": "2025-11-04T20:00:00"
  },
  {
    "id": 2,
    "userId": 123,
    "name": "Vanguard 401k",
    "type": "TAX_DEFERRED",
    "balanceCents": 4567890,
    "balanceDollars": 45678.90,
    "provider": "Vanguard",
    "accountId": "VANG-401K-5678",
    "creationType": "PULLED",
    "created": "2025-11-04T19:00:00",
    "updated": "2025-11-04T19:30:00"
  }
]
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 500 Internal Server Error - Server error

---

### GET /api/accounts/{accountId}
Get a specific account by ID.

**Authentication:** Required (Session cookie)

**Path Parameters:**
- `accountId` (long, required) - The account ID

**Example Request:**
```
GET /api/accounts/1
```

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "userId": 123,
  "name": "Chase Checking",
  "type": "CASH_EQUIVALENT",
  "balanceCents": 523456,
  "balanceDollars": 5234.56,
  "provider": "Chase Bank",
  "accountId": "XXXX-XXXX-1234",
  "creationType": "MANUAL",
  "created": "2025-11-04T20:00:00",
  "updated": "2025-11-04T20:00:00"
}
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 403 Forbidden - Account does not belong to user
- 404 Not Found - Account not found
- 500 Internal Server Error - Server error

---

### PUT /api/accounts/{accountId}
Update an existing account. All fields are optional; only provided fields will be updated.

**Authentication:** Required (Session cookie)

**Path Parameters:**
- `accountId` (long, required) - The account ID

**Request Body:**
```json
{
  "name": "Chase Premium Checking",
  "type": "CASH_EQUIVALENT",
  "balanceCents": 600000,
  "provider": "JPMorgan Chase",
  "accountId": "XXXX-XXXX-1234"
}
```

**Request Fields (all optional):**
- `name` (string) - Updated account name
- `type` (enum) - Updated account type
- `balanceCents` (integer) - Updated balance in cents
- `provider` (string) - Updated provider name
- `accountId` (string) - Updated provider account ID

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "userId": 123,
  "name": "Chase Premium Checking",
  "type": "CASH_EQUIVALENT",
  "balanceCents": 600000,
  "balanceDollars": 6000.00,
  "provider": "JPMorgan Chase",
  "accountId": "XXXX-XXXX-1234",
  "creationType": "MANUAL",
  "created": "2025-11-04T20:00:00",
  "updated": "2025-11-04T21:00:00"
}
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 403 Forbidden - Account does not belong to user
- 404 Not Found - Account not found
- 500 Internal Server Error - Server error

---

### DELETE /api/accounts/{accountId}
Soft delete an account. The account will be marked as deleted but not removed from the database.

**Authentication:** Required (Session cookie)

**Path Parameters:**
- `accountId` (long, required) - The account ID

**Example Request:**
```
DELETE /api/accounts/1
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 403 Forbidden - Account does not belong to user
- 404 Not Found - Account not found or already deleted
- 500 Internal Server Error - Server error

---

### GET /api/accounts/count
Get the count of active accounts for the authenticated user.

**Authentication:** Required (Session cookie)

**Example Request:**
```
GET /api/accounts/count
```

**Response (Success - 200 OK):**
```json
{
  "count": 5
}
```

**Error Responses:**
- 401 Unauthorized - No valid session
- 500 Internal Server Error - Server error

---

## Account Types

### AccountType Enum
- `CASH_EQUIVALENT` - Cash, checking accounts, savings accounts, money market
- `TAXABLE` - Regular investment/brokerage accounts
- `TAX_DEFERRED` - 401(k), Traditional IRA, 403(b), etc.
- `TAX_FREE` - Roth IRA, Roth 401(k), HSA, etc.

### CreationType Enum
- `MANUAL` - Account created manually by user
- `PULLED` - Account data pulled from financial institution via API

---

## Database Schema

### accounts Table
```sql
CREATE TABLE accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  balance_cents INT NOT NULL DEFAULT 0,
  provider VARCHAR(255),
  account_id VARCHAR(255),
  creation_type VARCHAR(50) NOT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_deleted (deleted),
  INDEX idx_created (created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

