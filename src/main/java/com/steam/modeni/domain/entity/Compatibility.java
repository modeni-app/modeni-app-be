package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "compatibility")
@Getter
@Setter
@NoArgsConstructor
public class Compatibility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type1_id")
    private TestType type1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type2_id")
    private TestType type2;
    
    @Column(columnDefinition = "TEXT")
    private String result;
} 