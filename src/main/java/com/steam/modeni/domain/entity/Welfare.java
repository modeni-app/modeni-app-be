package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "welfares")
@Getter
@Setter
@NoArgsConstructor
public class Welfare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private TestType testType;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
} 