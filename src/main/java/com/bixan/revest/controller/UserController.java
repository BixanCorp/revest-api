package com.bixan.revest.controller;

import com.bixan.revest.dto.CreateUserRequest;
import com.bixan.revest.entities.User;
import com.bixan.revest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for User management endpoints
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" })
@Profile("!test")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Creating user with UID: {} and email: {}", request.getUid(), request.getEmail());

            // Validate required fields
            if (request.getUid() == null || request.getUid().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "UID is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Convert CreateUserRequest to User entity
            User user = convertToUser(request);

            User createdUser = userService.createUser(user);

            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("user", sanitizeUser(createdUser));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("User creation failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String uid = loginRequest.get("uid");

            if (uid == null || uid.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "UID is required");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.loginUser(uid);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", sanitizeUser(user));
            response.put("sessionInfo", userService.getUserSessionInfo(uid));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Login failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(@RequestBody Map<String, String> logoutRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String uid = logoutRequest.get("uid");

            if (uid == null || uid.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "UID is required");
                return ResponseEntity.badRequest().body(response);
            }

            userService.logoutUser(uid);

            response.put("success", true);
            response.put("message", "Logout successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user information by UID
     */
    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userService.getUserInfo(uid);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            response.put("success", true);
            response.put("user", sanitizeUser(user));
            response.put("sessionInfo", userService.getUserSessionInfo(uid));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving user info: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user information by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            response.put("success", true);
            response.put("user", sanitizeUser(user));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving user by email: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update user information
     */
    @PutMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String uid, @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Ensure UID matches
            user.setUid(uid);

            User updatedUser = userService.updateUser(user);

            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", sanitizeUser(updatedUser));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deactivate user account
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.deactivateUser(uid);

            response.put("success", true);
            response.put("message", "User deactivated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all active users (admin endpoint)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllActiveUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> users = userService.getAllActiveUsers();

            response.put("success", true);
            response.put("users", users.stream().map(this::sanitizeUser).collect(Collectors.toList()));
            response.put("count", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving all users: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get recently active users
     */
    @GetMapping("/recent/{days}")
    public ResponseEntity<Map<String, Object>> getRecentlyActiveUsers(@PathVariable int days) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> users = userService.getRecentlyActiveUsers(days);

            response.put("success", true);
            response.put("users", users.stream().map(this::sanitizeUser).collect(Collectors.toList()));
            response.put("count", users.size());
            response.put("days", days);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving recent users: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check user session status
     */
    @GetMapping("/{uid}/session")
    public ResponseEntity<Map<String, Object>> checkUserSession(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = userService.isUserSessionValid(uid);
            Map<String, Object> sessionInfo = userService.getUserSessionInfo(uid);

            response.put("success", true);
            response.put("sessionValid", isValid);
            response.put("sessionInfo", sessionInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking user session: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user statistics (admin endpoint)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            long totalActiveUsers = userService.getActiveUserCount();
            int activeSessionCount = userService.getActiveSessionCount();

            response.put("success", true);
            response.put("totalActiveUsers", totalActiveUsers);
            response.put("activeSessionCount", activeSessionCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving user stats: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Clean up expired sessions (admin endpoint)
     */
    @PostMapping("/cleanup-sessions")
    public ResponseEntity<Map<String, Object>> cleanupExpiredSessions() {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.cleanupExpiredSessions();

            response.put("success", true);
            response.put("message", "Expired sessions cleaned up");
            response.put("activeSessionCount", userService.getActiveSessionCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error cleaning up sessions: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Convert CreateUserRequest to User entity
     */
    private User convertToUser(CreateUserRequest request) {
        User user = new User();
        user.setUid(request.getUid());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setEmailVerified(request.isEmailVerified());
        user.setProvider(request.getProvider());
        user.setProviderData(request.getProviderData());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setCurrency(request.getCurrency());
        return user;
    }

    /**
     * Sanitize user object for API response (remove sensitive data)
     */
    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("id", user.getId());
        sanitized.put("uid", user.getUid());
        sanitized.put("email", user.getEmail());
        sanitized.put("displayName", user.getDisplayName());
        sanitized.put("firstName", user.getFirstName());
        sanitized.put("lastName", user.getLastName());
        sanitized.put("phoneNumber", user.getPhoneNumber());
        sanitized.put("profilePictureUrl", user.getProfilePictureUrl());
        sanitized.put("emailVerified", user.isEmailVerified());
        sanitized.put("active", user.isActive());
        sanitized.put("createdAt", user.getCreatedAt());
        sanitized.put("updatedAt", user.getUpdatedAt());
        sanitized.put("lastLoginAt", user.getLastLoginAt());
        sanitized.put("provider", user.getProvider());
        sanitized.put("state", user.getState());
        sanitized.put("country", user.getCountry());
        sanitized.put("currency", user.getCurrency());
        sanitized.put("fullName", user.getFullName());
        // Note: providerData is not included as it may contain sensitive information
        return sanitized;
    }
}