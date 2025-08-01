package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.service.DailyQuestionService;
import com.steam.modeni.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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