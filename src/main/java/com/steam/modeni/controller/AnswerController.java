package com.steam.modeni.controller;

import com.steam.modeni.dto.AnswerResponse;
import com.steam.modeni.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            Long questionId = Long.valueOf(request.get("question_id").toString());
            Long userId = Long.valueOf(request.get("user_id").toString());
            String content = (String) request.get("content");
            
            Map<String, Object> response = answerService.createAnswer(questionId, userId, content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<Map<String, String>> updateAnswer(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            Map<String, String> response = answerService.updateAnswer(id, content);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAnswer(@PathVariable Long id) {
        try {
            Map<String, String> response = answerService.deleteAnswer(id);
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
    
    @GetMapping("/today/{familyId}")
    public ResponseEntity<List<AnswerResponse>> getTodayAnswersForFamily(@PathVariable Long familyId) {
        try {
            List<AnswerResponse> answers = answerService.getTodayAnswersForFamily(familyId);
            return ResponseEntity.ok(answers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 