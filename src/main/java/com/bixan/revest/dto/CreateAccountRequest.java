package com.bixan.revest.dto;

import com.bixan.revest.entities.AccountType;
import com.bixan.revest.entities.CreationType;

/**
 * Data Transfer Object for Account creation requests
 */
public class CreateAccountRequest {

    private String name;
    private AccountType type;
    private Integer balanceCents;
    private String provider;
    private String accountId;
    private CreationType creationType;

    // Default constructor
    public CreateAccountRequest() {
        this.balanceCents = 0;
    }

    // Constructor with required fields
    public CreateAccountRequest(String name, AccountType type, CreationType creationType) {
        this();
        this.name = name;
        this.type = type;
        this.creationType = creationType;
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

    public CreationType getCreationType() {
        return creationType;
    }

    public void setCreationType(CreationType creationType) {
        this.creationType = creationType;
    }
}
