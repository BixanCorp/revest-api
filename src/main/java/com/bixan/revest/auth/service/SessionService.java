package com.bixan.revest.auth.service;

import com.bixan.revest.auth.model.UserDetails;
import com.bixan.revest.config.EnvironmentConfig;
import com.bixan.revest.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class SessionService {

    private static final String SESSION_COOKIE_NAME = "revest_session";
    private static final String COOKIE_PATH = "/";
    private static final boolean COOKIE_HTTP_ONLY = true;
    private static final boolean COOKIE_SECURE = true; // Set to true for HTTPS

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnvironmentConfig environmentConfig;

    /**
     * Creates a session for a user after successful authentication
     * 
     * @param response      HTTP response to set the session cookie
     * @param userDetails   User details from Firebase
     * @param firebaseToken The Firebase ID token
     */
    public void createSession(HttpServletResponse response, UserDetails userDetails, String firebaseToken) {
        try {
            // Use Firebase UID as the user ID for the JWT
            String userId = userDetails.getUid();

            // Create JWT session token
            String sessionToken = jwtService.createSessionToken(userId, firebaseToken);

            // Create session cookie
            Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionToken);
            sessionCookie.setPath(COOKIE_PATH);
            sessionCookie.setHttpOnly(COOKIE_HTTP_ONLY);
            sessionCookie.setSecure(COOKIE_SECURE);
            sessionCookie.setMaxAge(environmentConfig.getSessionDurationSeconds());

            response.addCookie(sessionCookie);

            log.info("Session created for user: {}", userId);

        } catch (Exception e) {
            log.error("Error creating session for user: {}", userDetails.getUid(), e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    /**
     * Validates a session from the request and returns user details
     * 
     * @param request  HTTP request containing the session cookie
     * @param response HTTP response to refresh the session cookie
     * @return SessionValidationResult containing user details and validation status
     */
    public SessionValidationResult validateSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract session cookie
            String sessionToken = extractSessionToken(request);
            if (sessionToken == null) {
                log.debug("No session token found in request");
                return SessionValidationResult.invalid("No session token");
            }

            // Parse and validate JWT token
            JwtService.SessionTokenData sessionData;
            try {
                sessionData = jwtService.validateAndParseToken(sessionToken);
            } catch (Exception e) {
                log.debug("Invalid session token: {}", e.getMessage());
                return SessionValidationResult.invalid("Invalid session token");
            }

            // Validate Firebase token is still valid
            boolean isFirebaseTokenValid;
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(sessionData.getFirebaseToken());
                isFirebaseTokenValid = decodedToken != null;
            } catch (FirebaseAuthException e) {
                log.debug("Firebase token validation failed: {}", e.getMessage());
                return SessionValidationResult.invalid("Firebase token invalid");
            }

            if (!isFirebaseTokenValid) {
                return SessionValidationResult.invalid("Firebase token expired");
            }

            // Recover user entity from database
            String userId = sessionData.getUserId();
            try {
                // First check if user exists in the database
                boolean userExists = userService.userExists(userId);
                if (!userExists) {
                    // User doesn't exist in database, this shouldn't happen in normal flow
                    log.warn("User {} has valid session but doesn't exist in database", userId);
                    return SessionValidationResult.invalid("User not found in database");
                }

                log.debug("Session validation successful for user: {}", userId);

                // Refresh the session cookie with new expiration
                refreshSessionCookie(response, userId, sessionData.getFirebaseToken());

                return SessionValidationResult.valid(userId, sessionData.getFirebaseToken());

            } catch (Exception e) {
                log.error("Error recovering user entity for user: {}", userId, e);
                return SessionValidationResult.invalid("Error recovering user data");
            }

        } catch (Exception e) {
            log.error("Error validating session", e);
            return SessionValidationResult.invalid("Session validation error");
        }
    }

    /**
     * Clears the session by removing the session cookie
     * 
     * @param response HTTP response to clear the session cookie
     */
    public void clearSession(HttpServletResponse response) {
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, "");
        sessionCookie.setPath(COOKIE_PATH);
        sessionCookie.setHttpOnly(COOKIE_HTTP_ONLY);
        sessionCookie.setSecure(COOKIE_SECURE);
        sessionCookie.setMaxAge(0); // Expire immediately

        response.addCookie(sessionCookie);
        log.debug("Session cookie cleared");
    }

    /**
     * Extracts the session token from the request cookies
     */
    private String extractSessionToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Refreshes the session cookie with a new expiration time
     */
    private void refreshSessionCookie(HttpServletResponse response, String userId, String firebaseToken) {
        try {
            String newSessionToken = jwtService.refreshSessionToken(userId, firebaseToken);

            Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, newSessionToken);
            sessionCookie.setPath(COOKIE_PATH);
            sessionCookie.setHttpOnly(COOKIE_HTTP_ONLY);
            sessionCookie.setSecure(COOKIE_SECURE);
            sessionCookie.setMaxAge(environmentConfig.getSessionDurationSeconds());

            response.addCookie(sessionCookie);
            log.debug("Session cookie refreshed for user: {}", userId);

        } catch (Exception e) {
            log.error("Error refreshing session cookie for user: {}", userId, e);
            // Don't throw exception, just log the error
        }
    }

    /**
     * Result class for session validation
     */
    public static class SessionValidationResult {
        private final boolean valid;
        private final String userId;
        private final String firebaseToken;
        private final String errorMessage;

        private SessionValidationResult(boolean valid, String userId, String firebaseToken, String errorMessage) {
            this.valid = valid;
            this.userId = userId;
            this.firebaseToken = firebaseToken;
            this.errorMessage = errorMessage;
        }

        public static SessionValidationResult valid(String userId, String firebaseToken) {
            return new SessionValidationResult(true, userId, firebaseToken, null);
        }

        public static SessionValidationResult invalid(String errorMessage) {
            return new SessionValidationResult(false, null, null, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getUserId() {
            return userId;
        }

        public String getFirebaseToken() {
            return firebaseToken;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}