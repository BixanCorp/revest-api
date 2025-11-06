package com.bixan.revest.service;

import com.bixan.revest.dao.UserDao;
import com.bixan.revest.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for User management operations
 */
@Service
@Profile("!test")
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;

    // In-memory storage for active user sessions
    private final Map<String, User> activeUsers = new ConcurrentHashMap<>();

    // Session timeout in minutes
    private static final long SESSION_TIMEOUT_MINUTES = 120; // 2 hours

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @PostConstruct
    public void init() {
        // Ensure user table exists
        try {
            if (!userDao.userTableExists()) {
                logger.info("Users table does not exist. Creating it...");
                userDao.createUserTableIfNotExists();
                logger.info("Users table created successfully.");
            } else {
                logger.info("Users table already exists.");
            }
        } catch (Exception e) {
            logger.error("Error checking/creating users table: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a new user
     */
    public User createUser(User user) {
        logger.info("Creating new user with UID: {} and email: {}", user.getUid(), user.getEmail());

        // Check if user already exists
        if (userDao.existsByUid(user.getUid())) {
            throw new IllegalArgumentException("User with UID " + user.getUid() + " already exists");
        }

        if (userDao.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        // Set creation timestamp
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Create user in database
        User createdUser = userDao.createUser(user);

        // Add to active sessions
        activeUsers.put(createdUser.getUid(), createdUser);

        logger.info("User created successfully with ID: {}", createdUser.getId());
        return createdUser;
    }

    /**
     * Handle user login - update last login timestamp and load user into memory
     */
    public User loginUser(String uid) {
        logger.info("Processing login for user UID: {}", uid);

        Optional<User> userOpt = userDao.findByUid(uid);
        if (!userOpt.isPresent()) {
            return null;
        }

        User user = userOpt.get();

        // Update last login timestamp
        userDao.updateLastLogin(uid);
        user.updateLastLogin();

        // Add to active sessions
        activeUsers.put(uid, user);

        logger.info("User logged in successfully: {}", user.getEmail());
        return user;
    }

    /**
     * Handle user logout - remove from active sessions
     */
    public void logoutUser(String uid) {
        logger.info("Processing logout for user UID: {}", uid);
        activeUsers.remove(uid);
        logger.info("User logged out successfully");
    }

    /**
     * Get user information (from memory if available, otherwise from database)
     */
    public Optional<User> getUserInfo(String uid) {
        // First check in-memory active users
        User activeUser = activeUsers.get(uid);
        if (activeUser != null) {
            logger.debug("Retrieved user from active session: {}", uid);
            return Optional.of(activeUser);
        }

        // If not in memory, fetch from database
        logger.debug("Fetching user from database: {}", uid);
        return userDao.findByUid(uid);
    }

    /**
     * Check if user exists (in memory or database)
     */
    public boolean userExists(String uid) {
        // First check in-memory active users
        if (activeUsers.containsKey(uid)) {
            logger.debug("User found in active session: {}", uid);
            return true;
        }

        // If not in memory, check database
        logger.debug("Checking user existence in database: {}", uid);
        return userDao.findByUid(uid).isPresent();
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        // Check active users first
        for (User user : activeUsers.values()) {
            if (email.equals(user.getEmail())) {
                return Optional.of(user);
            }
        }

        // Fetch from database
        return userDao.findByEmail(email);
    }

    /**
     * Update user information
     */
    public User updateUser(User user) {
        logger.info("Updating user: {}", user.getUid());

        User updatedUser = userDao.updateUser(user);

        // Update in active sessions if present
        if (activeUsers.containsKey(user.getUid())) {
            activeUsers.put(user.getUid(), updatedUser);
        }

        logger.info("User updated successfully: {}", user.getUid());
        return updatedUser;
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(String uid) {
        logger.info("Deactivating user: {}", uid);

        userDao.deactivateUser(uid);

        // Remove from active sessions
        activeUsers.remove(uid);

        logger.info("User deactivated successfully: {}", uid);
    }

    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        return userDao.findAllActiveUsers();
    }

    /**
     * Get recently active users
     */
    public List<User> getRecentlyActiveUsers(int days) {
        return userDao.findRecentlyActiveUsers(days);
    }

    /**
     * Get count of active users
     */
    public long getActiveUserCount() {
        return userDao.countActiveUsers();
    }

    /**
     * Check if user session is valid and active
     */
    public boolean isUserSessionValid(String uid) {
        User activeUser = activeUsers.get(uid);
        if (activeUser == null) {
            return false;
        }

        // Check if session has expired
        if (activeUser.getLastLoginAt() != null) {
            LocalDateTime sessionExpiry = activeUser.getLastLoginAt().plusMinutes(SESSION_TIMEOUT_MINUTES);
            if (LocalDateTime.now().isAfter(sessionExpiry)) {
                // Session expired, remove from active users
                activeUsers.remove(uid);
                logger.info("Session expired for user: {}", uid);
                return false;
            }
        }

        return true;
    }

    /**
     * Get currently active users count (in memory)
     */
    public int getActiveSessionCount() {
        return activeUsers.size();
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        activeUsers.entrySet().removeIf(entry -> {
            User user = entry.getValue();
            if (user.getLastLoginAt() != null) {
                LocalDateTime sessionExpiry = user.getLastLoginAt().plusMinutes(SESSION_TIMEOUT_MINUTES);
                if (now.isAfter(sessionExpiry)) {
                    logger.info("Removing expired session for user: {}", entry.getKey());
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Get user session information
     */
    private Map<String, Object> createSuccessResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("uid", user.getUid());
        response.put("email", user.getEmail());
        response.put("displayName", user.getDisplayName());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("profilePictureUrl", user.getProfilePictureUrl());
        response.put("emailVerified", user.isEmailVerified());
        response.put("active", user.isActive());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());
        response.put("lastLoginAt", user.getLastLoginAt());
        response.put("provider", user.getProvider());
        return response;
    }

    /**
     * Get session information for a user
     */
    public Map<String, Object> getUserSessionInfo(String uid) {
        Map<String, Object> sessionInfo = new HashMap<>();

        // Check if user has an active session
        boolean isLoggedIn = activeUsers.containsKey(uid);
        sessionInfo.put("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            User activeUser = activeUsers.get(uid);
            sessionInfo.put("loginTime", activeUser.getLastLoginAt());
            if (activeUser.getLastLoginAt() != null) {
                sessionInfo.put("sessionDuration",
                        java.time.Duration.between(activeUser.getLastLoginAt(), LocalDateTime.now()).toMinutes());
            }
        }

        return sessionInfo;
    }
}