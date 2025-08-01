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
    private String userIdString;
    private Long familyCode;
    private String message;
    
    public AuthResponse(String token, Long userId, String userIdString, Long familyCode, String message) {
        this.token = token;
        this.userId = userId;
        this.userIdString = userIdString;
        this.familyCode = familyCode;
        this.message = message;
    }
}