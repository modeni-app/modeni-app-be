package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByFamilyCode(String familyCode);
    List<Question> findByFamilyCodeOrFamilyCode(String familyCode1, String familyCode2);
    
    @Query("SELECT DISTINCT q FROM Question q JOIN Answer a ON q.id = a.question.id JOIN User u ON a.user.id = u.id WHERE u.familyCode = :familyCode")
    List<Question> findQuestionsWithAnswersByFamilyCode(@Param("familyCode") String familyCode);
    
    @Query("SELECT DISTINCT q FROM Question q JOIN Answer a ON q.id = a.question.id WHERE a.user.id = :userId")
    List<Question> findQuestionsWithAnswersByUserId(@Param("userId") Long userId);
}