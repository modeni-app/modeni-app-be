package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Family;
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
    public ResponseEntity<Family> createFamily(@RequestBody Map<String, String> request) {
        try {
            String familyCode = request.get("familyCode");
            String motto = request.get("motto");
            Family family = familyService.createFamily(familyCode, motto);
            return ResponseEntity.ok(family);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Family> getFamilyById(@PathVariable Long id) {
        try {
            Family family = familyService.getFamilyById(id);
            return ResponseEntity.ok(family);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Family> updateFamily(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String motto = request.get("motto");
            Family family = familyService.updateFamily(id, motto);
            return ResponseEntity.ok(family);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 