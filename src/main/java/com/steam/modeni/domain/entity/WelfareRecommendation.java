package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "welfare_recommendations")
@Getter
@Setter
@NoArgsConstructor
public class WelfareRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "welfare_program_id", nullable = false)
    private WelfareProgram welfareProgram;
    
    @Column(name = "recommendation_score")
    private Double recommendationScore; // 추천 점수 (0.0 ~ 1.0)
    
    @Column(name = "analysis_keywords")
    private String analysisKeywords; // 분석된 키워드
    
    @Column(name = "emotion_analysis")
    private String emotionAnalysis; // 감정 분석 결과
    
    @Column(name = "reason")
    private String reason; // 추천 이유
    
    @Column(name = "is_clicked")
    private Boolean isClicked = false; // 클릭 여부
    
    @Column(name = "is_applied")
    private Boolean isApplied = false; // 신청 여부
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime clickedAt;
    
    private LocalDateTime appliedAt;
}