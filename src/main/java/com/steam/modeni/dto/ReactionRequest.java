package com.steam.modeni.dto;

import com.steam.modeni.domain.enums.ReactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReactionRequest {
    
    private ReactionType reactionType;
    
    public ReactionRequest(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
