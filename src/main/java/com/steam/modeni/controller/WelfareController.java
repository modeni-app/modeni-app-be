package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.WelfareRecommendationResponse;
import com.steam.modeni.service.UserService;
import com.steam.modeni.service.WelfareRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/welfare")
@RequiredArgsConstructor
public class WelfareController {

    private final WelfareRecommendationService welfareRecommendationService;
    private final UserService userService;

    @GetMapping("/recommendations")
    public ResponseEntity<?> getUserRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> recommendations = 
                welfareRecommendationService.getUserRecommendations(user);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "totalCount", recommendations.size()
            ));
        } catch (Exception e) {
            log.error("복지 추천 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("추천 목록을 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations/unread")
    public ResponseEntity<?> getUnreadRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> recommendations = 
                welfareRecommendationService.getUnreadRecommendations(user);
            
            return ResponseEntity.ok(Map.of(
                "unreadRecommendations", recommendations,
                "unreadCount", recommendations.size()
            ));
        } catch (Exception e) {
            log.error("미읽음 복지 추천 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("미읽음 추천 목록을 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/recommendations/{id}/click")
    public ResponseEntity<?> markAsClicked(@PathVariable Long id, 
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            welfareRecommendationService.markAsClicked(id, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "클릭 기록이 저장되었습니다.",
                "recommendationId", id
            ));
        } catch (Exception e) {
            log.error("추천 클릭 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("클릭 처리에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/recommendations/{id}/apply")
    public ResponseEntity<?> markAsApplied(@PathVariable Long id, 
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            welfareRecommendationService.markAsApplied(id, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "신청 기록이 저장되었습니다.",
                "recommendationId", id
            ));
        } catch (Exception e) {
            log.error("추천 신청 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("신청 처리에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/analyze-emotion")
    public ResponseEntity<?> analyzeEmotionAndRecommend(@RequestBody Map<String, String> request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotionText = request.get("text");
            if (emotionText == null || emotionText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("감정 텍스트가 필요합니다.");
            }
            
            User user = userService.findByUserId(userDetails.getUsername());
            
            // 비동기로 감정 분석 및 추천 처리
            welfareRecommendationService.processEmotionAndRecommend(user, emotionText);
            
            return ResponseEntity.ok(Map.of(
                "message", "감정 분석 및 맞춤 복지 추천을 처리 중입니다. 잠시 후 추천 목록을 확인해주세요.",
                "userId", user.getId()
            ));
        } catch (Exception e) {
            log.error("감정 분석 요청 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("감정 분석 요청 처리에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/recommend-by-buttons")
    public ResponseEntity<?> recommendByButtons(@RequestBody Map<String, String> request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotionKeyword = request.get("emotionKeyword");
            String wishActivity = request.get("wishActivity");
            
            if (emotionKeyword == null || emotionKeyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("감정 키워드가 필요합니다.");
            }
            if (wishActivity == null || wishActivity.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("희망 활동이 필요합니다.");
            }
            
            User user = userService.findByUserId(userDetails.getUsername());
            
            // 비동기로 버튼 기반 추천 처리
            welfareRecommendationService.processButtonBasedRecommend(user, emotionKeyword, wishActivity);
            
            return ResponseEntity.ok(Map.of(
                "message", "버튼 기반 맞춤 복지 추천을 처리 중입니다. 잠시 후 추천 목록을 확인해주세요.",
                "userId", user.getId(),
                "emotionKeyword", emotionKeyword,
                "wishActivity", wishActivity
            ));
        } catch (Exception e) {
            log.error("버튼 기반 추천 요청 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("버튼 기반 추천 요청 처리에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations/{id}")
    public ResponseEntity<?> getRecommendationDetail(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> userRecommendations = 
                welfareRecommendationService.getUserRecommendations(user);
            
            WelfareRecommendationResponse recommendation = userRecommendations.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("해당 추천 정보를 찾을 수 없습니다."));
            
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            log.error("추천 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("추천 상세 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/programs/search")
    public ResponseEntity<?> searchPrograms(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) Integer age,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            
            // 검색 조건을 기반으로 프로그램 검색 (테스트용)
            Map<String, Object> searchResult = Map.of(
                "message", "동작구 도서관 문화 프로그램 검색 결과",
                "searchConditions", Map.of(
                    "keyword", keyword != null ? keyword : "전체",
                    "category", category != null ? category : "전체", 
                    "age", age != null ? age : user.getAge(),
                    "city", user.getRegion() != null ? user.getRegion().getDisplayName() : "서울시"
                ),
                "totalPrograms", "95개의 실제 동작구 도서관 프로그램 로드됨",
                "availableCategories", List.of("문화", "교육", "독서", "영어", "과학", "요리", "놀이", "가족", "예술", "역사")
            );
            
            return ResponseEntity.ok(searchResult);
        } catch (Exception e) {
            log.error("프로그램 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("프로그램 검색에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/get-personalized-recommendations")
    public ResponseEntity<?> getPersonalizedRecommendations(@RequestBody Map<String, String> request,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotionKeyword = request.get("emotionKeyword");
            String wishActivity = request.get("wishActivity");
            
            if (emotionKeyword == null || emotionKeyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("감정 키워드가 필요합니다.");
            }
            if (wishActivity == null || wishActivity.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("희망 활동이 필요합니다.");
            }
            
            User user = userService.findByUserId(userDetails.getUsername());
            
            // GPT 기반 개인화된 추천 이유 생성 옵션으로 추천 처리
            welfareRecommendationService.processButtonBasedRecommend(user, emotionKeyword, wishActivity, true);
            
            // 성향 정보 유무에 따른 메시지 조정
            String message;
            String personalityInfo;
            
            // 성향 테스트 기능 비활성화 상태
            message = "추천 카드를 생성 중입니다. GPT가 당신의 감정과 활동을 분석하여 추천 이유를 작성하고 있어요.";
            personalityInfo = "미설정";
            
            return ResponseEntity.ok(Map.of(
                "message", message,
                "userId", user.getId(),
                "emotionKeyword", emotionKeyword,
                "wishActivity", wishActivity,
                "personalityType", personalityInfo,
                "hasPersonalityType", null /* user.getPersonalityType() - 임시 비활성화 */ != null,
                "recommendationMode", null /* user.getPersonalityType() - 임시 비활성화 */ != null ? "고도화된 성향 기반 추천" : "감정 & 활동 기반 추천",
                "estimatedTime", "약 10-30초 후 추천 목록에서 확인 가능합니다."
            ));
        } catch (Exception e) {
            log.error("개인화된 추천 요청 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("개인화된 추천 요청 처리에 실패했습니다: " + e.getMessage());
        }
    }
}