package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "test_types")
@Getter
@Setter
@NoArgsConstructor
public class TestType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private PersonalityTest test;
    
    private String name;
    
    @OneToOne(mappedBy = "testType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TypeResult typeResult;
    
    @OneToMany(mappedBy = "testType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestResult> testResults;
    
    @OneToMany(mappedBy = "type1", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Compatibility> compatibilities1;
    
    @OneToMany(mappedBy = "type2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Compatibility> compatibilities2;
    
    @OneToMany(mappedBy = "testType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Welfare> welfares;
} 