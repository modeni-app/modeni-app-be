package com.steam.modeni.service;

import com.steam.modeni.config.JwtUtil;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.*;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthResponse signup(SignupRequest request) {
        // 사용자 아이디 중복 확인
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("이미 사용중인 사용자 아이디입니다.");
        }
        
        // 사용자 생성
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setRole(request.getRole());
        user.setRegion(request.getRegion());
        // 회원가입 시에는 familyCode를 null로 설정
        user.setFamilyCode(null);
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getId());
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getUserId(), 
                savedUser.getFamilyCode(), "회원가입이 성공적으로 완료되었습니다.");
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 아이디입니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUserId(), 
                user.getFamilyCode(), "로그인이 성공적으로 완료되었습니다.");
    }
    
    public GenerateFamilyCodeResponse generateFamilyCode(Long userId, GenerateFamilyCodeRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        // 이미 가족 코드가 있는 경우
        if (user.getFamilyCode() != null) {
            throw new RuntimeException("이미 가족 코드가 할당된 사용자입니다.");
        }
        
        Long familyCode = request.getFamilyCode();
        
        // 가족 코드 중복 확인
        if (userRepository.existsByFamilyCode(familyCode)) {
            return new GenerateFamilyCodeResponse(familyCode, false, 
                    "이미 사용중인 가족 코드입니다. 다른 코드를 생성해주세요.");
        }
        
        // 가족 코드 할당
        user.setFamilyCode(familyCode);
        userRepository.save(user);
        
        return new GenerateFamilyCodeResponse(familyCode, true, 
                "가족 코드가 성공적으로 생성되었습니다!");
    }
    
    public AuthResponse joinFamily(Long userId, JoinFamilyRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        Long familyCode = request.getFamilyCode();
        
        // 가족 코드가 존재하는지 확인
        if (!userRepository.existsByFamilyCode(familyCode)) {
            throw new RuntimeException("존재하지 않는 가족 코드입니다.");
        }
        
        // 가족 코드 설정
        user.setFamilyCode(familyCode);
        userRepository.save(user);
        
        // JWT 토큰 재생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUserId(), 
                user.getFamilyCode(), "가족 참여가 성공적으로 완료되었습니다!");
    }
} 