package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.MissionReview;
import com.steam.modeni.service.MissionReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mission-reviews")
@RequiredArgsConstructor
public class MissionReviewController {
    
    private final MissionReviewService missionReviewService;
    
    /**
     * 미션 후기 작성
     */
    @PostMapping
    public ResponseEntity<Object> createReview(@RequestBody Map<String, Object> request) {
        try {
            // 요청 데이터 추출 (유연한 필드명 지원)
            Long missionId = Long.valueOf(request.getOrDefault("missionId", 
                    request.getOrDefault("mission_id", "")).toString());
            Long userId = Long.valueOf(request.getOrDefault("userId", 
                    request.getOrDefault("user_id", "")).toString());
            String content = (String) request.getOrDefault("content", "");
            
            Map<String, Object> response = missionReviewService.createReview(missionId, userId, content);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 미션 후기 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Object> updateReview(@PathVariable Long reviewId, 
                                             @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.getOrDefault("userId", 
                    request.getOrDefault("user_id", "")).toString());
            String content = (String) request.getOrDefault("content", "");
            
            Map<String, Object> response = missionReviewService.updateReview(reviewId, userId, content);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 미션 후기 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Object> deleteReview(@PathVariable Long reviewId, 
                                             @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.getOrDefault("userId", 
                    request.getOrDefault("user_id", "")).toString());
            
            Map<String, Object> response = missionReviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 미션의 모든 후기 조회
     */
    @GetMapping("/mission/{missionId}")
    public ResponseEntity<Object> getReviewsByMission(@PathVariable Long missionId) {
        try {
            List<MissionReview> reviews = missionReviewService.getReviewsByMission(missionId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 사용자의 모든 미션 후기 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getReviewsByUser(@PathVariable Long userId) {
        try {
            List<MissionReview> reviews = missionReviewService.getReviewsByUser(userId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 후기 상세 조회
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<Object> getReviewById(@PathVariable Long reviewId) {
        try {
            MissionReview review = missionReviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
