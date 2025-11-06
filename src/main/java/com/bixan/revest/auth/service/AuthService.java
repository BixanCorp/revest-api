package com.bixan.revest.auth.service;

import com.bixan.revest.auth.config.FirebaseConfig;
import com.bixan.revest.auth.model.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private FirebaseConfig firebaseConfig;

    /**
     * Verifies the Firebase ID token and extracts user details
     * 
     * @param idToken Firebase ID token from the client
     * @return UserDetails object containing user information
     * @throws FirebaseAuthException if token verification fails
     */
    public UserDetails verifyTokenAndGetUserDetails(String idToken) throws FirebaseAuthException {
        if (!firebaseConfig.isFirebaseEnabled()) {
            log.warn("Firebase is not configured. Authentication service is not available.");
            throw new RuntimeException("Authentication service is not available. Firebase is not configured.");
        }

        try {
            // Verify the ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            log.info("Successfully verified token for user: {}", uid);

            // Get user record from Firebase
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);

            // Extract user details
            UserDetails userDetails = UserDetails.builder()
                    .uid(uid)
                    .email(userRecord.getEmail())
                    .name(userRecord.getDisplayName())
                    .profilePictureUrl(userRecord.getPhotoUrl())
                    .emailVerified(userRecord.isEmailVerified())
                    .phoneNumber(userRecord.getPhoneNumber())
                    .providerId(decodedToken.getIssuer())
                    .build();

            log.debug("User details extracted: {}", userDetails);
            return userDetails;

        } catch (FirebaseAuthException e) {
            log.error("Failed to verify Firebase token", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token verification", e);
            throw new RuntimeException("Internal server error during authentication", e);
        }
    }

    /**
     * Generates a simple access token for the authenticated user
     * In a production environment, you might want to use JWT tokens
     * 
     * @param userDetails User details
     * @return Access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        // For now, generate a simple UUID-based token
        // In production, consider using JWT with proper signing
        String token = UUID.randomUUID().toString() + "-" + userDetails.getUid();
        log.debug("Generated access token for user: {}", userDetails.getUid());
        return token;
    }

    /**
     * Validates if the Firebase token is still valid
     * 
     * @param idToken Firebase ID token
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String idToken) {
        if (!firebaseConfig.isFirebaseEnabled()) {
            log.warn("Firebase is not configured. Token validation is not available.");
            return false;
        }

        try {
            FirebaseAuth.getInstance().verifyIdToken(idToken);
            return true;
        } catch (FirebaseAuthException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if authentication service is available
     * 
     * @return true if Firebase is properly configured and available
     */
    public boolean isAuthenticationAvailable() {
        return firebaseConfig.isFirebaseEnabled();
    }
}