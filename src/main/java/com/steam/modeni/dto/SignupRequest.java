package com.steam.modeni.dto;

import com.steam.modeni.domain.enums.City;
import com.steam.modeni.domain.enums.FamilyRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    
    @NotNull(message = "역할은 필수입니다")
    private FamilyRole role;
    
    // 기타 역할일 때만 사용 (role이 OTHER일 때 필수)
    private String customRole;
    
    @NotNull(message = "시는 필수입니다")
    private City city;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "사용자명은 필수입니다")
    private String username;
    
    @Min(value = 1, message = "나이는 1 이상이어야 합니다")
    @Max(value = 100, message = "나이는 100 이하여야 합니다")
    private Integer age;
} 