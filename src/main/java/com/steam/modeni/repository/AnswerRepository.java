package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);
    Optional<Answer> findByQuestionAndUser(Question question, User user);
    boolean existsByQuestionAndUser(Question question, User user);
    List<Answer> findByQuestionOrderByCreatedAtAsc(Question question);
}