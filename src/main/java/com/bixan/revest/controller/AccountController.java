package com.bixan.revest.controller;

import com.bixan.revest.dto.AccountResponse;
import com.bixan.revest.dto.CreateAccountRequest;
import com.bixan.revest.dto.UpdateAccountRequest;
import com.bixan.revest.entities.AccountType;
import com.bixan.revest.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Account management operations
 */
@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" }, allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
        RequestMethod.OPTIONS }, allowCredentials = "true", maxAge = 3600)
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    /**
     * Helper method to create error response map
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }

    /**
     * Helper method to create success response map
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    /**
     * Helper method to create count response map
     */
    private Map<String, Object> createCountResponse(int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return response;
    }

    /**
     * Create a new account for the authenticated user
     * POST /api/accounts
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Get userId from session (set by SessionValidationInterceptor)
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Create account attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Creating account for user ID: {}", userId);
            AccountResponse account = accountService.createAccount(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (Exception e) {
            logger.error("Error creating account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Get all accounts for the authenticated user
     * GET /api/accounts
     */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAccounts(@RequestParam(required = false) AccountType type,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Get accounts attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Fetching accounts for user ID: {}", userId);

            List<AccountResponse> accounts;
            if (type != null) {
                accounts = accountService.getAccountsByUserIdAndType(userId, type);
            } else {
                accounts = accountService.getAccountsByUserId(userId);
            }

            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            logger.error("Error fetching accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Get a specific account by ID
     * GET /api/accounts/{accountId}
     */
    @GetMapping(value = "/{accountId}", produces = "application/json")
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Get account attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Fetching account ID: {} for user ID: {}", accountId, userId);

            Optional<AccountResponse> account = accountService.getAccountById(accountId);

            if (account.isPresent()) {
                // Verify the account belongs to the user
                if (!account.get().getUserId().equals(userId)) {
                    logger.warn("User {} attempted to access account {} owned by user {}",
                            userId, accountId, account.get().getUserId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createErrorResponse("Forbidden", "Access denied to this account"));
                }
                return ResponseEntity.ok(account.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Not Found", "Account not found"));
            }
        } catch (Exception e) {
            logger.error("Error fetching account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Update an existing account
     * PUT /api/accounts/{accountId}
     */
    @PutMapping(value = "/{accountId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId,
            @RequestBody UpdateAccountRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Update account attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Updating account ID: {} for user ID: {}", accountId, userId);

            AccountResponse updatedAccount = accountService.updateAccount(accountId, userId, request);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            logger.error("Error updating account: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Not Found", e.getMessage()));
            } else if (e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Forbidden", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Bad Request", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Soft delete an account
     * DELETE /api/accounts/{accountId}
     */
    @DeleteMapping(value = "/{accountId}", produces = "application/json")
    public ResponseEntity<?> deleteAccount(@PathVariable Long accountId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Delete account attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Deleting account ID: {} for user ID: {}", accountId, userId);

            accountService.deleteAccount(accountId, userId);
            return ResponseEntity.ok(createSuccessResponse("Account deleted successfully"));
        } catch (RuntimeException e) {
            logger.error("Error deleting account: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Not Found", e.getMessage()));
            } else if (e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Forbidden", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Bad Request", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Get count of active accounts for the authenticated user
     * GET /api/accounts/count
     */
    @GetMapping(value = "/count", produces = "application/json")
    public ResponseEntity<?> getAccountCount(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");

            if (userId == null) {
                logger.warn("Get account count attempt without valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "Valid session required"));
            }

            logger.info("Getting account count for user ID: {}", userId);

            int count = accountService.getActiveAccountCount(userId);
            return ResponseEntity.ok(createCountResponse(count));
        } catch (Exception e) {
            logger.error("Error getting account count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }
}
