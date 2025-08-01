package com.steam.modeni.service;

import com.steam.modeni.config.JwtUtil;
import com.steam.modeni.domain.entity.User;
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
        
        // familyCode 필수 확인
        if (request.getFamilyCode() == null) {
            throw new RuntimeException("가족 코드는 필수입니다.");
        }
        
        // 사용자 생성
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getUsername()); // JSON의 username을 User의 name에 매핑
        user.setRole(request.getRole());
        user.setRegion(request.getRegion());
        user.setAge(request.getAge());
        
        // 프론트에서 받은 familyCode를 그대로 저장
        user.setFamilyCode(request.getFamilyCode());
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getId());
        
        AuthResponse response = new AuthResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setUserId(savedUser.getUserId());
        response.setRole(savedUser.getRole());
        response.setRegion(savedUser.getRegion());
        response.setAge(savedUser.getAge());
        response.setFamilyCode(savedUser.getFamilyCode());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setToken(token);
        response.setMessage("회원가입이 성공적으로 완료되었습니다.");
        
        return response;
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자ID입니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUserId(user.getUserId());
        response.setRole(user.getRole());
        response.setRegion(user.getRegion());
        response.setAge(user.getAge());
        response.setFamilyCode(user.getFamilyCode());
        response.setCreatedAt(user.getCreatedAt());
        response.setToken(token);
        response.setMessage("로그인이 성공적으로 완료되었습니다.");
        
        return response;
    }
    
    public AuthResponse joinFamily(Long userId, JoinFamilyRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        // 가족 코드 유효성 확인
        String familyCode = request.getFamilyCode();
        if (userRepository.findByFamilyCode(familyCode).isEmpty()) {
            throw new RuntimeException("존재하지 않는 가족 코드입니다.");
        }
        
        // 새 가족으로 이동
        user.setFamilyCode(familyCode);
        userRepository.save(user);
        
        // JWT 토큰 재생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getId());
        
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUserId(user.getUserId());
        response.setRole(user.getRole());
        response.setRegion(user.getRegion());
        response.setAge(user.getAge());
        response.setFamilyCode(user.getFamilyCode());
        response.setCreatedAt(user.getCreatedAt());
        response.setToken(token);
        response.setMessage("가족 참여가 성공적으로 완료되었습니다!");
        
        return response;
    }
    
    public GetFamilyCodeResponse getFamilyCode(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        String familyCode = user.getFamilyCode();
        return new GetFamilyCodeResponse(String.valueOf(familyCode), 
                "가족 코드입니다. 다른 가족 구성원들에게 공유해주세요!");
    }
    
    private String generateFamilyCode() {
        String code;
        do {
            code = String.valueOf(System.currentTimeMillis() % 1000000L); // 6자리 숫자 문자열
        } while (!userRepository.findByFamilyCode(code).isEmpty());
        return code;
    }
    
    private String parseFamilyCode(String familyCodeStr) {
    // "FAM39685B" 형태를 숫자 문자열로 변환
    String numericPart = familyCodeStr.replaceAll("[^0-9]", "");
    if (numericPart.isEmpty()) {
        // 숫자가 없으면 해시코드를 양수로 만들어 6자리 문자열로 반환
        int hashCode = Math.abs(familyCodeStr.hashCode()) % 1000000;
        return String.format("%06d", hashCode); // 항상 6자리로 맞춤
    }
    return numericPart;
}
}