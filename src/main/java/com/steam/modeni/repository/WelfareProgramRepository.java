package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.WelfareProgram;
import com.steam.modeni.domain.enums.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareProgramRepository extends JpaRepository<WelfareProgram, Long> {
    
    // 활성화된 프로그램만 조회
    List<WelfareProgram> findByIsActiveTrue();
    
    // 지역별 프로그램 조회
    List<WelfareProgram> findByTargetCityAndIsActiveTrue(City city);
    
    // 연령대별 프로그램 조회
    @Query("SELECT w FROM WelfareProgram w WHERE w.isActive = true AND " +
           "(w.targetAgeMin IS NULL OR w.targetAgeMin <= :age) AND " +
           "(w.targetAgeMax IS NULL OR w.targetAgeMax >= :age)")
    List<WelfareProgram> findByAgeRange(@Param("age") Integer age);
    
    // 지역과 연령대로 필터링
    @Query("SELECT w FROM WelfareProgram w WHERE w.isActive = true AND " +
           "w.targetCity = :city AND " +
           "(w.targetAgeMin IS NULL OR w.targetAgeMin <= :age) AND " +
           "(w.targetAgeMax IS NULL OR w.targetAgeMax >= :age)")
    List<WelfareProgram> findByCityAndAgeRange(@Param("city") City city, @Param("age") Integer age);
    
    // 감정 키워드로 검색
    @Query("SELECT w FROM WelfareProgram w WHERE w.isActive = true AND " +
           "w.emotionKeywords LIKE %:keyword%")
    List<WelfareProgram> findByEmotionKeywordContaining(@Param("keyword") String keyword);
    
    // 카테고리별 프로그램 조회
    List<WelfareProgram> findByCategoryAndIsActiveTrue(String category);
    
    // 복합 검색 (지역, 연령, 감정키워드)
    @Query("SELECT w FROM WelfareProgram w WHERE w.isActive = true AND " +
           "w.targetCity = :city AND " +
           "(w.targetAgeMin IS NULL OR w.targetAgeMin <= :age) AND " +
           "(w.targetAgeMax IS NULL OR w.targetAgeMax >= :age) AND " +
           "w.emotionKeywords LIKE %:keyword%")
    List<WelfareProgram> findByComplexCriteria(@Param("city") City city, 
                                              @Param("age") Integer age, 
                                              @Param("keyword") String keyword);
}