package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.User;
import com.steam.modeni.domain.entity.UserDailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyQuestionRepository extends JpaRepository<UserDailyQuestion, Long> {
    
    // 특정 사용자의 모든 일일 질문 이력 조회 (날짜순 정렬)
    List<UserDailyQuestion> findByUserOrderByQuestionDateAsc(User user);
    
    // 특정 사용자의 특정 날짜 질문 조회
    Optional<UserDailyQuestion> findByUserAndQuestionDate(User user, LocalDate questionDate);
    
    // 특정 사용자의 질문 이력 개수 조회
    long countByUser(User user);
    
    // 특정 사용자의 최신 질문 조회
    Optional<UserDailyQuestion> findTopByUserOrderByQuestionDateDesc(User user);
    
    // 특정 사용자의 특정 기간 질문 이력 조회
    @Query("SELECT udq FROM UserDailyQuestion udq WHERE udq.user = :user " +
           "AND udq.questionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY udq.questionDate ASC")
    List<UserDailyQuestion> findByUserAndDateRange(@Param("user") User user, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    // 특정 날짜에 질문을 받은 모든 사용자 조회
    List<UserDailyQuestion> findByQuestionDate(LocalDate questionDate);
}
