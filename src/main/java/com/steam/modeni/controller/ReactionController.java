package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Reaction;
import com.steam.modeni.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {
    
    private final ReactionService reactionService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReaction(@RequestBody Map<String, Object> request) {
        try {
            // 다양한 필드명 형태를 지원
            Long answerId = null;
            Long userId = null;
            
            // answerId 파싱 (answer_id 또는 answerId 지원)
            if (request.get("answerId") != null) {
                answerId = Long.valueOf(request.get("answerId").toString());
            } else if (request.get("answer_id") != null) {
                answerId = Long.valueOf(request.get("answer_id").toString());
            } else {
                throw new RuntimeException("answerId 또는 answer_id가 필요합니다.");
            }
            
            // userId 파싱 (user_id 또는 userId 지원)
            if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            } else if (request.get("user_id") != null) {
                userId = Long.valueOf(request.get("user_id").toString());
            } else {
                throw new RuntimeException("userId 또는 user_id가 필요합니다.");
            }
            
            String reactionType = (String) request.get("reaction_type");
            if (reactionType == null || reactionType.trim().isEmpty()) {
                reactionType = "LIKE"; // 기본값
            }
            
            Map<String, Object> response = reactionService.createReaction(answerId, userId, reactionType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 에러 메시지를 포함한 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Object> getReactionById(@PathVariable Long id) {
        try {
            Reaction reaction = reactionService.getReactionById(id);
            return ResponseEntity.ok(reaction);
        } catch (RuntimeException e) {
            // 에러 메시지를 포함한 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReaction(@PathVariable Long id) {
        try {
            Map<String, Object> response = reactionService.deleteReaction(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 에러 메시지를 포함한 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<Object> getReactionsByAnswer(@PathVariable Long answerId) {
        try {
            List<Reaction> reactions = reactionService.getReactionsByAnswer(answerId);
            return ResponseEntity.ok(reactions);
        } catch (RuntimeException e) {
            // 에러 메시지를 포함한 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}