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
            // TODO: PersonalityType 기능 임시 비활성화 - 프론트 개발 상황에 따라 추후 활성화
        EmotionAnalysisResult analysis = emotionAnalysisService.analyzeButtonBasedEmotion(emotionKeyword, wishActivity, null /* user.getPersonalityType() */);
            
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
        Integer userAge = parseAge(user.getAge());
        if (user.getRegion() != null && userAge != null) {
            allCandidates.addAll(
                welfareProgramRepository.findByCityAndAgeRange(user.getRegion(), userAge)
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
        // TODO: PersonalityType 기능 임시 비활성화
        boolean hasPersonality = false; // (user.getPersonalityType() != null);
        
        if (hasPersonality) {
            // 성향 정보가 있을 때: 더 정교한 매칭 (성향 가중치 증가)
            // TODO: PersonalityType 기능 임시 비활성화
            // log.debug("성향 정보 있음: {} - 정교한 매칭 적용", user.getPersonalityType().getNickname());
            log.debug("성향 정보 없음 - 기본 매칭 적용");
            
            // 지역 매칭 (25%)
            if (program.getTargetCity() != null && program.getTargetCity().equals(user.getRegion())) {
                score += 0.25;
            }
            
            // 연령 매칭 (15%)
            Integer userAge = parseAge(user.getAge());
            if (userAge != null) {
                if (program.getTargetAgeMin() == null || program.getTargetAgeMin() <= userAge) {
                    if (program.getTargetAgeMax() == null || program.getTargetAgeMax() >= userAge) {
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
            // TODO: PersonalityType 기능 임시 비활성화
            // score += calculatePersonalityScore(program, user.getPersonalityType()) * 1.33; // 0.15 -> 0.20
            
        } else {
            // 성향 정보가 없을 때: 기본적인 매칭에 더 집중
            log.debug("성향 정보 없음 - 기본 매칭 적용");
            
            // 지역 매칭 (30%)
            if (program.getTargetCity() != null && program.getTargetCity().equals(user.getRegion())) {
                score += 0.30;
            }
            
            // 연령 매칭 (20%)
            Integer userAge = parseAge(user.getAge());
            if (userAge != null) {
                if (program.getTargetAgeMin() == null || program.getTargetAgeMin() <= userAge) {
                    if (program.getTargetAgeMax() == null || program.getTargetAgeMax() >= userAge) {
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
    
    // TODO: PersonalityType 기능 임시 비활성화 - 프론트 개발 상황에 따라 추후 활성화
    private double calculatePersonalityScore(WelfareProgram program, PersonalityType personalityType) {
        // 임시로 null 체크 후 0.0 반환
        if (personalityType == null) {
            return 0.0;
        }
        
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
    
    // Helper method: String age를 Integer로 변환
    private Integer parseAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(ageStr.trim());
        } catch (NumberFormatException e) {
            log.warn("잘못된 age 형식: {}", ageStr);
            return null;
        }
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
            
            // 버튼 기반 추천인 경우 개인화된 추천 이유 사용
            if (emotionKeyword != null && wishActivity != null) {
                recommendation.setReason(generatePersonalizedReason(program, analysis, emotionKeyword, wishActivity));
            } else {
                // 기본 추천 이유
                recommendation.setReason(generateRecommendationReason(program, analysis));
            }
            
            recommendationRepository.save(recommendation);
            
            // GPT 기반 개인화된 추천 이유 생성 (비동기로 처리)
            if (generateGptReason && emotionKeyword != null && wishActivity != null) {
                // TODO: PersonalityType 기능 임시 비활성화
        generateAndUpdateGptReason(recommendation, emotionKeyword, wishActivity, null /* user.getPersonalityType() */);
            }
        }
    }
    
    @Async("welfareRecommendationExecutor")
    private void generateAndUpdateGptReason(WelfareRecommendation recommendation, String emotionKeyword, 
                                          String wishActivity, PersonalityType personalityType) {
        try {
            // TODO: PersonalityType 기능 임시 비활성화 - null이 올 수 있음
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
        return generatePersonalizedReason(program, analysis, null, null);
    }
    
    private String generatePersonalizedReason(WelfareProgram program, EmotionAnalysisResult analysis, String emotion, String activity) {
        StringBuilder reason = new StringBuilder();
        
        // 감정에 따른 친근한 인사말
        if (emotion != null) {
            switch (emotion) {
                case "우울함":
                case "슬픔":
                    reason.append("요즘 마음이 힘드시군요. ");
                    break;
                case "불안함":
                case "초조함":
                    reason.append("마음이 불안하신 것 같아요. ");
                    break;
                case "행복":
                case "여유로움":
                    reason.append("좋은 기분이시네요! ");
                    break;
                case "화남":
                case "짜증":
                    reason.append("스트레스가 많으신 것 같아요. ");
                    break;
                default:
                    reason.append("오늘의 기분에 맞는 ");
            }
        }
        
        // 희망활동에 따른 추천 이유
        if (activity != null) {
            switch (activity) {
                case "산책하기":
                    reason.append("산책을 좋아하시는 분에게 ");
                    break;
                case "요리하기":
                    reason.append("요리에 관심이 많으신 분에게 ");
                    break;
                case "그림그리기":
                    reason.append("창작 활동을 좋아하시는 분에게 ");
                    break;
                case "음악감상":
                    reason.append("음악을 사랑하시는 분에게 ");
                    break;
                case "독서하기":
                    reason.append("책 읽기를 좋아하시는 분에게 ");
                    break;
                case "운동하기":
                    reason.append("활동적인 삶을 좋아하시는 분에게 ");
                    break;
                case "영화감상":
                    reason.append("영화를 좋아하시는 분에게 ");
                    break;
                default:
                    reason.append("이런 활동을 좋아하시는 분에게 ");
            }
        }
        
        // 프로그램 소개
        reason.append(program.getTitle()).append("을(를) 추천드려요! ");
        
        // 추천 이유 상세 설명
        if (program.getCategory().contains("문화")) {
            reason.append("다양한 문화 체험을 통해 새로운 즐거움을 찾을 수 있어요.");
        } else if (program.getCategory().contains("교육")) {
            reason.append("새로운 지식과 기술을 배우며 성장할 수 있는 기회예요.");
        } else if (program.getCategory().contains("여가")) {
            reason.append("일상에서 벗어나 편안한 시간을 보낼 수 있어요.");
        } else if (program.getCategory().contains("건강")) {
            reason.append("머리와 마음의 건강을 동시에 챙길 수 있는 프로그램이에요.");
        } else {
            reason.append("의미 있는 시간을 보내며 새로운 경험을 할 수 있어요.");
        }
        
        // 마무리 멘트
        if (emotion != null && ("우울함".equals(emotion) || "불안함".equals(emotion))) {
            reason.append(" 좋은 사람들과 함께하며 마음의 안정을 찾으실 수 있을 거예요.");
        } else {
            reason.append(" 즐거운 시간 보내세요!");
        }
        
        return reason.toString();
    }

    @Transactional(readOnly = true)
    public List<WelfareRecommendationResponse> getUserRecommendations(User user) {
        List<WelfareRecommendation> recommendations = recommendationRepository.findByUserOrderByCreatedAtDesc(user);
        return recommendations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WelfareRecommendationResponse> getUnreadRecommendations(User user) {
        // 클릭 확인 기능 제거 - 모든 추천을 반환
        List<WelfareRecommendation> recommendations = recommendationRepository.findByUserOrderByCreatedAtDesc(user);
        return recommendations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 컨트롤러에서 호출하는 메서드들 추가
    public List<WelfareRecommendationResponse> analyzeEmotionAndRecommend(User user, String emotionText) {
        // 감정 분석 및 추천 처리
        processEmotionAndRecommend(user, emotionText);
        
        // 임시로 빈 리스트 반환 (실제로는 추천 결과를 반환해야 함)
        return new ArrayList<>();
    }

    public List<WelfareRecommendationResponse> recommendByButtons(User user, String emotion, String activity) {
        try {
            log.info("사용자 {}의 버튼 기반 복지 추천 시작: 감정={}, 희망활동={}", user.getId(), emotion, activity);
            
            // 버튼 기반 감정 분석 수행 (동기적 처리)
            EmotionAnalysisResult analysis = emotionAnalysisService.analyzeButtonBasedEmotion(emotion, activity, null);
            
            // 추천 프로그램 찾기
            List<WelfareProgram> recommendedPrograms = findRecommendedPrograms(user, analysis);
            
            if (recommendedPrograms.isEmpty()) {
                log.info("사용자 {}에게 추천할 프로그램이 없음", user.getId());
                return new ArrayList<>();
            }
            
            // 추천 결과 저장
            saveRecommendations(user, recommendedPrograms, analysis, false, emotion, activity);
            
            log.info("사용자 {}의 버튼 기반 복지 추천 완료: {}개 프로그램", user.getId(), recommendedPrograms.size());
            
            // 방금 생성된 추천 결과를 개인화된 이유와 함께 반환 (버튼 기반 추천은 4개로 제한)
            return recommendedPrograms.stream()
                    .limit(4)
                    .map(program -> {
                        WelfareRecommendationResponse response = new WelfareRecommendationResponse();
                        response.setId(program.getId());
                        response.setTitle(program.getTitle());
                        response.setDescription(program.getDescription());
                        response.setOrganization(program.getOrganization());
                        response.setCategory(program.getCategory());
                        response.setTargetCity(program.getTargetCity() != null ? program.getTargetCity().getDisplayName() : null);
                        response.setApplicationUrl(program.getApplicationUrl());
                        response.setContactNumber(program.getContactNumber());
                        response.setTarget(program.getTargetDescription());
                        response.setLocation(program.getLocation());
                        response.setSchedule(program.getSchedule());
                        
                        // 개인화된 추천 이유 추가
                        response.setReason(generatePersonalizedReason(program, analysis, emotion, activity));
                        
                        return response;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("버튼 기반 추천 오류: 사용자 {}, 오류: {}", user.getId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    public WelfareRecommendationResponse getRecommendationDetail(Long id, User user) {
        WelfareRecommendation recommendation = recommendationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("추천 정보를 찾을 수 없습니다."));
        return convertToResponse(recommendation);
    }

    public List<WelfareRecommendationResponse> searchPrograms(User user, String keyword, String category, Integer age) {
        // 프로그램 검색 로직 (임시 구현)
        List<WelfareProgram> programs = welfareProgramRepository.findAll();
        return programs.stream()
                .map(program -> {
                    WelfareRecommendationResponse response = new WelfareRecommendationResponse();
                    response.setId(program.getId());
                    response.setTitle(program.getTitle());
                    response.setDescription(program.getDescription());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<WelfareRecommendationResponse> getPersonalizedRecommendations(User user, String emotion, String activity, String keyword) {
        // 개인화된 추천 처리
        processButtonBasedRecommend(user, emotion, activity);
        
        // 임시로 빈 리스트 반환
        return new ArrayList<>();
    }

    // 클릭/신청 확인 기능 제거 - 간단한 복지 정보 제공에 집중

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