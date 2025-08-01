package com.steam.modeni.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.steam.modeni.domain.enums.City;
import com.steam.modeni.domain.enums.FamilyRole;
import com.steam.modeni.domain.enums.PersonalityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private FamilyRole role;
    
    // 기타 역할일 때 사용 (삼촌, 고모, 할머니 등)
    private String customRole;
    
    @Enumerated(EnumType.STRING)
    private City city;
    
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private PersonalityType personalityType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Answer> answers;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reaction> reactions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Diary> diaries;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestResult> testResults;
    
    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Compatibility> compatibilities1;
    
    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Compatibility> compatibilities2;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MissionCheck> missionChecks;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Inquiry> inquiries;
    
    // 편의 메서드들
    public Long getFamilyCode() {
        return family != null ? Long.parseLong(family.getFamilyCode()) : null;
    }
    
    public Long getUserId() {
        return this.id;
    }
} 