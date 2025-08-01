package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenerateFamilyCodeResponse {
    private Long familyCode;
    private boolean isValid;
    private String message;
} 