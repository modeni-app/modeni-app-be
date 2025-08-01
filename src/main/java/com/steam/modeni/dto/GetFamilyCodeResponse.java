package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetFamilyCodeResponse {
    private String familyCode;
    private String message;
} 