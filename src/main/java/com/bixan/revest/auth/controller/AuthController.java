package com.bixan.revest.auth.controller;

import com.bixan.revest.auth.model.AuthRequest;
import com.bixan.revest.auth.model.AuthResponse;
import com.bixan.revest.auth.model.UserDetails;
import com.bixan.revest.auth.service.AuthService;
import com.bixan.revest.auth.service.SessionService;
import com.bixan.revest.entities.User;
import com.bixan.revest.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" }, allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
        RequestMethod.OPTIONS }, allowCredentials = "true", maxAge = 3600)
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    /**
     * Login endpoint that accepts Firebase token and returns user details with
     * session cookie
     * 
     * @param authRequest Request containing Firebase ID token
     * @param response    HTTP response to set session cookie
     * @return AuthResponse with user details and access token
     */
    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        try {
            log.info("Login attempt received");

            // Check if authentication service is available
            if (!authService.isAuthenticationAvailable()) {
                log.warn("Login attempt but Firebase is not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(AuthResponse.failure("Authentication service is not available. Please contact support."));
            }

            if (authRequest.getFirebaseToken() == null || authRequest.getFirebaseToken().trim().isEmpty()) {
                log.warn("Login attempt with empty token");
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("Firebase token is required"));
            }

            // Verify Firebase token and get basic user details
            UserDetails firebaseUserDetails = authService.verifyTokenAndGetUserDetails(authRequest.getFirebaseToken());

            // Fetch complete user information from database
            Optional<User> userOpt = userService.getUserInfo(firebaseUserDetails.getUid());
            UserDetails completeUserDetails;

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Populate UserDetails with complete information from database
                completeUserDetails = UserDetails.builder()
                        .userId(user.getId())
                        .uid(user.getUid())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .name(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .profilePictureUrl(user.getProfilePictureUrl())
                        .emailVerified(user.isEmailVerified())
                        .providerId(firebaseUserDetails.getProviderId())
                        .state(user.getState())
                        .country(user.getCountry())
                        .currency(user.getCurrency())
                        .createdAt(user.getCreatedAt())
                        .build();

                log.info("User found in database: {}", user.getEmail());
            } else {
                // User not in database yet, use Firebase details
                log.warn("User not found in database, using Firebase details only: {}", firebaseUserDetails.getUid());
                completeUserDetails = firebaseUserDetails;
            }

            // Generate access token
            String accessToken = authService.generateAccessToken(completeUserDetails);

            // Create session with JWT cookie
            sessionService.createSession(response, completeUserDetails, authRequest.getFirebaseToken());

            log.info("Login successful for user: {}", completeUserDetails.getEmail());

            return ResponseEntity.ok(AuthResponse.success(completeUserDetails, accessToken));

        } catch (FirebaseAuthException e) {
            log.warn("Firebase authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.failure("Firebase token verification failed: " + e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.failure("Internal server error"));
        }
    }

    /**
     * Validate token endpoint to check if a Firebase token is still valid
     * 
     * @param authRequest Request containing Firebase ID token
     * @return Simple validation response
     */
    @PostMapping(value = "/validate", produces = "application/json", consumes = "application/json")
    public ResponseEntity<AuthResponse> validateToken(@RequestBody AuthRequest authRequest) {
        try {
            // Check if authentication service is available
            if (!authService.isAuthenticationAvailable()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(AuthResponse.failure("Authentication service is not available"));
            }

            if (authRequest.getFirebaseToken() == null || authRequest.getFirebaseToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("Firebase token is required"));
            }

            boolean isValid = authService.isTokenValid(authRequest.getFirebaseToken());

            if (isValid) {
                return ResponseEntity.ok(AuthResponse.success(null, null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.failure("Invalid or expired token"));
            }

        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.failure("Internal server error"));
        }
    }

    /**
     * Health check endpoint for authentication service
     * 
     * @return Simple health status including Firebase availability
     */
    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<String> health() {
        boolean authAvailable = authService.isAuthenticationAvailable();
        String status = authAvailable ? "healthy" : "limited";
        String message = authAvailable ? "Authentication service is fully operational"
                : "Authentication service is not available - Firebase not configured";

        return ResponseEntity.ok(String.format(
                "{\"status\":\"%s\",\"service\":\"auth\",\"firebase_available\":%s,\"message\":\"%s\"}",
                status, authAvailable, message));
    }

    /**
     * Validate session cookie endpoint
     * 
     * @param request  HTTP request containing session cookie
     * @param response HTTP response for potential cookie updates
     * @return Validation result with user details if valid
     */
    @GetMapping(value = "/validate-session", produces = "application/json")
    public ResponseEntity<AuthResponse> validateSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Session validation request received");

            // Validate session cookie
            SessionService.SessionValidationResult validationResult = sessionService.validateSession(request, response);

            if (validationResult.isValid()) {
                log.info("Valid session for user: {}", validationResult.getUserId());

                // Get user details from Firebase token
                UserDetails userDetails = authService.verifyTokenAndGetUserDetails(validationResult.getFirebaseToken());

                // Generate a fresh access token for this session
                String accessToken = authService.generateAccessToken(userDetails);
                return ResponseEntity.ok(AuthResponse.success(userDetails, accessToken));
            } else {
                log.info("No valid session found: {}", validationResult.getErrorMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.failure("No valid session"));
            }

        } catch (Exception e) {
            log.error("Error validating session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.failure("Error validating session"));
        }
    }

    /**
     * Logout endpoint that clears the session cookie
     * 
     * @param response HTTP response to clear session cookie
     * @return Simple success response
     */
    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        try {
            log.info("Logout request received");

            // Clear the session cookie
            sessionService.clearSession(response);

            log.info("Logout successful");
            return ResponseEntity.ok("{\"success\":true,\"message\":\"Logged out successfully\"}");

        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\":false,\"message\":\"Error during logout\"}");
        }
    }
}