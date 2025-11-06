package com.bixan.revest.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetails {
    private Long userId; // Database ID from User table
    private String uid; // Firebase UID
    private String email;
    private String firstName;
    private String lastName;
    private String name; // Display name (computed from firstName + lastName or displayName)
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean emailVerified;
    private String providerId;
    private String state;
    private String country;
    private String currency;
    private LocalDateTime createdAt;
}