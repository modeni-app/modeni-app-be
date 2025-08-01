package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisResult {
    private String primaryEmotion; // 주요 감정 (긍정/부정/중립)
    private Double emotionScore; // 감정 점수 (0.0 ~ 1.0)
    private List<String> keywords; // 추출된 키워드
    private String emotionCategory; // 세부 감정 분류 (스트레스, 우울, 행복, 불안 등)
    private String analysisText; // 분석 결과 텍스트
    private List<String> recommendedCategories; // 추천 카테고리
}