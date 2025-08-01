package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Mission;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.repository.MissionRepository;
import com.steam.modeni.repository.MissionReviewRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {
    
    private final MissionRepository missionRepository;
    private final MissionReviewRepository missionReviewRepository;
    private final UserRepository userRepository;
    
    // 8가지 미션 내용
    private static final String[] MISSION_CONTENTS = {
        "가족과 근처 공원에서 자전거를 타며 신선한 공기를 마시고 함께 운동하며 즐거운 시간을 보내세요.",
        "가족과 함께 영화를 정하고 팝콘과 간식을 준비해 편안하게 감상한 뒤, 영화에 대한 감상평을 자유롭게 나누며 소통하는 시간을 가져보세요.",
        "서로 좋아하는 음식을 다같이 요리하고, 저녁 식사로 먹으면서 뿌듯한 시간을 보내세요.",
        "쉬는 날에 자식이 가고싶은 장소 또는 하고싶은 것을 하면서 자식들이 좋아하는 것을 배우며 함께 즐겨보세요.",
        "가족의 옛 사진이나 추억이 담긴 물건들을 함께 꺼내어 보며 그때의 기억을 되살리고, 각자가 가장 소중하게 생각하는 추억 하나씩을 자세히 들려주세요.",
        "가족과 함께 집 안팎을 정리정돈하거나 청소를 하면서 협력하고, 작업을 마친 후 깨끗해진 공간에서 함께 차나 음료를 마시며 성취감을 나눠보세요.",
        "가족 구성원 한 명씩 돌아가며 '칭찬 릴레이'를 해보며 평소 하지 못한 말을 꺼내 보세요.",
        "최근 나를 웃게 만든 일이 있다면 가족과 함께 나눠보며 당신의 기분을 전염 시켜 보세요."
    };
    
    /**
     * 특정 가족에게 주간 미션 지급 (1주일에 1번)
     */
    public Mission assignWeeklyMission(Long familyCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                     .withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // 이미 이번 주에 미션이 지급되었는지 확인
        if (missionRepository.findByFamilyCodeAndWeekStartDate(familyCode, weekStart).isPresent()) {
            throw new RuntimeException("이번 주에 이미 미션이 지급되었습니다.");
        }
        
        // 랜덤 미션 선택 (1-8)
        Random random = new Random();
        int missionNumber = random.nextInt(8) + 1;
        String content = MISSION_CONTENTS[missionNumber - 1];
        
        Mission mission = Mission.builder()
                .familyCode(familyCode)
                .missionNumber(missionNumber)
                .content(content)
                .assignedAt(now)
                .weekStartDate(weekStart)
                .isCompleted(false)
                .build();
        
        return missionRepository.save(mission);
    }
    
    /**
     * 특정 가족의 현재 진행 중인 미션들 조회
     */
    @Transactional(readOnly = true)
    public List<Mission> getCurrentMissions(Long familyCode) {
        return missionRepository.findByFamilyCodeAndIsCompletedFalseOrderByAssignedAtDesc(familyCode);
    }
    
    /**
     * 특정 가족의 완료된 미션들 조회
     */
    @Transactional(readOnly = true)
    public List<Mission> getCompletedMissions(Long familyCode) {
        return missionRepository.findByFamilyCodeAndIsCompletedTrueOrderByCompletedAtDesc(familyCode);
    }
    
    /**
     * 특정 가족의 모든 미션 조회
     */
    @Transactional(readOnly = true)
    public List<Mission> getAllMissions(Long familyCode) {
        return missionRepository.findByFamilyCodeOrderByCreatedAtDesc(familyCode);
    }
    
    /**
     * 미션 완료 상태 확인 및 업데이트
     * 모든 가족 구성원이 후기를 작성했는지 확인
     */
    public void checkAndUpdateMissionCompletion(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
        
        if (mission.getIsCompleted()) {
            return; // 이미 완료된 미션
        }
        
        // 해당 가족의 구성원 수 조회
        long familyMemberCount = userRepository.countByFamilyCode(mission.getFamilyCode());
        
        // 해당 미션에 후기를 작성한 구성원 수 조회
        long reviewCount = missionReviewRepository.countDistinctUsersByMission(mission);
        
        // 모든 구성원이 후기를 작성했으면 미션 완료 처리
        if (familyMemberCount == reviewCount && familyMemberCount > 0) {
            mission.setIsCompleted(true);
            mission.setCompletedAt(LocalDateTime.now());
            missionRepository.save(mission);
        }
    }
    
    /**
     * 특정 미션 조회
     */
    @Transactional(readOnly = true)
    public Mission getMissionById(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
    }
}
