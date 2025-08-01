package com.steam.modeni.dto;

import com.steam.modeni.domain.enums.ReactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReactionResponse {
    
    private Long id;
    private String userName;
    private ReactionType reactionType;
    private LocalDateTime createdAt;
    
    public ReactionResponse(Long id, String userName, ReactionType reactionType, LocalDateTime createdAt) {
        this.id = id;
        this.userName = userName;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }
}
