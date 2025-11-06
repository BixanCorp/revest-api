package com.bixan.revest.service;

import com.bixan.revest.dao.AccountDao;
import com.bixan.revest.dto.AccountResponse;
import com.bixan.revest.dto.CreateAccountRequest;
import com.bixan.revest.dto.UpdateAccountRequest;
import com.bixan.revest.entities.Account;
import com.bixan.revest.entities.AccountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Account management operations
 */
@Service
@Profile("!test")
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountDao accountDao;

    @Autowired
    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @PostConstruct
    public void init() {
        // Ensure account table exists
        try {
            if (!accountDao.accountTableExists()) {
                logger.info("Accounts table does not exist. Creating it...");
                accountDao.createAccountTableIfNotExists();
                logger.info("Accounts table created successfully.");
            } else {
                logger.info("Accounts table already exists.");
            }
        } catch (Exception e) {
            logger.error("Error checking/creating accounts table: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a new account for a user
     */
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        logger.info("Creating account for user ID: {}", userId);

        Account account = new Account();
        account.setUserId(userId);
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalanceCents(request.getBalanceCents() != null ? request.getBalanceCents() : 0);
        account.setProvider(request.getProvider());
        account.setAccountId(request.getAccountId());
        account.setCreationType(request.getCreationType());

        Account createdAccount = accountDao.createAccount(account);
        return new AccountResponse(createdAccount);
    }

    /**
     * Get an account by ID
     */
    public Optional<AccountResponse> getAccountById(Long accountId) {
        logger.debug("Fetching account with ID: {}", accountId);
        return accountDao.findById(accountId)
                .map(AccountResponse::new);
    }

    /**
     * Get all accounts for a user
     */
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        logger.debug("Fetching all accounts for user ID: {}", userId);
        return accountDao.findByUserId(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by user ID and type
     */
    public List<AccountResponse> getAccountsByUserIdAndType(Long userId, AccountType type) {
        logger.debug("Fetching accounts for user ID: {} with type: {}", userId, type);
        return accountDao.findByUserIdAndType(userId, type).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing account
     */
    public AccountResponse updateAccount(Long accountId, Long userId, UpdateAccountRequest request) {
        logger.info("Updating account ID: {} for user ID: {}", accountId, userId);

        Account account = accountDao.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify the account belongs to the user
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Account does not belong to user");
        }

        // Update fields if provided
        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getType() != null) {
            account.setType(request.getType());
        }
        if (request.getBalanceCents() != null) {
            account.setBalanceCents(request.getBalanceCents());
        }
        if (request.getProvider() != null) {
            account.setProvider(request.getProvider());
        }
        if (request.getAccountId() != null) {
            account.setAccountId(request.getAccountId());
        }

        account.markAsUpdated();
        Account updatedAccount = accountDao.updateAccount(account);
        return new AccountResponse(updatedAccount);
    }

    /**
     * Soft delete an account
     */
    public void deleteAccount(Long accountId, Long userId) {
        logger.info("Deleting account ID: {} for user ID: {}", accountId, userId);

        Account account = accountDao.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify the account belongs to the user
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Account does not belong to user");
        }

        accountDao.softDelete(accountId);
    }

    /**
     * Get count of active accounts for a user
     */
    public int getActiveAccountCount(Long userId) {
        return accountDao.countActiveAccountsByUserId(userId);
    }
}
