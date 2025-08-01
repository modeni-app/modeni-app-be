package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.MissionReview;
import com.steam.modeni.domain.entity.MissionReviewReaction;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionReviewReactionRepository extends JpaRepository<MissionReviewReaction, Long> {
    
    // 특정 미션 후기의 모든 반응 조회
    List<MissionReviewReaction> findByMissionReview(MissionReview missionReview);
    
    // 특정 사용자가 특정 미션 후기에 남긴 반응 조회
    Optional<MissionReviewReaction> findByMissionReviewAndUser(MissionReview missionReview, User user);
    
    // 특정 미션 후기에 대한 반응 개수 조회
    long countByMissionReview(MissionReview missionReview);
    
    // 특정 사용자가 특정 미션 후기에 반응했는지 확인
    boolean existsByMissionReviewAndUser(MissionReview missionReview, User user);
    
    // 특정 사용자가 남긴 모든 미션 후기 반응 조회
    List<MissionReviewReaction> findByUserOrderByCreatedAtDesc(User user);
}
