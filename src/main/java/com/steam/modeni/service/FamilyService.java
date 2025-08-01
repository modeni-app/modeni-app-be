package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Family;
import com.steam.modeni.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {
    
    private final FamilyRepository familyRepository;
    
    public Family createFamily(String familyCode, String motto) {
        Family family = new Family();
        
        if (familyCode == null || familyCode.trim().isEmpty()) {
            familyCode = generateFamilyCode();
        } else {
            if (familyRepository.existsByFamilyCode(familyCode)) {
                throw new RuntimeException("이미 사용중인 가족 코드입니다.");
            }
        }
        
        family.setFamilyCode(familyCode);
        family.setMotto(motto != null ? motto : "우리 가족을 위한 새로운 시작!");
        
        return familyRepository.save(family);
    }
    
    @Transactional(readOnly = true)
    public Family getFamilyById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("가족을 찾을 수 없습니다."));
    }
    
    public Family updateFamily(Long id, String motto) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("가족을 찾을 수 없습니다."));
        
        if (motto != null) {
            family.setMotto(motto);
        }
        
        return familyRepository.save(family);
    }
    
    private String generateFamilyCode() {
        String code;
        do {
            code = "FAM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (familyRepository.existsByFamilyCode(code));
        return code;
    }
} 