package com.steam.modeni.dto;

import com.steam.modeni.domain.enums.Region;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String userId;
    private String role;
    private Region region;
    private String age;
    private Long familyCode;
    private LocalDateTime createdAt;
}