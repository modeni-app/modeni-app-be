package com.steam.modeni.service;

import com.steam.modeni.domain.entity.MissionReview;
import com.steam.modeni.domain.entity.MissionReviewReaction;
import com.steam.modeni.domain.enums.ReactionType;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.repository.MissionReviewReactionRepository;
import com.steam.modeni.repository.MissionReviewRepository;
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
public class MissionReviewReactionService {
    
    private final MissionReviewReactionRepository reactionRepository;
    private final MissionReviewRepository reviewRepository;
    private final UserRepository userRepository;
    
    /**
     * 미션 후기에 반응(칭찬) 추가 또는 토글
     */
    public Map<String, Object> toggleReaction(Long reviewId, Long userId, String reactionTypeStr) {
        MissionReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 본인 후기에는 반응할 수 없음
        if (review.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 후기에는 반응할 수 없습니다.");
        }
        
        // 같은 가족 구성원인지 확인
        if (!user.getFamilyCode().equals(review.getMission().getFamilyCode())) {
            throw new RuntimeException("같은 가족 구성원만 반응할 수 있습니다.");
        }
        
        // 반응 타입 검증
        ReactionType reactionType;
        try {
            reactionType = ReactionType.valueOf(reactionTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 반응 타입입니다.");
        }
        
        // 기존 반응 확인
        Optional<MissionReviewReaction> existingReaction = 
                reactionRepository.findByMissionReviewAndUser(review, user);
        
        Map<String, Object> response = new HashMap<>();
        
        if (existingReaction.isPresent()) {
            // 기존 반응이 있으면 삭제 (토글 기능)
            reactionRepository.delete(existingReaction.get());
            response.put("message", "반응이 취소되었습니다.");
            response.put("action", "removed");
        } else {
            // 새로운 반응 추가
            MissionReviewReaction reaction = MissionReviewReaction.builder()
                    .missionReview(review)
                    .user(user)
                    .reactionType(reactionType)
                    .build();
            
            reactionRepository.save(reaction);
            response.put("message", "반응이 추가되었습니다.");
            response.put("action", "added");
        }
        
        // 현재 반응 수 조회
        long reactionCount = reactionRepository.countByMissionReview(review);
        response.put("reactionCount", reactionCount);
        
        return response;
    }
    
    /**
     * 특정 후기의 모든 반응 조회
     */
    @Transactional(readOnly = true)
    public List<MissionReviewReaction> getReactionsByReview(Long reviewId) {
        MissionReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        
        return reactionRepository.findByMissionReview(review);
    }
    
    /**
     * 특정 사용자의 모든 미션 후기 반응 조회
     */
    @Transactional(readOnly = true)
    public List<MissionReviewReaction> getReactionsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return reactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 특정 후기에 대한 반응 수 조회
     */
    @Transactional(readOnly = true)
    public long getReactionCount(Long reviewId) {
        MissionReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        
        return reactionRepository.countByMissionReview(review);
    }
}
