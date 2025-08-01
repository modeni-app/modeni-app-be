package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByIdAsc();
    
    // 특정 가족 코드의 질문들 조회
    List<Question> findByFamilyCode(Long familyCode);
    
    // 시스템 질문 또는 특정 가족 질문 조회
    List<Question> findByFamilyCodeOrFamilyCode(Long systemFamilyCode, Long familyCode);
    
    // 가족 코드별로 정렬된 질문 조회
    List<Question> findByFamilyCodeOrderByIdAsc(Long familyCode);
}