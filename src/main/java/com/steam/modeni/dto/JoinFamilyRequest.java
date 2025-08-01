package com.steam.modeni.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinFamilyRequest {
    
    @NotNull(message = "가족 코드는 필수입니다")
    private String familyCode;
}