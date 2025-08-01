package com.steam.modeni.controller;

import com.steam.modeni.dto.AuthResponse;
import com.steam.modeni.dto.GetFamilyCodeResponse;
import com.steam.modeni.dto.JoinFamilyRequest;
import com.steam.modeni.dto.LoginRequest;
import com.steam.modeni.dto.SignupRequest;
import com.steam.modeni.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/get-family-code/{userId}")
    public ResponseEntity<?> getFamilyCode(@PathVariable Long userId) {
        try {
            GetFamilyCodeResponse response = authService.getFamilyCode(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/join-family/{userId}")
    public ResponseEntity<?> joinFamily(@PathVariable Long userId, @Valid @RequestBody JoinFamilyRequest request) {
        try {
            AuthResponse response = authService.joinFamily(userId, request);
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