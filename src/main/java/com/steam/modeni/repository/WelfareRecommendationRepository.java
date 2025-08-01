package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.domain.entity.WelfareRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WelfareRecommendationRepository extends JpaRepository<WelfareRecommendation, Long> {
    
    // 사용자별 추천 목록 조회 (최신순)
    List<WelfareRecommendation> findByUserOrderByCreatedAtDesc(User user);
    
    // 사용자별 최근 추천 목록 (상위 N개)
    List<WelfareRecommendation> findTop10ByUserOrderByCreatedAtDesc(User user);
    
    // 사용자별 미클릭 추천 목록
    List<WelfareRecommendation> findByUserAndIsClickedFalseOrderByRecommendationScoreDesc(User user);
    
    // 사용자별 특정 기간 추천 목록
    @Query("SELECT wr FROM WelfareRecommendation wr WHERE wr.user = :user AND " +
           "wr.createdAt >= :startDate AND wr.createdAt <= :endDate " +
           "ORDER BY wr.recommendationScore DESC")
    List<WelfareRecommendation> findByUserAndDateRange(@Param("user") User user, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    // 추천 점수 기준 상위 추천 목록
    List<WelfareRecommendation> findByUserOrderByRecommendationScoreDesc(User user);
    
    // 클릭률 통계용 쿼리
    @Query("SELECT COUNT(wr) FROM WelfareRecommendation wr WHERE wr.user = :user AND wr.isClicked = true")
    Long countClickedByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(wr) FROM WelfareRecommendation wr WHERE wr.user = :user")
    Long countTotalByUser(@Param("user") User user);
}