package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionOrderByCreatedAtAsc(Question question);
    boolean existsByQuestionAndUser(Question question, User user);
    
    // 특정 사용자가 답변한 모든 답변 조회
    List<Answer> findByUserOrderByCreatedAtDesc(User user);
    
    // 특정 가족 코드의 사용자들이 답변한 질문들을 중복 없이 조회 (MySQL 호환)
    @Query("SELECT q FROM Question q WHERE q.id IN " +
           "(SELECT DISTINCT a.question.id FROM Answer a JOIN a.user u WHERE u.familyCode = :familyCode) " +
           "ORDER BY q.id DESC")
    List<Question> findDistinctQuestionsByFamilyCode(@Param("familyCode") String familyCode);
    
    // 특정 사용자가 답변한 질문들을 중복 없이 조회 (MySQL 호환)
    @Query("SELECT q FROM Question q WHERE q.id IN " +
           "(SELECT DISTINCT a.question.id FROM Answer a WHERE a.user = :user) " +
           "ORDER BY q.id DESC")
    List<Question> findDistinctQuestionsByUser(@Param("user") User user);
    
    // 특정 가족의 특정 질문에 대한 모든 답변 조회
    @Query("SELECT a FROM Answer a JOIN a.user u WHERE a.question = :question AND u.familyCode = :familyCode ORDER BY a.createdAt ASC")
    List<Answer> findByQuestionAndFamilyCode(@Param("question") Question question, @Param("familyCode") String familyCode);
}