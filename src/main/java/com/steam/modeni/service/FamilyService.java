package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Family;
import com.steam.modeni.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {
    
    private final FamilyRepository familyRepository;
    
    public Map<String, Object> createFamily(Map<String, String> request) {
        Family family = new Family();
        
        String familyCode = request.get("family_code");
        if (familyCode == null || familyCode.trim().isEmpty()) {
            familyCode = generateFamilyCode();
        } else {
            if (familyRepository.existsByFamilyCode(familyCode)) {
                throw new RuntimeException("이미 사용중인 가족 코드입니다.");
            }
        }
        
        family.setFamilyCode(familyCode);
        family.setMotto(request.getOrDefault("motto", "우리 가족을 위한 새로운 시작!"));
        
        Family savedFamily = familyRepository.save(family);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedFamily.getId());
        response.put("family_code", savedFamily.getFamilyCode());
        response.put("motto", savedFamily.getMotto());
        response.put("message", "가족이 성공적으로 생성되었습니다.");
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getFamilyById(Long id) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("가족을 찾을 수 없습니다."));
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", family.getId());
        response.put("family_code", family.getFamilyCode());
        response.put("motto", family.getMotto());
        
        return response;
    }
    
    public Map<String, String> updateFamily(Long id, Map<String, String> updates) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("가족을 찾을 수 없습니다."));
        
        if (updates.containsKey("motto")) {
            family.setMotto(updates.get("motto"));
        }
        
        familyRepository.save(family);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "가족 정보가 성공적으로 수정되었습니다.");
        return response;
    }
    
    private String generateFamilyCode() {
        String code;
        do {
            code = "FAM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (familyRepository.existsByFamilyCode(code));
        return code;
    }
} 