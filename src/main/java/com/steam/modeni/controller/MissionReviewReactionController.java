package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.MissionReviewReaction;
import com.steam.modeni.service.MissionReviewReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mission-review-reactions")
@RequiredArgsConstructor
public class MissionReviewReactionController {
    
    private final MissionReviewReactionService reactionService;
    
    /**
     * 미션 후기에 반응(칭찬) 토글
     */
    @PostMapping
    public ResponseEntity<Object> toggleReaction(@RequestBody Map<String, Object> request) {
        try {
            // 요청 데이터 추출 (유연한 필드명 지원)
            Long reviewId = Long.valueOf(request.getOrDefault("reviewId", 
                    request.getOrDefault("review_id", "")).toString());
            Long userId = Long.valueOf(request.getOrDefault("userId", 
                    request.getOrDefault("user_id", "")).toString());
            String reactionType = (String) request.getOrDefault("reactionType", 
                    request.getOrDefault("reaction_type", "LIKE"));
            
            Map<String, Object> response = reactionService.toggleReaction(reviewId, userId, reactionType);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 후기의 모든 반응 조회
     */
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Object> getReactionsByReview(@PathVariable Long reviewId) {
        try {
            List<MissionReviewReaction> reactions = reactionService.getReactionsByReview(reviewId);
            return ResponseEntity.ok(reactions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 사용자의 모든 미션 후기 반응 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getReactionsByUser(@PathVariable Long userId) {
        try {
            List<MissionReviewReaction> reactions = reactionService.getReactionsByUser(userId);
            return ResponseEntity.ok(reactions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 후기에 대한 반응 수 조회
     */
    @GetMapping("/count/{reviewId}")
    public ResponseEntity<Object> getReactionCount(@PathVariable Long reviewId) {
        try {
            long count = reactionService.getReactionCount(reviewId);
            Map<String, Object> response = new HashMap<>();
            response.put("reviewId", reviewId);
            response.put("reactionCount", count);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
