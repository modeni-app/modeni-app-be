package com.steam.modeni.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "type_results")
@Getter
@Setter
@NoArgsConstructor
public class TypeResult {
    
    @Id
    private Long typeId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "type_id")
    private TestType testType;
    
    @Column(columnDefinition = "TEXT")
    private String result;
} 