package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Mission;
import com.steam.modeni.domain.entity.MissionReview;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionReviewRepository extends JpaRepository<MissionReview, Long> {
    
    // 특정 미션의 모든 후기 조회 (최신순)
    List<MissionReview> findByMissionOrderByCreatedAtDesc(Mission mission);
    
    // 특정 사용자의 특정 미션에 대한 후기 조회
    Optional<MissionReview> findByMissionAndUser(Mission mission, User user);
    
    // 특정 미션에 대한 후기 개수 조회
    long countByMission(Mission mission);
    
    // 특정 사용자가 작성한 모든 미션 후기 조회
    List<MissionReview> findByUserOrderByCreatedAtDesc(User user);
    
    // 특정 미션에 후기를 작성한 사용자 수 조회
    @Query("SELECT COUNT(DISTINCT mr.user) FROM MissionReview mr WHERE mr.mission = :mission")
    long countDistinctUsersByMission(@Param("mission") Mission mission);
    
    // 특정 사용자가 특정 미션에 후기를 작성했는지 확인
    boolean existsByMissionAndUser(Mission mission, User user);
}
