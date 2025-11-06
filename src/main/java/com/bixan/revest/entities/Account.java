package com.bixan.revest.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Account entity representing a financial account in the system
 */
public class Account {

    private Long id;
    private Long userId; // Foreign key to User table
    private String name;
    private AccountType type;
    private Integer balanceCents;
    private String provider;
    private String accountId; // Provider-provided account ID
    private CreationType creationType;
    private boolean deleted;
    private LocalDateTime created;
    private LocalDateTime updated;

    // Default constructor
    public Account() {
        this.balanceCents = 0;
        this.deleted = false;
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    // Constructor with required fields
    public Account(Long userId, String name, AccountType type, CreationType creationType) {
        this();
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.creationType = creationType;
    }

    // Full constructor for database retrieval
    public Account(Long id, Long userId, String name, AccountType type, Integer balanceCents,
            String provider, String accountId, CreationType creationType,
            boolean deleted, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.balanceCents = balanceCents;
        this.provider = provider;
        this.accountId = accountId;
        this.creationType = creationType;
        this.deleted = deleted;
        this.created = created;
        this.updated = updated;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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

    public void markAsUpdated() {
        this.updated = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.deleted = true;
        markAsUpdated();
    }

    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(userId, account.userId) &&
                Objects.equals(name, account.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", balanceCents=" + balanceCents +
                ", provider='" + provider + '\'' +
                ", creationType=" + creationType +
                ", deleted=" + deleted +
                '}';
    }
}
