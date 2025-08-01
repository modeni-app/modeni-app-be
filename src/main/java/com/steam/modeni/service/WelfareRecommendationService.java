package com.steam.modeni.service;

import com.steam.modeni.domain.entity.*;
import com.steam.modeni.domain.enums.PersonalityType;
import com.steam.modeni.dto.EmotionAnalysisResult;
import com.steam.modeni.dto.WelfareRecommendationResponse;
import com.steam.modeni.repository.WelfareProgramRepository;
import com.steam.modeni.repository.WelfareRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WelfareRecommendationService {

    private final WelfareProgramRepository welfareProgramRepository;
    private final WelfareRecommendationRepository recommendationRepository;
    private final EmotionAnalysisService emotionAnalysisService;

    @Async("welfareRecommendationExecutor")
    public void processEmotionAndRecommend(User user, String emotionText) {
        try {
            log.info("사용자 {}의 감정 분석 및 복지 추천 처리 시작", user.getId());
            
            // 1. 감정 분석
            EmotionAnalysisResult analysis = emotionAnalysisService.analyzeEmotion(emotionText);
            
            // 2. 복지 추천 필요 여부 확인
            if (!emotionAnalysisService.needsWelfareRecommendation(analysis)) {
                log.info("사용자 {}는 복지 추천이 필요하지 않음", user.getId());
                return;
            }
            
            // 3. 맞춤 프로그램 검색 및 추천
            List<WelfareProgram> recommendedPrograms = findRecommendedPrograms(user, analysis);
            
            // 4. 추천 결과 저장
            saveRecommendations(user, recommendedPrograms, analysis);
            
            log.info("사용자 {}에게 {}개의 복지 프로그램 추천 완료", user.getId(), recommendedPrograms.size());
            
        } catch (Exception e) {
            log.error("복지 추천 처리 중 오류 발생 (사용자: {}): {}", user.getId(), e.getMessage(), e);
        }
    }

    /**
     * 버튼 기반 감정 키워드와 희망 활동을 통한 복지 추천
     */
    @Async("welfareRecommendationExecutor")
    public void processButtonBasedRecommend(User user, String emotionKeyword, String wishActivity) {
        processButtonBasedRecommend(user, emotionKeyword, wishActivity, false);
    }

    /**
     * 버튼 기반 감정 키워드와 희망 활동을 통한 복지 추천 (GPT 추천 이유 생성 옵션)
     */
    @Async("welfareRecommendationExecutor")
    public void processButtonBasedRecommend(User user, String emotionKeyword, String wishActivity, boolean generateGptReason) {
        try {
            log.info("사용자 {}의 버튼 기반 복지 추천 시작: 감정={}, 희망활동={}", user.getId(), emotionKeyword, wishActivity);
            
            // 버튼 기반 감정 분석 수행 (사용자 성향 고려)
            EmotionAnalysisResult analysis = emotionAnalysisService.analyzeButtonBasedEmotion(emotionKeyword, wishActivity, user.getPersonalityType());
            
            // 추천 프로그램 찾기 (버튼 기반은 항상 추천 수행)
            List<WelfareProgram> recommendedPrograms = findRecommendedPrograms(user, analysis);
            
            if (recommendedPrograms.isEmpty()) {
                log.info("사용자 {}에게 추천할 프로그램이 없음", user.getId());
                return;
            }
            
            // 추천 결과 저장 (GPT 추천 이유 생성 옵션 포함)
            saveRecommendations(user, recommendedPrograms, analysis, generateGptReason, emotionKeyword, wishActivity);
            
            log.info("사용자 {}의 버튼 기반 복지 추천 완료: {}개 프로그램", user.getId(), recommendedPrograms.size());
            
        } catch (Exception e) {
            log.error("버튼 기반 복지 추천 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private List<WelfareProgram> findRecommendedPrograms(User user, EmotionAnalysisResult analysis) {
        List<WelfareProgram> allCandidates = new ArrayList<>();
        
        // 1. 지역 + 연령 기반 필터링
        if (user.getCity() != null && user.getAge() != null) {
            allCandidates.addAll(
                welfareProgramRepository.findByCityAndAgeRange(user.getCity(), user.getAge())
            );
        }
        
        // 2. 감정 키워드 기반 프로그램 추가
        for (String keyword : analysis.getKeywords()) {
            allCandidates.addAll(
                welfareProgramRepository.findByEmotionKeywordContaining(keyword)
            );
        }
        
        // 3. 추천 카테고리 기반 프로그램 추가
        for (String category : analysis.getRecommendedCategories()) {
            allCandidates.addAll(
                welfareProgramRepository.findByCategoryAndIsActiveTrue(category)
            );
        }
        
        // 4. 중복 제거 및 점수 계산
        return allCandidates.stream()
                .distinct()
                .filter(program -> calculateRelevanceScore(program, user, analysis) > 0.3)
                .sorted((p1, p2) -> Double.compare(
                    calculateRelevanceScore(p2, user, analysis), 
                    calculateRelevanceScore(p1, user, analysis)
                ))
                .limit(5) // 상위 5개만 추천
                .collect(Collectors.toList());
    }

    private double calculateRelevanceScore(WelfareProgram program, User user, EmotionAnalysisResult analysis) {
        double score = 0.0;
        
        // 성향 정보 유무에 따른 가중치 조정
        boolean hasPersonality = (user.getPersonalityType() != null);
        
        if (hasPersonality) {
            // 성향 정보가 있을 때: 더 정교한 매칭 (성향 가중치 증가)
            log.debug("성향 정보 있음: {} - 정교한 매칭 적용", user.getPersonalityType().getNickname());
            
            // 지역 매칭 (25%)
            if (program.getTargetCity() != null && program.getTargetCity().equals(user.getCity())) {
                score += 0.25;
            }
            
            // 연령 매칭 (15%)
            if (user.getAge() != null) {
                if (program.getTargetAgeMin() == null || program.getTargetAgeMin() <= user.getAge()) {
                    if (program.getTargetAgeMax() == null || program.getTargetAgeMax() >= user.getAge()) {
                        score += 0.15;
                    }
                }
            }
            
            // 감정 키워드 매칭 (25%)
            if (program.getEmotionKeywords() != null) {
                long matchingKeywords = analysis.getKeywords().stream()
                        .mapToLong(keyword -> program.getEmotionKeywords().contains(keyword) ? 1 : 0)
                        .sum();
                score += (matchingKeywords * 0.25) / Math.max(analysis.getKeywords().size(), 1);
            }
            
            // 카테고리 매칭 (15%)
            if (analysis.getRecommendedCategories().contains(program.getCategory())) {
                score += 0.15;
            }
            
            // 성향 매칭 (20%) - 성향이 있을 때 더 높은 가중치
            score += calculatePersonalityScore(program, user.getPersonalityType()) * 1.33; // 0.15 -> 0.20
            
        } else {
            // 성향 정보가 없을 때: 기본적인 매칭에 더 집중
            log.debug("성향 정보 없음 - 기본 매칭 적용");
            
            // 지역 매칭 (30%)
            if (program.getTargetCity() != null && program.getTargetCity().equals(user.getCity())) {
                score += 0.30;
            }
            
            // 연령 매칭 (20%)
            if (user.getAge() != null) {
                if (program.getTargetAgeMin() == null || program.getTargetAgeMin() <= user.getAge()) {
                    if (program.getTargetAgeMax() == null || program.getTargetAgeMax() >= user.getAge()) {
                        score += 0.20;
                    }
                }
            }
            
            // 감정 키워드 매칭 (30%)
            if (program.getEmotionKeywords() != null) {
                long matchingKeywords = analysis.getKeywords().stream()
                        .mapToLong(keyword -> program.getEmotionKeywords().contains(keyword) ? 1 : 0)
                        .sum();
                score += (matchingKeywords * 0.30) / Math.max(analysis.getKeywords().size(), 1);
            }
            
            // 카테고리 매칭 (20%)
            if (analysis.getRecommendedCategories().contains(program.getCategory())) {
                score += 0.20;
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    private double calculatePersonalityScore(WelfareProgram program, PersonalityType personalityType) {
        String title = program.getTitle().toLowerCase();
        String description = program.getDescription() != null ? program.getDescription().toLowerCase() : "";
        String keywords = program.getEmotionKeywords() != null ? program.getEmotionKeywords().toLowerCase() : "";
        String content = title + " " + description + " " + keywords;
        
        switch (personalityType) {
            case LOGICAL_BLUE:
                // 논리적, 교육적 프로그램 선호
                if (containsAny(content, "교육", "과학", "분석", "학습", "연구", "탐구", "역사", "지식")) {
                    return 0.15;
                }
                break;
            case EMOTIONAL_RED:
                // 감정적, 소통 중심 프로그램 선호
                if (containsAny(content, "가족", "소통", "만남", "공감", "상담", "함께", "이야기", "나눔")) {
                    return 0.15;
                }
                break;
            case CONTROL_GRAY:
                // 지도, 관리 관련 프로그램 선호
                if (containsAny(content, "리더", "지도", "교육", "관리", "멘토", "가이드", "봉사", "도움")) {
                    return 0.15;
                }
                break;
            case INDEPENDENT_NAVY:
                // 개인적, 자율적 활동 선호
                if (containsAny(content, "개인", "자율", "독립", "선택", "취미", "자유", "혼자", "창작")) {
                    return 0.15;
                }
                break;
            case AFFECTIONATE_YELLOW:
                // 표현적, 활동적 프로그램 선호
                if (containsAny(content, "표현", "활동", "즐거움", "놀이", "체험", "소통", "참여", "함께")) {
                    return 0.15;
                }
                break;
            case INTROSPECTIVE_GREEN:
                // 내향적, 사색적 프로그램 선호
                if (containsAny(content, "독서", "사색", "조용", "개인", "깊이", "성찰", "책", "글")) {
                    return 0.15;
                }
                break;
        }
        
        return 0.0; // 매칭되지 않으면 보너스 없음
    }
    
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    private void saveRecommendations(User user, List<WelfareProgram> programs, EmotionAnalysisResult analysis) {
        saveRecommendations(user, programs, analysis, false, null, null);
    }

    private void saveRecommendations(User user, List<WelfareProgram> programs, EmotionAnalysisResult analysis, 
                                   boolean generateGptReason, String emotionKeyword, String wishActivity) {
        for (WelfareProgram program : programs) {
            WelfareRecommendation recommendation = new WelfareRecommendation();
            recommendation.setUser(user);
            recommendation.setWelfareProgram(program);
            recommendation.setRecommendationScore(calculateRelevanceScore(program, user, analysis));
            recommendation.setAnalysisKeywords(String.join(", ", analysis.getKeywords()));
            recommendation.setEmotionAnalysis(analysis.getAnalysisText());
            
            // 기본 추천 이유
            recommendation.setReason(generateRecommendationReason(program, analysis));
            
            recommendationRepository.save(recommendation);
            
            // GPT 기반 개인화된 추천 이유 생성 (비동기로 처리)
            if (generateGptReason && emotionKeyword != null && wishActivity != null) {
                generateAndUpdateGptReason(recommendation, emotionKeyword, wishActivity, user.getPersonalityType());
            }
        }
    }
    
    @Async("welfareRecommendationExecutor")
    private void generateAndUpdateGptReason(WelfareRecommendation recommendation, String emotionKeyword, 
                                          String wishActivity, PersonalityType personalityType) {
        try {
            String gptReason = emotionAnalysisService.generatePersonalizedRecommendationReason(
                emotionKeyword, wishActivity, personalityType, recommendation.getWelfareProgram());
            
            // GPT 추천 이유를 reason 필드에 업데이트
            recommendation.setReason(gptReason);
            recommendationRepository.save(recommendation);
            
            log.info("GPT 기반 개인화된 추천 이유 생성 완료: 추천 ID {}", recommendation.getId());
        } catch (Exception e) {
            log.error("GPT 추천 이유 생성 실패: 추천 ID {}, 오류: {}", recommendation.getId(), e.getMessage());
        }
    }

    private String generateRecommendationReason(WelfareProgram program, EmotionAnalysisResult analysis) {
        StringBuilder reason = new StringBuilder();
        
        if (emotionAnalysisService.isNegativeEmotion(analysis)) {
            reason.append("현재 ").append(analysis.getEmotionCategory()).append(" 상태를 고려하여 ");
        }
        
        reason.append(program.getCategory()).append(" 분야의 ").append(program.getTitle()).append("을(를) 추천합니다.");
        
        if (!analysis.getKeywords().isEmpty()) {
            reason.append(" 특히 '").append(String.join(", ", analysis.getKeywords()))
                  .append("' 키워드와 관련이 있습니다.");
        }
        
        return reason.toString();
    }

    @Transactional(readOnly = true)
    public List<WelfareRecommendationResponse> getUserRecommendations(User user) {
        List<WelfareRecommendation> recommendations = recommendationRepository
                .findTop10ByUserOrderByCreatedAtDesc(user);
        
        return recommendations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WelfareRecommendationResponse> getUnreadRecommendations(User user) {
        List<WelfareRecommendation> recommendations = recommendationRepository
                .findByUserAndIsClickedFalseOrderByRecommendationScoreDesc(user);
        
        return recommendations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void markAsClicked(Long recommendationId, User user) {
        WelfareRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("추천 정보를 찾을 수 없습니다."));
        
        if (!recommendation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }
        
        recommendation.setIsClicked(true);
        recommendation.setClickedAt(LocalDateTime.now());
        recommendationRepository.save(recommendation);
    }

    public void markAsApplied(Long recommendationId, User user) {
        WelfareRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("추천 정보를 찾을 수 없습니다."));
        
        if (!recommendation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }
        
        recommendation.setIsApplied(true);
        recommendation.setAppliedAt(LocalDateTime.now());
        if (!recommendation.getIsClicked()) {
            recommendation.setIsClicked(true);
            recommendation.setClickedAt(LocalDateTime.now());
        }
        recommendationRepository.save(recommendation);
    }

    private WelfareRecommendationResponse convertToResponse(WelfareRecommendation recommendation) {
        WelfareProgram program = recommendation.getWelfareProgram();
        
        WelfareRecommendationResponse response = new WelfareRecommendationResponse();
        response.setId(recommendation.getId());
        response.setTitle(program.getTitle());
        response.setDescription(program.getDescription());
        response.setOrganization(program.getOrganization());
        response.setCategory(program.getCategory());
        response.setTargetCity(program.getTargetCity() != null ? program.getTargetCity().getDisplayName() : null);
        response.setApplicationUrl(program.getApplicationUrl());
        response.setContactNumber(program.getContactNumber());
        response.setRecommendationScore(recommendation.getRecommendationScore());
        response.setReason(recommendation.getReason());
        response.setClicked(recommendation.getIsClicked());
        response.setApplied(recommendation.getIsApplied());
        response.setCreatedAt(recommendation.getCreatedAt());
        
        // 새로 추가된 상세 정보
        response.setTarget(program.getTargetDescription());
        response.setLocation(program.getLocation());
        response.setSchedule(program.getSchedule());
        response.setGptRecommendationReason(recommendation.getReason()); // GPT가 생성한 이유
        
        return response;
    }
}