package com.steam.modeni.domain.entity;

import com.steam.modeni.domain.enums.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "welfare_programs")
@Getter
@Setter
@NoArgsConstructor
public class WelfareProgram {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "organization")
    private String organization; // 주관 기관
    
    @Column(name = "category")
    private String category; // 카테고리 (문화, 교육, 상담, 취업, 의료 등)
    
    @Enumerated(EnumType.STRING)
    private Region targetCity; // 대상 지역
    
    @Column(name = "target_age_min")
    private Integer targetAgeMin; // 최소 연령
    
    @Column(name = "target_age_max")
    private Integer targetAgeMax; // 최대 연령
    
    @Column(name = "emotion_keywords")
    private String emotionKeywords; // 관련 감정 키워드 (콤마 구분)
    
    @Column(name = "application_url")
    private String applicationUrl; // 신청 URL
    
    @Column(name = "contact_number")
    private String contactNumber; // 연락처
    
    @Column(name = "target_description")
    private String targetDescription; // 대상 상세 설명
    
    @Column(name = "location")
    private String location; // 장소/위치
    
    @Column(name = "schedule")
    private String schedule; // 일정/기간
    
    @Column(name = "is_active")
    private Boolean isActive = true; // 활성화 여부
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "welfareProgram", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WelfareRecommendation> recommendations;
}