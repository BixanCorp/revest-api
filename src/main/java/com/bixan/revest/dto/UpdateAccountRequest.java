package com.bixan.revest.dto;

import com.bixan.revest.entities.AccountType;

/**
 * Data Transfer Object for Account update requests
 */
public class UpdateAccountRequest {

    private String name;
    private AccountType type;
    private Integer balanceCents;
    private String provider;
    private String accountId;

    // Default constructor
    public UpdateAccountRequest() {
    }

    // Getters and Setters
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
}
