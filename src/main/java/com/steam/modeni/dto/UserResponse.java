package com.steam.modeni.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String userId;
    private String name;
    private String role;
    private String region;
    private String age;
    private Long familyCode;
    private LocalDateTime createdAt;
} 