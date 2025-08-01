package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Reaction;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByAnswerId(Long answerId);
    Optional<Reaction> findByAnswerAndUser(Answer answer, User user);
    List<Reaction> findByAnswerOrderByCreatedAtAsc(Answer answer);
}