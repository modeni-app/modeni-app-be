package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyQuestionResponse {
    
    private Long id;
    private Integer dayNumber; // 가입 후 몇 번째 날
    private String questionContent; // 질문 내용
    private LocalDate questionDate; // 질문을 받은 날짜
    private LocalDateTime createdAt; // 생성 시간
    
    // 질문 정보
    private QuestionInfo question;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private Long questionId;
        private String content;
        private Long familyCode;
        private LocalDateTime createdAt;
    }
}
