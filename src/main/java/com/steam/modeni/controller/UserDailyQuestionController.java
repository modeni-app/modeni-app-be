package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.UserDailyQuestion;
import com.steam.modeni.dto.InitialQuestionResponse;
import com.steam.modeni.dto.UserDailyQuestionResponse;
import com.steam.modeni.service.QuestionInitService;
import com.steam.modeni.service.UserDailyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-daily-questions")
@RequiredArgsConstructor
public class UserDailyQuestionController {
    
    private final UserDailyQuestionService userDailyQuestionService;
    private final QuestionInitService questionInitService;
    
    /**
     * 사용자의 오늘 질문 조회
     */
    @GetMapping("/today/{userId}")
    public ResponseEntity<UserDailyQuestionResponse> getTodayQuestionForUser(@PathVariable Long userId) {
        try {
            UserDailyQuestion todayQuestion = userDailyQuestionService.getTodayQuestionForUser(userId);
            UserDailyQuestionResponse response = convertToResponse(todayQuestion);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 사용자의 모든 질문 이력 조회
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<UserDailyQuestionResponse>> getQuestionHistoryForUser(@PathVariable Long userId) {
        try {
            List<UserDailyQuestion> history = userDailyQuestionService.getQuestionHistoryForUser(userId);
            List<UserDailyQuestionResponse> responses = history.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 가족별 질문 이력 조회 (효율적 - 같은 가족은 동일한 질문을 받음)
     */
    @GetMapping("/history/family/{familyCode}")
    public ResponseEntity<List<UserDailyQuestionResponse>> getQuestionHistoryForFamily(@PathVariable String familyCode) {
        try {
            List<UserDailyQuestion> history = userDailyQuestionService.getQuestionHistoryForFamily(familyCode);
            List<UserDailyQuestionResponse> responses = history.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 사용자의 특정 기간 질문 이력 조회
     */
    @GetMapping("/history/{userId}/range")
    public ResponseEntity<List<UserDailyQuestionResponse>> getQuestionHistoryByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<UserDailyQuestion> history = userDailyQuestionService
                    .getQuestionHistoryForUserByDateRange(userId, startDate, endDate);
            List<UserDailyQuestionResponse> responses = history.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 가족별 특정 기간 질문 이력 조회
     */
    @GetMapping("/history/family/{familyCode}/range")
    public ResponseEntity<List<UserDailyQuestionResponse>> getQuestionHistoryForFamilyByDateRange(
            @PathVariable String familyCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<UserDailyQuestion> history = userDailyQuestionService
                    .getQuestionHistoryForFamilyByDateRange(familyCode, startDate, endDate);
            List<UserDailyQuestionResponse> responses = history.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 사용자의 누락된 질문들 생성 (가입일~오늘까지)
     */
    @PostMapping("/generate-missing/{userId}")
    public ResponseEntity<Map<String, Object>> generateMissingQuestionsForUser(@PathVariable Long userId) {
        try {
            userDailyQuestionService.generateMissingQuestionsForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "누락된 질문들이 성공적으로 생성되었습니다.");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 가족의 누락된 질문들 생성 (가족 중 가장 먼저 가입한 사용자 기준)
     */
    @PostMapping("/generate-missing/family/{familyCode}")
    public ResponseEntity<Map<String, Object>> generateMissingQuestionsForFamily(@PathVariable String familyCode) {
        try {
            userDailyQuestionService.generateMissingQuestionsForFamily(familyCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "가족의 누락된 질문들이 성공적으로 생성되었습니다.");
            response.put("familyCode", familyCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 초기 질문 목록 조회 (id와 content 포함)
     */
    @GetMapping("/initial-questions")
    public ResponseEntity<List<InitialQuestionResponse>> getInitialQuestions() {
        try {
            List<String> initialQuestions = questionInitService.getInitialQuestions();
            List<InitialQuestionResponse> responses = new ArrayList<>();
            
            for (int i = 0; i < initialQuestions.size(); i++) {
                responses.add(new InitialQuestionResponse(i + 1, initialQuestions.get(i)));
            }
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * UserDailyQuestion을 UserDailyQuestionResponse로 변환
     */
    private UserDailyQuestionResponse convertToResponse(UserDailyQuestion userDailyQuestion) {
        UserDailyQuestionResponse response = new UserDailyQuestionResponse();
        response.setId(userDailyQuestion.getId());
        response.setDayNumber(userDailyQuestion.getDayNumber());
        response.setQuestionContent(userDailyQuestion.getQuestion().getContent());
        response.setQuestionDate(userDailyQuestion.getQuestionDate());
        response.setCreatedAt(userDailyQuestion.getCreatedAt());
        
        // 질문 정보 설정 (실제 가족 코드로 변환)
        UserDailyQuestionResponse.QuestionInfo questionInfo = 
                new UserDailyQuestionResponse.QuestionInfo();
        questionInfo.setQuestionId(userDailyQuestion.getQuestion().getId());
        questionInfo.setContent(userDailyQuestion.getQuestion().getContent());
        questionInfo.setFamilyCode(userDailyQuestion.getUser().getFamilyCode()); // 실제 가족 코드
        questionInfo.setCreatedAt(userDailyQuestion.getQuestion().getCreatedAt());
        
        response.setQuestion(questionInfo);
        
        return response;
    }
}
