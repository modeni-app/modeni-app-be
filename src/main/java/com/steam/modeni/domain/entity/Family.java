package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "families")
@Getter
@Setter
@NoArgsConstructor
public class Family {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String familyCode;
    
    @Column(columnDefinition = "TEXT")
    private String motto;
    
    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<User> users;
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MissionCheck> missionChecks;
} 