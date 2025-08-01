package com.steam.modeni.controller;

import com.steam.modeni.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
public class FamilyController {
    
    private final FamilyService familyService;
    
    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody Map<String, String> request) {
        try {
            Map<String, Object> response = familyService.createFamily(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFamilyById(@PathVariable Long id) {
        try {
            Map<String, Object> response = familyService.getFamilyById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFamily(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            Map<String, String> response = familyService.updateFamily(id, updates);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 에러 응답용 내부 클래스
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 