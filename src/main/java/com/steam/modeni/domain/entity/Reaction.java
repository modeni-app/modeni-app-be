package com.steam.modeni.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.steam.modeni.domain.enums.ReactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reactions")
@Getter
@Setter
@NoArgsConstructor
public class Reaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Answer와 연결 (기존 기능 유지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    @JsonIgnore
    private Answer answer;
    
    // Diary와 연결 (새로운 기능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    @JsonIgnore
    private Diary diary;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 편의 메서드: Answer 리액션인지 확인
    public boolean isAnswerReaction() {
        return answer != null;
    }
    
    // 편의 메서드: Diary 리액션인지 확인
    public boolean isDiaryReaction() {
        return diary != null;
    }
}