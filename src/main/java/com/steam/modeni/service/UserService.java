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
        if (updates.containsKey("region")) {
            user.setRegion((String) updates.get("region"));
        }

        if (updates.containsKey("age")) {
            user.setAge((String) updates.get("age"));
        }
        
        if (updates.containsKey("role")) {
            user.setRole((String) updates.get("role"));
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
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setRole(user.getRole());
        response.setRegion(user.getRegion());
        response.setAge(user.getAge());
        response.setFamilyCode(user.getFamilyCode());
        response.setCreatedAt(user.getCreatedAt());
        
        return response;
    }
} 