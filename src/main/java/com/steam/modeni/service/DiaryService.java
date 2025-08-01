package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.DiaryRequest;
import com.steam.modeni.repository.DiaryRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final WelfareRecommendationService welfareRecommendationService;

    public Diary createDiary(User user, DiaryRequest request) {
        // 오늘 이미 일기를 작성했는지 확인
        Optional<Diary> todayDiary = diaryRepository.findTodayDiary(user, LocalDateTime.now());
        if (todayDiary.isPresent()) {
            throw new RuntimeException("오늘은 이미 감정 일기를 작성하셨습니다. 내일 다시 작성해주세요.");
        }

        // 일기 생성
        Diary diary = new Diary();
        diary.setUser(user);
        diary.setContent(request.getContent());
        
        // List<String> → String 변환 (콤마 구분)
        diary.setEmotionKeyword(convertListToString(request.getEmotionKeywords()));
        diary.setWishActivity(convertListToString(request.getWishActivities()));

        Diary savedDiary = diaryRepository.save(diary);
        log.info("사용자 {}의 감정 일기 생성 완료: {}", user.getId(), savedDiary.getId());

        // 비동기로 복지 추천 처리 (복수 선택 지원)
        if (request.getEmotionKeywords() != null && !request.getEmotionKeywords().isEmpty() &&
            request.getWishActivities() != null && !request.getWishActivities().isEmpty()) {
            // 버튼 기반 추천 (첫 번째 값 사용, 하위 호환성)
            welfareRecommendationService.processButtonBasedRecommend(user, 
                request.getEmotionKeywords().get(0), 
                request.getWishActivities().get(0));
        } else if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            // 텍스트 기반 추천 (보조)
            welfareRecommendationService.processEmotionAndRecommend(user, request.getContent());
        }

        return savedDiary;
    }

    @Transactional(readOnly = true)
    public List<Diary> getUserDiaries(User user) {
        return diaryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<Diary> getTodayDiary(User user) {
        return diaryRepository.findTodayDiary(user, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Diary> getDiariesByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return diaryRepository.findByUserAndDateRange(user, startDate, endDate);
    }

    public Diary updateDiary(Long diaryId, User user, DiaryRequest request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 일기만 수정할 수 있습니다.");
        }

        // 오늘 작성된 일기만 수정 가능
        LocalDateTime today = LocalDateTime.now();
        if (!diary.getCreatedAt().toLocalDate().equals(today.toLocalDate())) {
            throw new RuntimeException("오늘 작성한 일기만 수정할 수 있습니다.");
        }

        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }
        if (request.getEmotionKeywords() != null) {
            diary.setEmotionKeyword(convertListToString(request.getEmotionKeywords()));
        }
        if (request.getWishActivities() != null) {
            diary.setWishActivity(convertListToString(request.getWishActivities()));
        }

        Diary updatedDiary = diaryRepository.save(diary);

        // 내용이 변경된 경우 다시 감정 분석 및 추천 처리 (복수 선택 지원)
        if (request.getEmotionKeywords() != null && !request.getEmotionKeywords().isEmpty() &&
            request.getWishActivities() != null && !request.getWishActivities().isEmpty()) {
            // 버튼 기반 추천 (첫 번째 값 사용, 하위 호환성)
            welfareRecommendationService.processButtonBasedRecommend(user, 
                request.getEmotionKeywords().get(0), 
                request.getWishActivities().get(0));
        } else if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            // 텍스트 기반 추천 (보조)
            welfareRecommendationService.processEmotionAndRecommend(user, request.getContent());
        }

        return updatedDiary;
    }

    public void deleteDiary(Long diaryId, User user) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 일기만 삭제할 수 있습니다.");
        }

        diaryRepository.delete(diary);
        log.info("사용자 {}의 일기 삭제 완료: {}", user.getId(), diaryId);
    }

    @Transactional(readOnly = true)
    public List<Diary> searchDiariesByEmotion(User user, String emotionKeyword) {
        return diaryRepository.findByUserAndEmotionKeywordContainingOrderByCreatedAtDesc(user, emotionKeyword);
    }

    // ===== 가족 공유 기능 =====

    @Transactional(readOnly = true)
    public List<Diary> getFamilyDiaries(User user) {
        if (user.getFamilyCode() == null) {
            throw new RuntimeException("가족 코드가 설정되지 않았습니다.");
        }

        // 같은 familyCode를 가진 모든 사용자의 일기 조회
        List<User> familyMembers = userRepository.findByFamilyCode(user.getFamilyCode());

        return diaryRepository.findByUserInOrderByCreatedAtDesc(familyMembers);
    }

    @Transactional(readOnly = true)
    public List<Diary> getFamilyDiariesExceptMine(User user) {
        if (user.getFamilyCode() == null) {
            throw new RuntimeException("가족 코드가 설정되지 않았습니다.");
        }

        // 같은 familyCode를 가진 다른 사용자들의 일기만 조회
        List<User> familyMembers = userRepository.findByFamilyCodeAndIdNot(user.getFamilyCode(), user.getId());

        return diaryRepository.findByUserInOrderByCreatedAtDesc(familyMembers);
    }

    @Transactional(readOnly = true)
    public Diary getDiaryById(Long diaryId, User requestUser) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));

        // 가족 구성원인지 확인 (자신 또는 같은 가족)
        if (!diary.getUser().getId().equals(requestUser.getId()) &&
            !diary.getUser().getFamilyCode().equals(requestUser.getFamilyCode())) {
            throw new RuntimeException("가족 구성원만 일기를 볼 수 있습니다.");
        }

        return diary;
    }
    
    // ===== 하이브리드 구조 지원 유틸리티 메서드 =====
    
    /**
     * List<String>을 콤마 구분 문자열로 변환
     * ["행복", "뿌듯함"] → "행복,뿌듯함"
     */
    private String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .collect(Collectors.joining(","));
    }
    
    /**
     * 콤마 구분 문자열을 List<String>으로 변환
     * "행복,뿌듯함" → ["행복", "뿌듯함"]
     */
    private List<String> convertStringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Diary 엔티티에서 감정 키워드 리스트 추출
     */
    public List<String> getEmotionKeywordsList(Diary diary) {
        return convertStringToList(diary.getEmotionKeyword());
    }
    
    /**
     * Diary 엔티티에서 희망활동 리스트 추출
     */
    public List<String> getWishActivitiesList(Diary diary) {
        return convertStringToList(diary.getWishActivity());
    }
}