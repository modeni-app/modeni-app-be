package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answer> answers;
    
    // 편의 메서드
    public void setFamilyCode(Long familyCode) {
        // Family 엔티티를 통해 설정하는 것이 더 적절하지만,
        // 임시로 이 메서드를 제공합니다.
        // 실제로는 Family 엔티티를 생성하거나 기존 Family를 찾아서 설정해야 합니다.
        // 현재는 빈 구현으로 두고, 필요시 Family 엔티티를 통해 설정하도록 합니다.
    }
} 