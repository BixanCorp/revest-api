package com.bixan.revest.auth.service;

import com.bixan.revest.config.EnvironmentConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Autowired
    private EnvironmentConfig environmentConfig;

    private SecretKey getSigningKey() {
        try {
            // Create a SHA-256 hash of the salt to ensure it's the right length for
            // HMAC-SHA256
            String salt = environmentConfig.getJwtSecretSalt();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salt.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hash);
        } catch (Exception e) {
            log.error("Error creating signing key", e);
            throw new RuntimeException("Failed to create JWT signing key", e);
        }
    }

    /**
     * Creates a JWT session token containing user ID and Firebase token
     * 
     * @param userId        The revest user ID from the User entity
     * @param firebaseToken The Firebase ID token
     * @return JWT token string
     */
    public String createSessionToken(String userId, String firebaseToken) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + (environmentConfig.getSessionDurationSeconds() * 1000L));

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("firebaseToken", firebaseToken);
            claims.put("iat", now.getTime() / 1000);
            claims.put("exp", expiration.getTime() / 1000);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            log.debug("Created session token for user: {}, expires at: {}", userId, expiration);
            return token;

        } catch (Exception e) {
            log.error("Error creating session token for user: {}", userId, e);
            throw new RuntimeException("Failed to create session token", e);
        }
    }

    /**
     * Validates and parses a JWT session token
     * 
     * @param token The JWT token string
     * @return SessionTokenData containing parsed information
     * @throws JwtException if token is invalid or expired
     */
    public SessionTokenData validateAndParseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.get("userId", String.class);
            String firebaseToken = claims.get("firebaseToken", String.class);
            Date expiration = claims.getExpiration();

            if (userId == null || firebaseToken == null) {
                throw new JwtException("Token missing required claims");
            }

            log.debug("Validated session token for user: {}, expires at: {}", userId, expiration);
            return new SessionTokenData(userId, firebaseToken, expiration);

        } catch (ExpiredJwtException e) {
            log.debug("Session token expired: {}", e.getMessage());
            throw new JwtException("Session token expired", e);
        } catch (JwtException e) {
            log.debug("Invalid session token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error validating session token", e);
            throw new JwtException("Failed to validate session token", e);
        }
    }

    /**
     * Refreshes a session token with a new expiration time
     * 
     * @param userId        The user ID
     * @param firebaseToken The Firebase token
     * @return New JWT token with updated expiration
     */
    public String refreshSessionToken(String userId, String firebaseToken) {
        return createSessionToken(userId, firebaseToken);
    }

    /**
     * Data class for parsed session token information
     */
    public static class SessionTokenData {
        private final String userId;
        private final String firebaseToken;
        private final Date expiration;

        public SessionTokenData(String userId, String firebaseToken, Date expiration) {
            this.userId = userId;
            this.firebaseToken = firebaseToken;
            this.expiration = expiration;
        }

        public String getUserId() {
            return userId;
        }

        public String getFirebaseToken() {
            return firebaseToken;
        }

        public Date getExpiration() {
            return expiration;
        }

        public boolean isExpired() {
            return expiration.before(new Date());
        }
    }
}