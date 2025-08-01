package com.steam.modeni.service;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.UserResponse;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return convertToUserResponse(user);
    }
    
    public Map<String, String> updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 업데이트 가능한 필드들만 처리
        if (updates.containsKey("city")) {
            user.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("district")) {
            user.setDistrict((String) updates.get("district"));
        }
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updates.get("phoneNumber"));
        }
        if (updates.containsKey("age")) {
            user.setAge((Integer) updates.get("age"));
        }
        
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "사용자 정보가 성공적으로 수정되었습니다.");
        return response;
    }
    
    public Map<String, String> deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        userRepository.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "사용자가 성공적으로 삭제되었습니다.");
        return response;
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setCity(user.getCity());
        response.setDistrict(user.getDistrict());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        
        if (user.getFamily() != null) {
            response.setFamilyId(user.getFamily().getId());
            response.setFamilyCode(user.getFamily().getFamilyCode());
        }
        
        return response;
    }
} 