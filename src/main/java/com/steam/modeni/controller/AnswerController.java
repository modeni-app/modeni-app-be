package com.steam.modeni.controller;

import com.steam.modeni.dto.AnswerResponse;
import com.steam.modeni.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {
    
    private final AnswerService answerService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAnswer(@RequestBody Map<String, Object> request) {
        try {
            // 다양한 필드명 형태를 지원
            Long questionId = null;
            Long userId = null;
            
            // questionId 파싱 (question_id 또는 questionId 지원)
            if (request.get("questionId") != null) {
                questionId = Long.valueOf(request.get("questionId").toString());
            } else if (request.get("question_id") != null) {
                questionId = Long.valueOf(request.get("question_id").toString());
            } else {
                throw new RuntimeException("questionId 또는 question_id가 필요합니다.");
            }
            
            // userId 파싱 (user_id 또는 userId 지원)
            if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            } else if (request.get("user_id") != null) {
                userId = Long.valueOf(request.get("user_id").toString());
            } else {
                throw new RuntimeException("userId 또는 user_id가 필요합니다.");
            }
            
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("content가 필요합니다.");
            }
            
            Map<String, Object> response = answerService.createAnswer(questionId, userId, content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 에러 메시지를 포함한 응답 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Long id) {
        try {
            AnswerResponse answer = answerService.getAnswerById(id);
            return ResponseEntity.ok(answer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAnswer(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            Map<String, Object> response = answerService.updateAnswer(id, content);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnswer(@PathVariable Long id) {
        try {
            Map<String, Object> response = answerService.deleteAnswer(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestion(@PathVariable Long questionId) {
        try {
            List<AnswerResponse> answers = answerService.getAnswersByQuestion(questionId);
            return ResponseEntity.ok(answers);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/today/{familyCode}")
    public ResponseEntity<List<AnswerResponse>> getTodayAnswersForFamily(@PathVariable String familyCode) {
        try {
            List<AnswerResponse> answers = answerService.getTodayAnswersForFamily(familyCode);
            return ResponseEntity.ok(answers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 