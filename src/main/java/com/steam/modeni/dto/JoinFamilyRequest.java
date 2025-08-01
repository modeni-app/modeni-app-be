package com.steam.modeni.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinFamilyRequest {
    
    @NotBlank(message = "가족 코드는 필수입니다")
    private String familyCode;
} 