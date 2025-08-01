package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.Reaction;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    // Answer 관련 메서드 (기존)
    List<Reaction> findByAnswerOrderByCreatedAtAsc(Answer answer);
    Optional<Reaction> findByAnswerAndUser(Answer answer, User user);
    
    // Diary 관련 메서드 (새로 추가)
    List<Reaction> findByDiaryOrderByCreatedAtAsc(Diary diary);
    Optional<Reaction> findByDiaryAndUser(Diary diary, User user);
}