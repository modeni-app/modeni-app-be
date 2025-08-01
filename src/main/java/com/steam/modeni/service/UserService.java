package com.steam.modeni.service;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.UserResponse;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByFamilyCode(String familyCode) {
        List<User> users = userRepository.findByFamilyCode(familyCode);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return convertToUserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return convertToUserResponse(user);
    }
    
    public Map<String, String> updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 업데이트 가능한 필드들
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("role")) {
            user.setRole((String) updates.get("role"));
        }
        if (updates.containsKey("region")) {
            user.setRegion((String) updates.get("region"));
        }
        if (updates.containsKey("age")) {
            user.setAge((String) updates.get("age"));
        }
        
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "사용자 정보가 성공적으로 업데이트되었습니다.");
        return response;
    }
    
    public Map<String, String> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        userRepository.delete(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "사용자가 성공적으로 삭제되었습니다.");
        return response;
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUserId(user.getUserId());
        response.setRole(user.getRole());
        response.setRegion(user.getRegion());
        response.setAge(user.getAge());
        response.setFamilyCode(user.getFamilyCode());
        response.setCreatedAt(user.getCreatedAt());
        
        return response;
    }
}