package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String familyCode;
    private String message;
    
    public AuthResponse(String token, Long userId, String username, String familyCode, String message) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.familyCode = familyCode;
        this.message = message;
    }
} 