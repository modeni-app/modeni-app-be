package com.steam.modeni.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    
    @NotBlank(message = "사용자ID는 필수입니다")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    private String username; // 실제 이름 (User.name 필드에 매핑됨)
    
    @NotBlank(message = "나이는 필수입니다")
    private String age;
    
    @NotBlank(message = "역할은 필수입니다")
    private String role;
    
    @NotBlank(message = "지역은 필수입니다")
    private String region;
    
    // 필수 필드 - 프론트에서 생성한 가족 코드
    @NotNull(message = "가족 코드는 필수입니다")
    private String familyCode;
}