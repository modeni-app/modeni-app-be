package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionOrderByCreatedAtAsc(Question question);
    boolean existsByQuestionAndUser(Question question, User user);
} 