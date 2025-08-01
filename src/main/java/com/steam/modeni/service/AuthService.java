package com.steam.modeni.service;

import com.steam.modeni.config.JwtUtil;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.domain.enums.Region;
import com.steam.modeni.dto.AuthResponse;
import com.steam.modeni.dto.GetFamilyCodeResponse;
import com.steam.modeni.dto.JoinFamilyRequest;
import com.steam.modeni.dto.LoginRequest;
import com.steam.modeni.dto.SignupRequest;
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
        // 사용자ID 중복 확인
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("이미 사용중인 사용자ID입니다.");
        }
        
        // 사용자 생성
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getUsername()); // JSON의 username을 User의 name에 매핑
        user.setRole(request.getRole());
        // Region enum으로 변환 (displayName 기준)
        if (request.getRegion() != null && !request.getRegion().trim().isEmpty()) {
            try {
                user.setRegion(Region.fromDisplayName(request.getRegion()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        user.setAge(request.getAge());
        
        // 가족 코드 처리
        Long familyCode;
        if (request.getFamilyCode() != null && !request.getFamilyCode().trim().isEmpty()) {
            // 기존 가족 참여 - 가족 코드 파싱
            try {
                // "FAM39685B" 형태에서 숫자 부분만 추출하거나 전체를 해시로 변환
                familyCode = parseFamilyCode(request.getFamilyCode());
                
                // 해당 가족 코드를 사용하는 다른 사용자가 있는지 확인
                if (userRepository.findByFamilyCode(familyCode).isEmpty()) {
                    throw new RuntimeException("존재하지 않는 가족 코드입니다.");
                }
            } catch (Exception e) {
                throw new RuntimeException("잘못된 가족 코드 형식입니다.");
            }
        } else {
            // 새로운 가족 생성
            familyCode = generateFamilyCode();
        }
        
        user.setFamilyCode(familyCode);
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getId());
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getUserId(), 
                savedUser.getFamilyCode(), "회원가입이 성공적으로 완료되었습니다.");
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자ID입니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUserId(), 
                user.getFamilyCode(), "로그인이 성공적으로 완료되었습니다.");
    }
    
    public AuthResponse joinFamily(Long userId, JoinFamilyRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        // 가족 코드 유효성 확인
        Long familyCode = request.getFamilyCode();
        if (userRepository.findByFamilyCode(familyCode).isEmpty()) {
            throw new RuntimeException("존재하지 않는 가족 코드입니다.");
        }
        
        // 새 가족으로 이동
        user.setFamilyCode(familyCode);
        userRepository.save(user);
        
        // JWT 토큰 재생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        return new AuthResponse(token, user.getId(), user.getUserId(), 
                user.getFamilyCode(), "가족 참여가 성공적으로 완료되었습니다!");
    }
    
    public GetFamilyCodeResponse getFamilyCode(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        Long familyCode = user.getFamilyCode();
        return new GetFamilyCodeResponse(String.valueOf(familyCode), 
                "가족 코드입니다. 다른 가족 구성원들에게 공유해주세요!");
    }
    
    private Long generateFamilyCode() {
        Long code;
        do {
            code = System.currentTimeMillis() % 1000000L; // 6자리 숫자
        } while (!userRepository.findByFamilyCode(code).isEmpty());
        return code;
    }
    
    private Long parseFamilyCode(String familyCodeStr) {
        // "FAM39685B" 형태를 Long으로 변환
        // 숫자 부분만 추출하거나 해시 값으로 변환
        String numericPart = familyCodeStr.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) {
            // 숫자가 없으면 문자열 해시코드 사용
            return Math.abs((long) familyCodeStr.hashCode()) % 1000000L;
        }
        return Long.parseLong(numericPart);
    }
}