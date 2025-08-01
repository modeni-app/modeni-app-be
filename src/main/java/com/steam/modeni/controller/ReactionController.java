package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Reaction;
import com.steam.modeni.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {
    
    private final ReactionService reactionService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReaction(@RequestBody Map<String, Object> request) {
        try {
            Long answerId = Long.valueOf(request.get("answer_id").toString());
            Long userId = Long.valueOf(request.get("user_id").toString());
            String reactionType = (String) request.get("reaction_type");
            
            Map<String, Object> response = reactionService.createReaction(answerId, userId, reactionType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Reaction> getReactionById(@PathVariable Long id) {
        try {
            Reaction reaction = reactionService.getReactionById(id);
            return ResponseEntity.ok(reaction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReaction(@PathVariable Long id) {
        try {
            Map<String, String> response = reactionService.deleteReaction(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<List<Reaction>> getReactionsByAnswer(@PathVariable Long answerId) {
        try {
            List<Reaction> reactions = reactionService.getReactionsByAnswer(answerId);
            return ResponseEntity.ok(reactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 