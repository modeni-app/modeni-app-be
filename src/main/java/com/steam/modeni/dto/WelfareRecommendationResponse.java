package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WelfareRecommendationResponse {
    private Long id;
    private String title;
    private String description;
    private String organization;
    private String category;
    private String targetCity;
    private String applicationUrl;
    private String contactNumber;
    private Double recommendationScore;
    private String reason;
    private boolean isClicked;
    private boolean isApplied;
    private LocalDateTime createdAt;
    
    // 새로 추가된 상세 정보
    private String target; // 대상
    private String location; // 위치/장소
    private String schedule; // 기간/일정
    private String gptRecommendationReason; // GPT가 생성한 개인화된 추천 이유
}