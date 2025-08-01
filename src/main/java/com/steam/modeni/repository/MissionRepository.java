package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    
    // 특정 가족의 모든 미션 조회 (최신순)
    List<Mission> findByFamilyCodeOrderByCreatedAtDesc(Long familyCode);
    
    // 특정 가족의 완료된 미션들만 조회
    List<Mission> findByFamilyCodeAndIsCompletedTrueOrderByCompletedAtDesc(Long familyCode);
    
    // 특정 가족의 현재 진행 중인 미션들 조회
    List<Mission> findByFamilyCodeAndIsCompletedFalseOrderByAssignedAtDesc(Long familyCode);
    
    // 특정 주에 해당 가족에게 이미 미션이 지급되었는지 확인
    @Query("SELECT m FROM Mission m WHERE m.familyCode = :familyCode AND m.weekStartDate = :weekStartDate")
    Optional<Mission> findByFamilyCodeAndWeekStartDate(@Param("familyCode") Long familyCode, 
                                                       @Param("weekStartDate") LocalDateTime weekStartDate);
    
    // 특정 가족의 특정 미션 번호로 조회
    Optional<Mission> findByFamilyCodeAndMissionNumber(Long familyCode, Integer missionNumber);
}
