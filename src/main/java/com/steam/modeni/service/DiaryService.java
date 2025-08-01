package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.DiaryRequest;
import com.steam.modeni.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
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
        diary.setEmotionKeyword(request.getEmotionKeyword());
        diary.setWishActivity(request.getWishActivity());

        Diary savedDiary = diaryRepository.save(diary);
        log.info("사용자 {}의 감정 일기 생성 완료: {}", user.getId(), savedDiary.getId());

        // 비동기로 복지 추천 처리 (버튼 기반 우선, 텍스트는 보조)
        if (request.getEmotionKeyword() != null && request.getWishActivity() != null) {
            // 버튼 기반 추천 (우선)
            welfareRecommendationService.processButtonBasedRecommend(user, request.getEmotionKeyword(), request.getWishActivity());
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
        if (request.getEmotionKeyword() != null) {
            diary.setEmotionKeyword(request.getEmotionKeyword());
        }
        if (request.getWishActivity() != null) {
            diary.setWishActivity(request.getWishActivity());
        }

        Diary updatedDiary = diaryRepository.save(diary);

        // 내용이 변경된 경우 다시 감정 분석 및 추천 처리 (버튼 기반 우선)
        if (request.getEmotionKeyword() != null && request.getWishActivity() != null) {
            // 버튼 기반 추천 (우선)
            welfareRecommendationService.processButtonBasedRecommend(user, request.getEmotionKeyword(), request.getWishActivity());
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
}