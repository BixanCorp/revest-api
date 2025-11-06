package com.bixan.revest.dao;

import com.bixan.revest.entities.User;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User operations
 */
@Repository
@Profile("!test")
public class UserDao extends AbstractBaseDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * Create the users table if it doesn't exist
     */
    public void createUserTableIfNotExists() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "firebase_id VARCHAR(255) UNIQUE NOT NULL," +
                "email VARCHAR(255) UNIQUE NOT NULL," +
                "firstname VARCHAR(255)," +
                "lastname VARCHAR(255)," +
                "phone VARCHAR(63)," +
                "state VARCHAR(63) NOT NULL," +
                "country VARCHAR(3) NOT NULL DEFAULT 'USA'," +
                "currency VARCHAR(3) DEFAULT 'USD'," +
                "active TINYINT(1) NOT NULL DEFAULT 1," +
                "first_login DATETIME NULL," +
                "last_login DATETIME NULL," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "role_id INT NULL," +
                "INDEX idx_firebase_id (firebase_id)," +
                "INDEX idx_email (email)," +
                "INDEX idx_active (active)," +
                "INDEX idx_state (state)," +
                "INDEX idx_country (country)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        jdbcTemplate.execute(createTableSql);
    }

    /**
     * Check if users table exists
     */
    public boolean userTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Create a new user
     */
    public User createUser(User user) {
        String sql = "INSERT INTO users (firebase_id, email, firstname, lastname, " +
                "phone, state, country, currency, active, first_login, created_at, updated_at) " +
                "VALUES (:firebaseId, :email, :firstname, :lastname, " +
                ":phone, :state, :country, :currency, :active, :firstLogin, :createdAt, :updatedAt)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firebaseId", user.getUid())
                .addValue("email", user.getEmail())
                .addValue("firstname", user.getFirstName())
                .addValue("lastname", user.getLastName())
                .addValue("phone", user.getPhoneNumber())
                .addValue("state", user.getState())
                .addValue("country", user.getCountry())
                .addValue("currency", user.getCurrency())
                .addValue("active", user.isActive())
                .addValue("firstLogin", user.getLastLoginAt() != null ? Timestamp.valueOf(user.getLastLoginAt()) : null)
                .addValue("createdAt", Timestamp.valueOf(user.getCreatedAt()))
                .addValue("updatedAt", Timestamp.valueOf(user.getUpdatedAt()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder);

        Long id = keyHolder.getKey().longValue();
        user.setId(id);

        return user;
    }

    /**
     * Find user by UID
     */
    public Optional<User> findByUid(String uid) {
        String sql = "SELECT * FROM users WHERE firebase_id = ? AND active = 1";

        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), uid);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND active = 1";

        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ? AND active = 1";

        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Update user's last login timestamp
     */
    public void updateLastLogin(String uid) {
        String sql = "UPDATE users SET last_login = ?, updated_at = ? WHERE firebase_id = ?";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, Timestamp.valueOf(now), Timestamp.valueOf(now), uid);
    }

    /**
     * Update user information
     */
    public User updateUser(User user) {
        user.markAsUpdated();

        String sql = "UPDATE users SET " +
                "email = :email, " +
                "firstname = :firstname, " +
                "lastname = :lastname, " +
                "phone = :phone, " +
                "state = :state, " +
                "country = :country, " +
                "currency = :currency, " +
                "active = :active, " +
                "updated_at = :updatedAt " +
                "WHERE firebase_id = :firebaseId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("firstname", user.getFirstName())
                .addValue("lastname", user.getLastName())
                .addValue("phone", user.getPhoneNumber())
                .addValue("state", user.getState())
                .addValue("country", user.getCountry())
                .addValue("currency", user.getCurrency())
                .addValue("active", user.isActive())
                .addValue("updatedAt", Timestamp.valueOf(user.getUpdatedAt()))
                .addValue("firebaseId", user.getUid());

        namedParameterJdbcTemplate.update(sql, params);
        return user;
    }

    /**
     * Soft delete user (set active to false)
     */
    public void deactivateUser(String uid) {
        String sql = "UPDATE users SET active = 0, updated_at = ? WHERE firebase_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), uid);
    }

    /**
     * Hard delete user (permanent removal)
     */
    public void deleteUser(String uid) {
        String sql = "DELETE FROM users WHERE firebase_id = ?";
        jdbcTemplate.update(sql, uid);
    }

    /**
     * Get all active users
     */
    public List<User> findAllActiveUsers() {
        String sql = "SELECT * FROM users WHERE active = 1 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    /**
     * Get users with recent login activity
     */
    public List<User> findRecentlyActiveUsers(int days) {
        String sql = "SELECT * FROM users WHERE active = 1 AND last_login >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY last_login DESC";
        return jdbcTemplate.query(sql, new UserRowMapper(), days);
    }

    /**
     * Count total active users
     */
    public long countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE active = 1";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    /**
     * Check if user exists by UID
     */
    public boolean existsByUid(String uid) {
        String sql = "SELECT COUNT(*) FROM users WHERE firebase_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uid);
        return count != null && count > 0;
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * RowMapper for User entity
     */
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUid(rs.getString("firebase_id"));
            user.setEmail(rs.getString("email"));
            user.setFirstName(rs.getString("firstname"));
            user.setLastName(rs.getString("lastname"));
            user.setPhoneNumber(rs.getString("phone"));
            user.setState(rs.getString("state"));
            user.setCountry(rs.getString("country"));
            user.setCurrency(rs.getString("currency"));
            user.setActive(rs.getBoolean("active"));

            // Set display name from first and last name
            if (user.getFirstName() != null && user.getLastName() != null) {
                user.setDisplayName(user.getFirstName() + " " + user.getLastName());
            }

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                user.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            Timestamp firstLogin = rs.getTimestamp("first_login");
            if (firstLogin != null) {
                user.setLastLoginAt(firstLogin.toLocalDateTime());
            }

            Timestamp lastLogin = rs.getTimestamp("last_login");
            if (lastLogin != null) {
                user.setLastLoginAt(lastLogin.toLocalDateTime());
            }

            // Set default values for fields not in database
            user.setEmailVerified(true); // Default to true since user registered
            user.setProvider("firebase");

            return user;
        }
    }
}