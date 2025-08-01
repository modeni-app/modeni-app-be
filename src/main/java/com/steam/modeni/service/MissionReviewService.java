package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Mission;
import com.steam.modeni.domain.entity.MissionReview;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.repository.MissionRepository;
import com.steam.modeni.repository.MissionReviewRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionReviewService {
    
    private final MissionReviewRepository missionReviewRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final MissionService missionService;
    
    /**
     * 미션 후기 작성
     */
    public Map<String, Object> createReview(Long missionId, Long userId, String content) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 요청 데이터 검증
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("후기 내용은 필수입니다.");
        }
        
        // 가족 구성원인지 확인
        if (!user.getFamilyCode().equals(mission.getFamilyCode())) {
            throw new RuntimeException("해당 미션에 참여할 권한이 없습니다.");
        }
        
        // 이미 후기를 작성했는지 확인
        if (missionReviewRepository.existsByMissionAndUser(mission, user)) {
            throw new RuntimeException("이미 해당 미션에 대한 후기를 작성하셨습니다.");
        }
        
        // 후기 생성
        MissionReview review = MissionReview.builder()
                .mission(mission)
                .user(user)
                .content(content.trim())
                .build();
        
        MissionReview savedReview = missionReviewRepository.save(review);
        
        // 미션 완료 상태 확인 및 업데이트
        missionService.checkAndUpdateMissionCompletion(missionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "후기가 성공적으로 작성되었습니다.");
        response.put("reviewId", savedReview.getId());
        return response;
    }
    
    /**
     * 미션 후기 수정
     */
    public Map<String, Object> updateReview(Long reviewId, Long userId, String content) {
        MissionReview review = missionReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 작성자 본인인지 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 후기만 수정할 수 있습니다.");
        }
        
        // 요청 데이터 검증
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("후기 내용은 필수입니다.");
        }
        
        review.setContent(content.trim());
        missionReviewRepository.save(review);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "후기가 성공적으로 수정되었습니다.");
        return response;
    }
    
    /**
     * 미션 후기 삭제
     */
    public Map<String, Object> deleteReview(Long reviewId, Long userId) {
        MissionReview review = missionReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 작성자 본인인지 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 후기만 삭제할 수 있습니다.");
        }
        
        Long missionId = review.getMission().getId();
        missionReviewRepository.delete(review);
        
        // 미션 완료 상태 재확인 (후기 삭제로 인해 미완료 상태로 변경될 수 있음)
        Mission mission = missionRepository.findById(missionId).orElse(null);
        if (mission != null && mission.getIsCompleted()) {
            mission.setIsCompleted(false);
            mission.setCompletedAt(null);
            missionRepository.save(mission);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "후기가 성공적으로 삭제되었습니다.");
        return response;
    }
    
    /**
     * 특정 미션의 모든 후기 조회
     */
    @Transactional(readOnly = true)
    public List<MissionReview> getReviewsByMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
        
        return missionReviewRepository.findByMissionOrderByCreatedAtDesc(mission);
    }
    
    /**
     * 특정 사용자의 모든 미션 후기 조회
     */
    @Transactional(readOnly = true)
    public List<MissionReview> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return missionReviewRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 특정 후기 조회
     */
    @Transactional(readOnly = true)
    public MissionReview getReviewById(Long reviewId) {
        return missionReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
    }
}
