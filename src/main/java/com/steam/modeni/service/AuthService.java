package com.steam.modeni.service;

import com.steam.modeni.config.JwtUtil;
import com.steam.modeni.domain.entity.Family;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.AuthResponse;
import com.steam.modeni.dto.GetFamilyCodeResponse;
import com.steam.modeni.dto.JoinFamilyRequest;
import com.steam.modeni.dto.LoginRequest;
import com.steam.modeni.dto.SignupRequest;
import com.steam.modeni.repository.FamilyRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthResponse signup(SignupRequest request) {
        // 사용자명 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 사용중인 사용자명입니다.");
        }
        
        // 사용자 생성
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCity(request.getCity());
        user.setDistrict(request.getDistrict());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAge(request.getAge());
        
        // 회원가입 시 항상 개인 가족 생성 (1인 가족)
        Family newFamily = new Family();
        newFamily.setFamilyCode(generateFamilyCode());
        newFamily.setMotto("우리 가족을 위한 새로운 시작!");
        familyRepository.save(newFamily);
        user.setFamily(newFamily);
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId());
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), 
                savedUser.getFamily().getFamilyCode(), "회원가입이 성공적으로 완료되었습니다.");
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자명입니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUsername(), 
                user.getFamily().getFamilyCode(), "로그인이 성공적으로 완료되었습니다.");
    }
    
    public GetFamilyCodeResponse getFamilyCode(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        String familyCode = user.getFamily().getFamilyCode();
        return new GetFamilyCodeResponse(familyCode, 
                "가족 코드입니다. 다른 가족 구성원들에게 공유해주세요!");
    }
    
    public AuthResponse joinFamily(Long userId, JoinFamilyRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        // 기존 가족에 참여
        Family targetFamily = familyRepository.findByFamilyCode(request.getFamilyCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 가족 코드입니다."));
        
        // 기존 1인 가족 처리 (다른 멤버가 없는 경우에만 삭제)
        Family currentFamily = user.getFamily();
        long memberCount = userRepository.countByFamily(currentFamily);
        
        // 새 가족으로 이동
        user.setFamily(targetFamily);
        userRepository.save(user);
        
        // 기존 가족이 1인 가족이었다면 삭제 (이미 사용자는 다른 가족으로 이동했으므로 안전)
        if (memberCount == 1) {
            familyRepository.delete(currentFamily);
        }
        
        // JWT 토큰 재생성
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUsername(), 
                targetFamily.getFamilyCode(), "가족 참여가 성공적으로 완료되었습니다!");
    }
    
    private String generateFamilyCode() {
        String code;
        do {
            code = "FAM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (familyRepository.existsByFamilyCode(code));
        return code;
    }
} 