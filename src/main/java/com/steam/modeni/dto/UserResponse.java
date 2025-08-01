package com.steam.modeni.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String city;
    private String district;
    private String phoneNumber;
    private Integer age;
    private Long familyId;
    private String familyCode;
    private LocalDateTime createdAt;
} 