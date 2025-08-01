package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.Reaction;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.domain.enums.ReactionType;
import com.steam.modeni.dto.ReactionResponse;
import com.steam.modeni.repository.AnswerRepository;
import com.steam.modeni.repository.DiaryRepository;
import com.steam.modeni.repository.ReactionRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {
    
    private final ReactionRepository reactionRepository;
    private final AnswerRepository answerRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    
    public Map<String, Object> createReaction(Long answerId, Long userId, String reactionType) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 자신의 답변에는 반응할 수 없음
        if (answer.getUser().getId().equals(userId)) {
            throw new RuntimeException("자신의 답변에는 반응할 수 없습니다.");
        }
        
        // 같은 가족 구성원인지 확인
        if (!answer.getUser().getFamilyCode().equals(user.getFamilyCode())) {
            throw new RuntimeException("같은 가족 구성원만 공감할 수 있습니다.");
        }
        
        // 이미 반응한 경우 기존 반응 삭제
        Optional<Reaction> existingReaction = reactionRepository.findByAnswerAndUser(answer, user);
        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
        }
        
        Reaction reaction = new Reaction();
        reaction.setAnswer(answer);
        reaction.setUser(user);
        reaction.setReactionType(ReactionType.valueOf(reactionType.toUpperCase()));
        
        Reaction savedReaction = reactionRepository.save(reaction);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedReaction.getId());
        response.put("created_at", savedReaction.getCreatedAt());
        response.put("message", "반응이 성공적으로 등록되었습니다.");
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public Reaction getReactionById(Long id) {
        return reactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("반응을 찾을 수 없습니다."));
    }
    
    public Map<String, String> deleteReaction(Long id) {
        Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("반응을 찾을 수 없습니다."));
        
        reactionRepository.delete(reaction);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "반응이 성공적으로 취소되었습니다.");
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<Reaction> getReactionsByAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        
        return reactionRepository.findByAnswerOrderByCreatedAtAsc(answer);
    }
    
    // ===== Diary 리액션 관련 메서드 =====
    
    public Map<String, Object> createDiaryReaction(Long diaryId, User user, ReactionType reactionType) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        
        // 자신의 일기에는 반응할 수 없음
        if (diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("자신의 일기에는 반응할 수 없습니다.");
        }
        
        // 같은 가족 구성원인지 확인
        if (!diary.getUser().getFamilyCode().equals(user.getFamilyCode())) {
            throw new RuntimeException("같은 가족 구성원만 공감할 수 있습니다.");
        }
        
        // 이미 반응한 경우 기존 반응 삭제
        Optional<Reaction> existingReaction = reactionRepository.findByDiaryAndUser(diary, user);
        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
        }
        
        Reaction reaction = new Reaction();
        reaction.setDiary(diary);
        reaction.setUser(user);
        reaction.setReactionType(reactionType);
        
        Reaction savedReaction = reactionRepository.save(reaction);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedReaction.getId());
        response.put("reactionType", savedReaction.getReactionType());
        response.put("userName", user.getName());
        response.put("createdAt", savedReaction.getCreatedAt());
        response.put("message", "일기에 반응이 성공적으로 등록되었습니다.");
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<ReactionResponse> getReactionsByDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        
        List<Reaction> reactions = reactionRepository.findByDiaryOrderByCreatedAtAsc(diary);
        
        return reactions.stream()
                .map(reaction -> new ReactionResponse(
                    reaction.getId(),
                    reaction.getUser().getName(),
                    reaction.getReactionType(),
                    reaction.getCreatedAt()
                ))
                .toList();
    }
    
    public Map<String, String> deleteDiaryReaction(Long reactionId, User user) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new RuntimeException("반응을 찾을 수 없습니다."));
        
        // 자신의 반응만 삭제 가능
        if (!reaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("자신의 반응만 삭제할 수 있습니다.");
        }
        
        // Diary 반응인지 확인
        if (!reaction.isDiaryReaction()) {
            throw new RuntimeException("일기 반응이 아닙니다.");
        }
        
        reactionRepository.delete(reaction);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "일기 반응이 성공적으로 취소되었습니다.");
        return response;
    }
}