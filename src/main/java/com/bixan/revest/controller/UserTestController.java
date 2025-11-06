package com.bixan.revest.controller;

import com.bixan.revest.dao.UserDao;
import com.bixan.revest.entities.User;
import com.bixan.revest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test controller for User management system
 */
@RestController
@RequestMapping("/api/test")
@Profile("!test")
public class UserTestController {

    private static final Logger logger = LoggerFactory.getLogger(UserTestController.class);

    private final UserService userService;
    private final UserDao userDao;

    @Autowired
    public UserTestController(UserService userService, UserDao userDao) {
        this.userService = userService;
        this.userDao = userDao;
    }

    /**
     * Test the complete user flow
     */
    @GetMapping("/user-flow")
    public ResponseEntity<Map<String, Object>> testUserFlow() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Starting user flow test...");

            // Check if users table exists
            boolean tableExists = userDao.userTableExists();
            response.put("userTableExists", tableExists);

            if (!tableExists) {
                userDao.createUserTableIfNotExists();
                response.put("tableCreated", true);
            }

            // Create a test user
            String testUid = "test-user-" + System.currentTimeMillis();
            User testUser = new User();
            testUser.setUid(testUid);
            testUser.setEmail("test@example.com");
            testUser.setDisplayName("Test User");
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setProvider("firebase");
            testUser.setEmailVerified(true);

            // Test user creation
            User createdUser = userService.createUser(testUser);
            response.put("userCreated", true);
            response.put("createdUserId", createdUser.getId());

            // Test user login
            User loggedInUser = userService.loginUser(testUid);
            response.put("userLoggedIn", true);
            response.put("lastLoginUpdated", loggedInUser.getLastLoginAt() != null);

            // Test session validation
            boolean sessionValid = userService.isUserSessionValid(testUid);
            response.put("sessionValid", sessionValid);

            // Test getting user info
            Optional<User> userInfo = userService.getUserInfo(testUid);
            response.put("userInfoRetrieved", userInfo.isPresent());

            // Test session info
            Map<String, Object> sessionInfo = userService.getUserSessionInfo(testUid);
            response.put("sessionInfo", sessionInfo);

            // Test user update
            createdUser.setDisplayName("Updated Test User");
            User updatedUser = userService.updateUser(createdUser);
            response.put("userUpdated", true);
            response.put("updatedDisplayName", updatedUser.getDisplayName());

            // Test getting all users
            List<User> allUsers = userService.getAllActiveUsers();
            response.put("totalUsers", allUsers.size());

            // Test logout
            userService.logoutUser(testUid);
            response.put("userLoggedOut", true);

            // Clean up - deactivate test user
            userService.deactivateUser(testUid);
            response.put("userDeactivated", true);

            response.put("success", true);
            response.put("message", "User flow test completed successfully");

            logger.info("User flow test completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("User flow test failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test database operations
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test table existence
            boolean tableExists = userDao.userTableExists();
            response.put("userTableExists", tableExists);

            // Get user count
            long userCount = userService.getActiveUserCount();
            response.put("activeUserCount", userCount);

            // Get session count
            int sessionCount = userService.getActiveSessionCount();
            response.put("activeSessionCount", sessionCount);

            // Test cleanup
            userService.cleanupExpiredSessions();
            response.put("sessionsCleanedUp", true);

            response.put("success", true);
            response.put("message", "Database test completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Database test failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}