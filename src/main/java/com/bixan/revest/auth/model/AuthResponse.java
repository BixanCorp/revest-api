package com.bixan.revest.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private boolean success;
    private String message;
    private UserDetails userDetails;
    private String accessToken;
    
    public static AuthResponse success(UserDetails userDetails, String accessToken) {
        return AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .userDetails(userDetails)
                .accessToken(accessToken)
                .build();
    }
    
    public static AuthResponse failure(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}