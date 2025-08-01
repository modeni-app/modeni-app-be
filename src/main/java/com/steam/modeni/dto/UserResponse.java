package com.steam.modeni.dto;

import com.steam.modeni.domain.enums.City;
import com.steam.modeni.domain.enums.FamilyRole;
import com.steam.modeni.domain.enums.PersonalityType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private FamilyRole role;
    private String customRole;
    private City city;
    private Integer age;
    private PersonalityType personalityType;
    private Long familyId;
    private String familyCode;
    private LocalDateTime createdAt;
} 