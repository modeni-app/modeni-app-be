package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    
    // 사용자별 일기 목록 (최신순)
    List<Diary> findByUserOrderByCreatedAtDesc(User user);
    
    // 사용자별 오늘 일기
    @Query("SELECT d FROM Diary d WHERE d.user = :user AND " +
           "DATE(d.createdAt) = DATE(:today)")
    Optional<Diary> findTodayDiary(@Param("user") User user, @Param("today") LocalDateTime today);
    
    // 사용자별 특정 기간 일기
    @Query("SELECT d FROM Diary d WHERE d.user = :user AND " +
           "d.createdAt >= :startDate AND d.createdAt <= :endDate " +
           "ORDER BY d.createdAt DESC")
    List<Diary> findByUserAndDateRange(@Param("user") User user, 
                                      @Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // 특정 감정 키워드가 포함된 일기
    List<Diary> findByUserAndEmotionKeywordContainingOrderByCreatedAtDesc(User user, String emotionKeyword);
    
    // 가족 공유 기능을 위한 메서드
    List<Diary> findByUserInOrderByCreatedAtDesc(List<User> users);
}