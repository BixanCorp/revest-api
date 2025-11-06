package com.bixan.revest.dao;

import com.bixan.revest.entities.Account;
import com.bixan.revest.entities.AccountType;
import com.bixan.revest.entities.CreationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Data Access Object for Account operations
 */
@Repository
@Profile("!test")
public class AccountDao extends AbstractBaseDao {

    private static final Logger logger = LoggerFactory.getLogger(AccountDao.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * Create the accounts table if it doesn't exist
     */
    public void createAccountTableIfNotExists() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "type VARCHAR(50) NOT NULL," +
                "balance_cents INT NOT NULL DEFAULT 0," +
                "provider VARCHAR(255)," +
                "account_id VARCHAR(255)," +
                "creation_type VARCHAR(50) NOT NULL," +
                "deleted TINYINT(1) NOT NULL DEFAULT 0," +
                "created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "INDEX idx_user_id (user_id)," +
                "INDEX idx_type (type)," +
                "INDEX idx_deleted (deleted)," +
                "INDEX idx_created (created)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        jdbcTemplate.execute(createTableSql);
        logger.info("Accounts table created or already exists");
    }

    /**
     * Check if accounts table exists
     */
    public boolean accountTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'accounts'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Create a new account
     */
    public Account createAccount(Account account) {
        String sql = "INSERT INTO accounts (user_id, name, type, balance_cents, provider, " +
                "account_id, creation_type, deleted, created, updated) " +
                "VALUES (:userId, :name, :type, :balanceCents, :provider, " +
                ":accountId, :creationType, :deleted, :created, :updated)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", account.getUserId())
                .addValue("name", account.getName())
                .addValue("type", account.getType().name())
                .addValue("balanceCents", account.getBalanceCents())
                .addValue("provider", account.getProvider())
                .addValue("accountId", account.getAccountId())
                .addValue("creationType", account.getCreationType().name())
                .addValue("deleted", account.isDeleted())
                .addValue("created", Timestamp.valueOf(account.getCreated()))
                .addValue("updated", Timestamp.valueOf(account.getUpdated()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder);

        Long id = keyHolder.getKey().longValue();
        account.setId(id);

        logger.info("Created account with ID: {}", id);
        return account;
    }

    /**
     * Find account by ID (excluding soft-deleted accounts)
     */
    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM accounts WHERE id = ? AND deleted = 0";
        try {
            Account account = jdbcTemplate.queryForObject(sql, new AccountRowMapper(), id);
            return Optional.ofNullable(account);
        } catch (Exception e) {
            logger.debug("Account not found with ID: {}", id);
            return Optional.empty();
        }
    }

    /**
     * Find all accounts for a user (excluding soft-deleted accounts)
     */
    public List<Account> findByUserId(Long userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? AND deleted = 0 ORDER BY created DESC";
        return jdbcTemplate.query(sql, new AccountRowMapper(), userId);
    }

    /**
     * Find all accounts for a user by type (excluding soft-deleted accounts)
     */
    public List<Account> findByUserIdAndType(Long userId, AccountType type) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? AND type = ? AND deleted = 0 ORDER BY created DESC";
        return jdbcTemplate.query(sql, new AccountRowMapper(), userId, type.name());
    }

    /**
     * Update an existing account
     */
    public Account updateAccount(Account account) {
        String sql = "UPDATE accounts SET name = :name, type = :type, balance_cents = :balanceCents, " +
                "provider = :provider, account_id = :accountId, updated = :updated " +
                "WHERE id = :id AND deleted = 0";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", account.getId())
                .addValue("name", account.getName())
                .addValue("type", account.getType().name())
                .addValue("balanceCents", account.getBalanceCents())
                .addValue("provider", account.getProvider())
                .addValue("accountId", account.getAccountId())
                .addValue("updated", Timestamp.valueOf(LocalDateTime.now()));

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);

        if (rowsAffected == 0) {
            logger.warn("No account updated with ID: {}", account.getId());
            throw new RuntimeException("Account not found or already deleted");
        }

        logger.info("Updated account with ID: {}", account.getId());
        return findById(account.getId()).orElseThrow(() -> new RuntimeException("Account not found after update"));
    }

    /**
     * Soft delete an account
     */
    public void softDelete(Long id) {
        String sql = "UPDATE accounts SET deleted = 1, updated = :updated WHERE id = :id AND deleted = 0";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("updated", Timestamp.valueOf(LocalDateTime.now()));

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);

        if (rowsAffected == 0) {
            logger.warn("No account deleted with ID: {}", id);
            throw new RuntimeException("Account not found or already deleted");
        }

        logger.info("Soft deleted account with ID: {}", id);
    }

    /**
     * Count active accounts for a user
     */
    public int countActiveAccountsByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE user_id = ? AND deleted = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * RowMapper for Account entity
     */
    private static class AccountRowMapper implements RowMapper<Account> {
        @Override
        public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
            Account account = new Account();
            account.setId(rs.getLong("id"));
            account.setUserId(rs.getLong("user_id"));
            account.setName(rs.getString("name"));
            account.setType(AccountType.valueOf(rs.getString("type")));
            account.setBalanceCents(rs.getInt("balance_cents"));
            account.setProvider(rs.getString("provider"));
            account.setAccountId(rs.getString("account_id"));
            account.setCreationType(CreationType.valueOf(rs.getString("creation_type")));
            account.setDeleted(rs.getBoolean("deleted"));

            Timestamp created = rs.getTimestamp("created");
            if (created != null) {
                account.setCreated(created.toLocalDateTime());
            }

            Timestamp updated = rs.getTimestamp("updated");
            if (updated != null) {
                account.setUpdated(updated.toLocalDateTime());
            }

            return account;
        }
    }
}
