package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_questions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_date"}))
@Getter
@Setter
@NoArgsConstructor
public class UserDailyQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "question_date", nullable = false)
    private LocalDate questionDate;
    
    @Column(name = "day_number", nullable = false)
    private Integer dayNumber; // 가입 후 몇 번째 날인지 (1, 2, 3, ...)
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public UserDailyQuestion(User user, Question question, LocalDate questionDate, Integer dayNumber) {
        this.user = user;
        this.question = question;
        this.questionDate = questionDate;
        this.dayNumber = dayNumber;
    }
}
