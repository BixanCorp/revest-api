package com.bixan.revest.dto;

import com.bixan.revest.entities.Account;
import com.bixan.revest.entities.AccountType;
import com.bixan.revest.entities.CreationType;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Account responses
 */
public class AccountResponse {

    private Long id;
    private Long userId;
    private String name;
    private AccountType type;
    private Integer balanceCents;
    private Double balanceDollars;
    private String provider;
    private String accountId;
    private CreationType creationType;
    private LocalDateTime created;
    private LocalDateTime updated;

    // Default constructor
    public AccountResponse() {
    }

    // Constructor from Account entity
    public AccountResponse(Account account) {
        this.id = account.getId();
        this.userId = account.getUserId();
        this.name = account.getName();
        this.type = account.getType();
        this.balanceCents = account.getBalanceCents();
        this.balanceDollars = account.getBalanceCents() / 100.0;
        this.provider = account.getProvider();
        this.accountId = account.getAccountId();
        this.creationType = account.getCreationType();
        this.created = account.getCreated();
        this.updated = account.getUpdated();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Integer getBalanceCents() {
        return balanceCents;
    }

    public void setBalanceCents(Integer balanceCents) {
        this.balanceCents = balanceCents;
        this.balanceDollars = balanceCents / 100.0;
    }

    public Double getBalanceDollars() {
        return balanceDollars;
    }

    public void setBalanceDollars(Double balanceDollars) {
        this.balanceDollars = balanceDollars;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public CreationType getCreationType() {
        return creationType;
    }

    public void setCreationType(CreationType creationType) {
        this.creationType = creationType;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }
}
