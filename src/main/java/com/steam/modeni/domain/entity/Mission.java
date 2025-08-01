package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long familyCode;
    
    @Column(nullable = false)
    private Integer missionNumber; // 1-8 중 하나
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime assignedAt; // 미션 지급 시간
    
    @Column(nullable = false)
    private LocalDateTime weekStartDate; // 해당 주의 시작일 (월요일)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false; // 모든 가족 구성원이 후기 작성 완료 여부
    
    @Column
    private LocalDateTime completedAt; // 미션 완료 시간
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
