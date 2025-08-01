package com.steam.modeni.controller;

import com.steam.modeni.dto.UserResponse;
import com.steam.modeni.domain.enums.PersonalityType;
import com.steam.modeni.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> updates) {
        try {
            Map<String, String> response = userService.updateUser(id, updates);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        try {
            Map<String, String> response = userService.deleteUser(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/personality")
    public ResponseEntity<?> setPersonalityType(@PathVariable Long id, 
                                               @RequestBody Map<String, String> request) {
        try {
            String personalityTypeStr = request.get("personalityType");
            if (personalityTypeStr == null) {
                return ResponseEntity.badRequest().body("성향 정보가 필요합니다.");
            }
            
            PersonalityType personalityType;
            try {
                personalityType = PersonalityType.valueOf(personalityTypeStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("유효하지 않은 성향 타입입니다.");
            }
            
            Map<String, String> response = userService.setPersonalityType(id, personalityType);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/personality-types")
    public ResponseEntity<?> getPersonalityTypes() {
        Map<String, Object> personalityTypes = Map.of(
            "LOGICAL_BLUE", Map.of(
                "name", "이성적 분석형",
                "nickname", "파랑이", 
                "description", "감정보다는 논리 중심, 갈등을 해결하려 함",
                "icon", "🧠"
            ),
            "EMOTIONAL_RED", Map.of(
                "name", "감정 공감형",
                "nickname", "빨강이",
                "description", "정 교류 중시, 상처에도 예민",
                "icon", "❤️"
            ),
            "CONTROL_GRAY", Map.of(
                "name", "통제 보호형",
                "nickname", "회색이",
                "description", "통제, 지도에 익숙하고 보호욕 강함",
                "icon", "👮‍♀️"
            ),
            "INDEPENDENT_NAVY", Map.of(
                "name", "자율 독립형",
                "nickname", "남색이",
                "description", "자기 선택을 중요시하고 간섭을 싫어함",
                "icon", "🕊"
            ),
            "AFFECTIONATE_YELLOW", Map.of(
                "name", "애정 표현형",
                "nickname", "노랑이",
                "description", "자주 표현하고 스킨십/말로 사랑을 전달",
                "icon", "🧸"
            ),
            "INTROSPECTIVE_GREEN", Map.of(
                "name", "내면형",
                "nickname", "초록이",
                "description", "표현은 적지만 속은 깊음, 혼자 해결하려 함",
                "icon", "🔒"
            )
        );
        
        return ResponseEntity.ok(personalityTypes);
    }
} 