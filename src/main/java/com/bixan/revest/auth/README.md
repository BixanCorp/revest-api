# Authentication Service

This package provides Firebase-based authentication for the Revest API.

## Features

- Firebase ID token verification
- User details extraction from Firebase
- Simple access token generation
- Token validation
- Comprehensive error handling

## API Endpoints

### POST /auth/login
Authenticates a user using Firebase ID token.

**Request Body:**
```json
{
  "firebaseToken": "your-firebase-id-token"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Authentication successful",
  "userDetails": {
    "uid": "firebase-user-id",
    "email": "user@example.com",
    "name": "User Name",
    "picture": "profile-picture-url",
    "emailVerified": true,
    "phoneNumber": "+1234567890",
    "providerId": "firebase-project-id"
  },
  "accessToken": "generated-access-token"
}
```

### POST /auth/validate
Validates if a Firebase token is still valid.

**Request Body:**
```json
{
  "firebaseToken": "your-firebase-id-token"
}
```

### GET /auth/health
Health check endpoint for the authentication service.

## Configuration

Set the `FIREBASE_CONFIG_JSON` environment variable with your Firebase service account JSON:

```bash
export FIREBASE_CONFIG_JSON='{"type":"service_account","project_id":"your-project",...}'
```

## Usage Example

1. Client authenticates with Firebase and obtains an ID token
2. Client sends the ID token to `/auth/login`
3. Server verifies the token with Firebase
4. Server returns user details and an access token
5. Client uses the access token for subsequent API calls

## Error Handling

The service provides detailed error messages for various authentication failures:
- Invalid token format
- Expired tokens
- Disabled user accounts
- Configuration errors

## Dependencies

- Firebase Admin SDK 9.2.0
- Spring Boot 2.7.5
- Lombok (for data annotations)