package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.service.DailyQuestionService;
import com.steam.modeni.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    private final DailyQuestionService dailyQuestionService;
    
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            List<Question> questions = questionService.getAllQuestions();
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Question>> getQuestionsForUser(@PathVariable Long userId) {
        try {
            List<Question> questions = questionService.getQuestionsForUser(userId);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/family/{familyCode}")
    public ResponseEntity<List<Question>> getQuestionsForFamily(@PathVariable Long familyCode) {
        try {
            List<Question> questions = questionService.getQuestionsForFamily(familyCode);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/answered/family/{familyCode}")
    public ResponseEntity<Object> getAnsweredQuestionsByFamily(@PathVariable Long familyCode) {
        try {
            List<Question> questions = questionService.getAnsweredQuestionsByFamily(familyCode);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/answered/user/{userId}")
    public ResponseEntity<Object> getAnsweredQuestionsByUser(@PathVariable Long userId) {
        try {
            List<Question> questions = questionService.getAnsweredQuestionsByUser(userId);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/today/{familyId}")
    public ResponseEntity<Question> getTodayQuestionForFamily(@PathVariable Long familyId) {
        try {
            Question question = dailyQuestionService.getTodayQuestionForFamily(familyId);
            if (question == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/random/{familyId}")
    public ResponseEntity<Question> getRandomQuestion(@PathVariable Long familyId) {
        try {
            Question question = questionService.getRandomQuestionForFamily(familyId);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        try {
            Question question = questionService.getQuestionById(id);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 