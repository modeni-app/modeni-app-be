package com.steam.modeni.controller;


import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.WelfareRecommendationResponse;
import com.steam.modeni.service.UserService;
import com.steam.modeni.service.WelfareRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/welfare")
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

    // 클릭/신청 확인 기능 제거 - 간단한 복지 정보 제공에 집중

    @PostMapping("/analyze-emotion")
    public ResponseEntity<?> analyzeEmotionAndRecommend(@RequestBody Map<String, String> request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotionText = request.get("text");
            if (emotionText == null || emotionText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("감정 텍스트가 필요합니다.");
            }

            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> recommendations = 
                welfareRecommendationService.analyzeEmotionAndRecommend(user, emotionText);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "totalCount", recommendations.size(),
                "message", "감정 분석 기반 추천이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("감정 분석 기반 추천 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("감정 분석 기반 추천에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/recommend-by-buttons")
    public ResponseEntity<?> recommendByButtons(@RequestBody Map<String, String> request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotion = request.get("emotion");
            String activity = request.get("activity");
            
            if (emotion == null || emotion.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("감정 정보가 필요합니다.");
            }

            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> recommendations = 
                welfareRecommendationService.recommendByButtons(user, emotion, activity);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "totalCount", recommendations.size(),
                "message", "버튼 기반 추천이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("버튼 기반 추천 요청 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("버튼 기반 추천에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations/{id}")
    public ResponseEntity<?> getRecommendationDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            WelfareRecommendationResponse recommendation = 
                welfareRecommendationService.getRecommendationDetail(id, user);
            
            return ResponseEntity.ok(Map.of(
                "recommendation", recommendation
            ));
        } catch (Exception e) {
            log.error("추천 상세 조회 실패: {}", e.getMessage());
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
            List<WelfareRecommendationResponse> programs = 
                welfareRecommendationService.searchPrograms(user, keyword, category, age);
            
            return ResponseEntity.ok(Map.of(
                "programs", programs,
                "totalCount", programs.size()
            ));
        } catch (Exception e) {
            log.error("프로그램 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("프로그램 검색에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/get-personalized-recommendations")
    public ResponseEntity<?> getPersonalizedRecommendations(@RequestBody Map<String, String> request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String emotion = request.get("emotion");
            String activity = request.get("activity");
            String keyword = request.get("keyword");
            
            User user = userService.findByUserId(userDetails.getUsername());
            List<WelfareRecommendationResponse> recommendations = 
                welfareRecommendationService.getPersonalizedRecommendations(user, emotion, activity, keyword);
            
            return ResponseEntity.ok(Map.of(
                "recommendations", recommendations,
                "totalCount", recommendations.size(),
                "message", "개인화된 추천이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("개인화된 추천 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("개인화된 추천에 실패했습니다: " + e.getMessage());
        }
    }
    
    @PostMapping("/recommend-simple")
    public ResponseEntity<?> getSimpleRecommendations(@RequestBody Map<String, String> request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserId(userDetails.getUsername());
            
            // 간단한 복지 정보 카드 4개 생성
            List<Map<String, Object>> welfareCards = new ArrayList<>();
            
            // 예시 데이터 (실제로는 DB에서 가져오거나 서비스를 통해 계산)
            welfareCards.add(Map.of(
                "id", 1,
                "title", "청년 주거 안정 지원 사업",
                "target", "만 19-34세 무주택 청년",
                "location", "서울시 전역",
                "link", "https://youth.seoul.go.kr/site/main/content/youth_housing"
            ));
            
            welfareCards.add(Map.of(
                "id", 2,
                "title", "육아 종합 지원 서비스",
                "target", "영유아 자녀를 둔 가정",
                "location", "동작구 육아종합지원센터",
                "link", "https://www.dreamstart.go.kr/dognak/"
            ));
            
            welfareCards.add(Map.of(
                "id", 3,
                "title", "노인 건강 증진 프로그램",
                "target", "만 65세 이상 어르신",
                "location", "동작구 노인복지관",
                "link", "https://www.dongjak.go.kr/portal/main/contents.do?menuNo=200731"
            ));
            
            welfareCards.add(Map.of(
                "id", 4,
                "title", "취업 지원 교육 프로그램",
                "target", "구직 중인 청년 및 중장년",
                "location", "서울시 일자리센터",
                "link", "https://job.seoul.go.kr/"
            ));
            
            return ResponseEntity.ok(Map.of(
                "welfareCards", welfareCards,
                "totalCount", welfareCards.size(),
                "message", "복지 정보 카드가 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("복지 정보 카드 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("복지 정보 카드 생성에 실패했습니다: " + e.getMessage());
        }
    }
}