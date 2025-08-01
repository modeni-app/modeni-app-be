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
    private Long id;
    private String userId;
    private Long familyCode;
    private String message;
    
    public AuthResponse(String token, Long id, String userId, Long familyCode, String message) {
        this.token = token;
        this.id = id;
        this.userId = userId;
        this.familyCode = familyCode;
        this.message = message;
    }
} 