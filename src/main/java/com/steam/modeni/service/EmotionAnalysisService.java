package com.steam.modeni.service;

import com.steam.modeni.dto.EmotionAnalysisResult;
import com.steam.modeni.domain.entity.WelfareProgram;
import com.steam.modeni.domain.enums.PersonalityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

    private final OpenAiService openAiService;

    public EmotionAnalysisResult analyzeEmotion(String text) {
        try {
            String analysisPrompt = createEmotionAnalysisPrompt(text);
            String aiResponse = openAiService.generateSimpleResponse(analysisPrompt);
            return parseEmotionAnalysis(aiResponse);
        } catch (Exception e) {
            log.error("감정 분석 중 오류 발생: {}", e.getMessage());
            return createFallbackAnalysis(text);
        }
    }

    private String createEmotionAnalysisPrompt(String text) {
        return "다음 텍스트의 감정을 분석해주세요. 결과는 정확히 아래 형식으로 응답해주세요:\n\n" +
                "PRIMARY_EMOTION: [긍정/부정/중립]\n" +
                "EMOTION_SCORE: [0.0-1.0 사이의 숫자]\n" +
                "KEYWORDS: [키워드1, 키워드2, 키워드3]\n" +
                "EMOTION_CATEGORY: [행복/우울/스트레스/불안/평온/흥미/분노/슬픔/호기심/성장 중 하나]\n" +
                "RECOMMENDED_CATEGORIES: [문화, 교육, 상담, 취업, 의료, 운동, 여가, 독서, 영어, 과학, 요리, 놀이, 가족, 예술, 역사 중 관련된 것들]\n" +
                "ANALYSIS: [감정 분석 결과 설명]\n\n" +
                "참고: 동작구립도서관의 다양한 문화 프로그램(독서, 영어, 과학, 요리, 놀이, 가족활동, 예술, 역사 등)이 있으니 이를 고려하여 추천해주세요.\n\n" +
                "분석할 텍스트: \"" + text + "\"";
    }

    private EmotionAnalysisResult parseEmotionAnalysis(String aiResponse) {
        EmotionAnalysisResult result = new EmotionAnalysisResult();

        try {
            // PRIMARY_EMOTION 추출
            String primaryEmotion = extractValue(aiResponse, "PRIMARY_EMOTION");
            result.setPrimaryEmotion(primaryEmotion);

            // EMOTION_SCORE 추출
            String scoreStr = extractValue(aiResponse, "EMOTION_SCORE");
            try {
                result.setEmotionScore(Double.parseDouble(scoreStr));
            } catch (NumberFormatException e) {
                result.setEmotionScore(0.5); // 기본값
            }

            // KEYWORDS 추출
            String keywordsStr = extractValue(aiResponse, "KEYWORDS");
            if (keywordsStr != null) {
                List<String> keywords = Arrays.asList(keywordsStr.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                result.setKeywords(keywords);
            }

            // EMOTION_CATEGORY 추출
            String emotionCategory = extractValue(aiResponse, "EMOTION_CATEGORY");
            result.setEmotionCategory(emotionCategory);

            // RECOMMENDED_CATEGORIES 추출
            String categoriesStr = extractValue(aiResponse, "RECOMMENDED_CATEGORIES");
            if (categoriesStr != null) {
                List<String> categories = Arrays.asList(categoriesStr.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                result.setRecommendedCategories(categories);
            }

            // ANALYSIS 추출
            String analysis = extractValue(aiResponse, "ANALYSIS");
            result.setAnalysisText(analysis);

        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류: {}", e.getMessage());
            return createFallbackAnalysis("");
        }

        return result;
    }

    private String extractValue(String text, String key) {
        try {
            Pattern pattern = Pattern.compile(key + ":\\s*(.+?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("값 추출 실패 for key: {}", key);
        }
        return null;
    }

    private EmotionAnalysisResult createFallbackAnalysis(String text) {
        EmotionAnalysisResult result = new EmotionAnalysisResult();
        result.setPrimaryEmotion("중립");
        result.setEmotionScore(0.5);
        result.setKeywords(List.of("일상", "생활"));
        result.setEmotionCategory("평온");
        result.setRecommendedCategories(List.of("문화", "여가"));
        result.setAnalysisText("감정 분석을 수행할 수 없습니다.");
        return result;
    }

    public boolean isNegativeEmotion(EmotionAnalysisResult analysis) {
        return "부정".equals(analysis.getPrimaryEmotion()) || 
               List.of("우울", "스트레스", "불안", "분노", "슬픔").contains(analysis.getEmotionCategory());
    }

    public boolean needsWelfareRecommendation(EmotionAnalysisResult analysis) {
        // 부정적 감정이거나 특정 키워드가 포함된 경우 복지 추천 필요
        if (isNegativeEmotion(analysis)) {
            return true;
        }

        // 특정 키워드가 포함된 경우
        List<String> triggerKeywords = List.of(
            "도움", "지원", "상담", "취업", "교육", "문화", "힘들어", "어려워",
            "독서", "책", "영어", "과학", "요리", "놀이", "가족", "예술", "역사",
            "배우고", "학습", "성장", "호기심", "창작", "소통", "만남", "체험",
            "즐거움", "재미", "흥미", "취미", "활동", "참여"
        );
        return analysis.getKeywords().stream()
                .anyMatch(keyword -> triggerKeywords.stream()
                        .anyMatch(trigger -> keyword.contains(trigger)));
    }

    /**
     * 버튼 선택 기반 감정 분석 결과 생성
     */
    public EmotionAnalysisResult analyzeButtonBasedEmotion(String emotionKeyword, String wishActivity) {
        return analyzeButtonBasedEmotion(emotionKeyword, wishActivity, null);
    }

    /**
     * 버튼 선택 기반 감정 분석 결과 생성 (성향 고려)
     */
    public EmotionAnalysisResult analyzeButtonBasedEmotion(String emotionKeyword, String wishActivity, PersonalityType personalityType) {
        EmotionAnalysisResult result = new EmotionAnalysisResult();
        
        // 긍정적 감정 키워드들
        List<String> positiveEmotions = List.of("행복", "뿌듯함", "즐거움", "설렘", "여유로움", "활기참", "안도감", "차분함", "기특함");
        
        // 부정적 감정 키워드들  
        List<String> negativeEmotions = List.of("서운함", "불안함", "짜증남", "초조함", "실망", "후회", "우울함", "슬픔", "지침", "답답함");
        
        // 감정 분석
        if (positiveEmotions.contains(emotionKeyword)) {
            result.setPrimaryEmotion("긍정");
            result.setEmotionScore(0.8);
            result.setEmotionCategory("행복");
        } else if (negativeEmotions.contains(emotionKeyword)) {
            result.setPrimaryEmotion("부정");
            result.setEmotionScore(0.3);
            result.setEmotionCategory("스트레스");
        } else {
            result.setPrimaryEmotion("중립");
            result.setEmotionScore(0.5);
            result.setEmotionCategory("평온");
        }
        
        // 키워드 생성 (감정 + 희망활동 기반)
        List<String> keywords = new ArrayList<>();
        keywords.add(emotionKeyword);
        keywords.add(wishActivity);
        
        // 희망 활동별 추가 키워드
        addWishActivityKeywords(keywords, wishActivity);
        
        // 성향별 추가 키워드
        if (personalityType != null) {
            addPersonalityKeywords(keywords, personalityType);
        }
        
        result.setKeywords(keywords);
        
        // 추천 카테고리 생성 (성향 고려)
        List<String> recommendedCategories = generateRecommendedCategories(emotionKeyword, wishActivity, personalityType);
        result.setRecommendedCategories(recommendedCategories);
        
        // 분석 텍스트 생성
        result.setAnalysisText(generateButtonBasedAnalysis(emotionKeyword, wishActivity));
        
        return result;
    }
    
    private void addWishActivityKeywords(List<String> keywords, String wishActivity) {
        switch (wishActivity) {
            case "독서하기":
                keywords.addAll(List.of("독서", "교육", "학습", "문화"));
                break;
            case "요리하기":
                keywords.addAll(List.of("요리", "창작", "가족", "활동"));
                break;
            case "그림그리기":
                keywords.addAll(List.of("예술", "창작", "문화", "표현"));
                break;
            case "노래부르기":
                keywords.addAll(List.of("음악", "예술", "문화", "표현"));
                break;
            case "운동하기":
                keywords.addAll(List.of("운동", "건강", "활력", "활동"));
                break;
            case "영화보기":
                keywords.addAll(List.of("문화", "여가", "감상", "체험"));
                break;
            case "카페가기":
            case "맛집가기":
                keywords.addAll(List.of("여가", "문화", "소통", "활동"));
                break;
            case "산책하기":
            case "꽃구경":
                keywords.addAll(List.of("자연", "여가", "힐링", "활동"));
                break;
            case "사진찍기":
                keywords.addAll(List.of("예술", "창작", "문화", "기록"));
                break;
            case "게임하기":
                keywords.addAll(List.of("놀이", "즐거움", "활동", "여가"));
                break;
            case "음악듣기":
                keywords.addAll(List.of("음악", "문화", "여가", "감상"));
                break;
            default:
                keywords.addAll(List.of("활동", "여가", "문화"));
        }
    }
    
    private void addPersonalityKeywords(List<String> keywords, PersonalityType personalityType) {
        switch (personalityType) {
            case LOGICAL_BLUE:
                keywords.addAll(List.of("분석", "논리", "교육", "과학", "문제해결", "학습"));
                break;
            case EMOTIONAL_RED:
                keywords.addAll(List.of("공감", "소통", "가족", "상담", "감정표현", "만남"));
                break;
            case CONTROL_GRAY:
                keywords.addAll(List.of("지도", "교육", "리더십", "보호", "관리", "책임"));
                break;
            case INDEPENDENT_NAVY:
                keywords.addAll(List.of("자율", "독립", "개별", "자유", "선택", "취미"));
                break;
            case AFFECTIONATE_YELLOW:
                keywords.addAll(List.of("표현", "애정", "소통", "가족", "활동", "즐거움"));
                break;
            case INTROSPECTIVE_GREEN:
                keywords.addAll(List.of("내면", "독서", "사색", "개인", "깊이", "조용"));
                break;
        }
    }

    private List<String> generateRecommendedCategories(String emotionKeyword, String wishActivity) {
        return generateRecommendedCategories(emotionKeyword, wishActivity, null);
    }

    private List<String> generateRecommendedCategories(String emotionKeyword, String wishActivity, PersonalityType personalityType) {
        List<String> categories = new ArrayList<>();
        
        // 감정 기반 카테고리
        if (List.of("우울함", "슬픔", "스트레스", "불안함").contains(emotionKeyword)) {
            categories.add("상담");
        }
        
        // 희망 활동 기반 카테고리
        switch (wishActivity) {
            case "독서하기":
                categories.addAll(List.of("문화", "교육", "독서"));
                break;
            case "요리하기":
                categories.addAll(List.of("문화", "창작", "가족"));
                break;
            case "그림그리기":
            case "사진찍기":
                categories.addAll(List.of("문화", "예술", "창작"));
                break;
            case "노래부르기":
            case "음악듣기":
                categories.addAll(List.of("문화", "예술", "음악"));
                break;
            case "운동하기":
                categories.addAll(List.of("운동", "건강", "활동"));
                break;
            case "영화보기":
            case "게임하기":
                categories.addAll(List.of("문화", "여가", "활동"));
                break;
            case "산책하기":
            case "꽃구경":
                categories.addAll(List.of("여가", "자연", "힐링"));
                break;
            default:
                categories.addAll(List.of("문화", "여가", "활동"));
        }
        
        // 성향별 추가 카테고리
        if (personalityType != null) {
            addPersonalityCategories(categories, personalityType);
        }
        
        return categories;
    }
    
    private void addPersonalityCategories(List<String> categories, PersonalityType personalityType) {
        switch (personalityType) {
            case LOGICAL_BLUE:
                categories.addAll(List.of("교육", "과학", "역사"));
                break;
            case EMOTIONAL_RED:
                categories.addAll(List.of("상담", "가족", "소통"));
                break;
            case CONTROL_GRAY:
                categories.addAll(List.of("교육", "리더십", "관리"));
                break;
            case INDEPENDENT_NAVY:
                categories.addAll(List.of("취미", "개인활동", "자율학습"));
                break;
            case AFFECTIONATE_YELLOW:
                categories.addAll(List.of("가족", "소통", "표현"));
                break;
            case INTROSPECTIVE_GREEN:
                categories.addAll(List.of("독서", "사색", "개인성장"));
                break;
        }
    }
    
    private String generateButtonBasedAnalysis(String emotionKeyword, String wishActivity) {
        StringBuilder analysis = new StringBuilder();
        
        if (List.of("행복", "뿌듯함", "즐거움", "설렘", "여유로움", "활기참", "안도감", "차분함", "기특함").contains(emotionKeyword)) {
            analysis.append("긍정적인 감정 상태로 보입니다. ");
        } else if (List.of("서운함", "불안함", "짜증남", "초조함", "실망", "후회", "우울함", "슬픔", "지침", "답답함").contains(emotionKeyword)) {
            analysis.append("부정적인 감정 상태로 보입니다. ");
        }
        
        analysis.append("희망 활동 '").append(wishActivity).append("'을 통해 ");
        
        switch (wishActivity) {
            case "독서하기":
                analysis.append("지식과 문화를 향상시킬 수 있는 프로그램을 추천합니다.");
                break;
            case "요리하기":
                analysis.append("창의적이고 실용적인 활동 프로그램을 추천합니다.");
                break;
            case "그림그리기":
            case "사진찍기":
                analysis.append("예술적 표현과 창작 활동 프로그램을 추천합니다.");
                break;
            case "운동하기":
                analysis.append("건강 증진과 활력 회복 프로그램을 추천합니다.");
                break;
            case "영화보기":
            case "게임하기":
                analysis.append("여가와 문화 활동 프로그램을 추천합니다.");
                break;
            default:
                analysis.append("다양한 문화 활동 프로그램을 추천합니다.");
        }
        
        return analysis.toString();
    }
    
    /**
     * 성향과 감정 키워드를 기반으로 GPT에서 개인화된 추천 이유 생성
     * 성향 정보가 없어도 작동 (선택적 처리)
     */
    public String generatePersonalizedRecommendationReason(String emotionKeyword, String wishActivity, 
                                                          PersonalityType personalityType, WelfareProgram program) {
        try {
            String personalizedPrompt = createPersonalizedRecommendationPrompt(emotionKeyword, wishActivity, personalityType, program);
            return openAiService.generateSimpleResponse(personalizedPrompt);
        } catch (Exception e) {
            log.error("개인화된 추천 이유 생성 중 오류 발생: {}", e.getMessage());
            return generateFallbackRecommendationReason(emotionKeyword, wishActivity, personalityType, program);
        }
    }
    
    private String createPersonalizedRecommendationPrompt(String emotionKeyword, String wishActivity, 
                                                         PersonalityType personalityType, WelfareProgram program) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 정보를 바탕으로 이 문화 프로그램을 추천하는 개인화된 이유를 2-3문장으로 작성해주세요:\n\n");
        
        // 사용자 정보
        prompt.append("**사용자 정보:**\n");
        prompt.append("- 현재 감정: ").append(emotionKeyword).append("\n");
        prompt.append("- 하고 싶은 활동: ").append(wishActivity).append("\n");
        if (personalityType != null) {
            prompt.append("- 성향: ").append(personalityType.getFullName()).append(" (").append(personalityType.getNickname()).append(") - ");
            prompt.append(personalityType.getDescription()).append("\n");
        } else {
            prompt.append("- 성향: 미설정 (일반적인 접근으로 추천)\n");
        }
        prompt.append("\n");
        
        // 프로그램 정보
        prompt.append("**추천 프로그램:**\n");
        prompt.append("- 프로그램명: ").append(program.getTitle()).append("\n");
        prompt.append("- 대상: ").append(program.getTargetDescription() != null ? program.getTargetDescription() : "전체").append("\n");
        prompt.append("- 장소: ").append(program.getLocation() != null ? program.getLocation() : "동작구립도서관").append("\n");
        prompt.append("- 일정: ").append(program.getSchedule() != null ? program.getSchedule() : "상시 운영").append("\n");
        prompt.append("- 카테고리: ").append(program.getCategory()).append("\n");
        prompt.append("\n");
        
        prompt.append("**요청:**\n");
        if (personalityType != null) {
            prompt.append("사용자의 현재 감정 상태와 희망 활동, 그리고 성향을 고려하여 ");
            prompt.append("이 프로그램이 왜 이 사용자에게 특별히 도움이 될지 ");
            prompt.append("따뜻하고 공감적인 톤으로 설명해주세요. ");
            prompt.append("성향의 특성을 반영한 구체적인 효과나 장점을 포함해서 작성해주세요.");
        } else {
            prompt.append("사용자의 현재 감정 상태와 희망 활동을 중심으로 ");
            prompt.append("이 프로그램이 왜 도움이 될지 ");
            prompt.append("따뜻하고 공감적인 톤으로 설명해주세요. ");
            prompt.append("일반적이지만 구체적인 효과나 장점을 포함해서 작성해주세요.");
        }
        
        return prompt.toString();
    }
    
    private String generateFallbackRecommendationReason(String emotionKeyword, String wishActivity, 
                                                       PersonalityType personalityType, WelfareProgram program) {
        StringBuilder reason = new StringBuilder();
        
        // 감정 기반
        if (List.of("행복", "뿌듯함", "즐거움", "설렘", "여유로움", "활기참", "안도감", "차분함", "기특함").contains(emotionKeyword)) {
            reason.append("현재 긍정적인 감정 상태에서 ");
        } else {
            reason.append("현재 ").append(emotionKeyword).append(" 상태에서 ");
        }
        
        // 활동 연계
        reason.append("'").append(wishActivity).append("'에 대한 관심을 고려하여, ");
        
        // 성향 반영 (있을 때만)
        if (personalityType != null) {
            switch (personalityType) {
                case LOGICAL_BLUE:
                    reason.append("분석적이고 논리적인 성향에 맞는 ");
                    break;
                case EMOTIONAL_RED:
                    reason.append("감정적 소통을 중시하는 성향에 맞는 ");
                    break;
                case CONTROL_GRAY:
                    reason.append("리더십과 보호욕이 강한 성향에 맞는 ");
                    break;
                case INDEPENDENT_NAVY:
                    reason.append("자율성과 독립성을 중시하는 성향에 맞는 ");
                    break;
                case AFFECTIONATE_YELLOW:
                    reason.append("표현적이고 활동적인 성향에 맞는 ");
                    break;
                case INTROSPECTIVE_GREEN:
                    reason.append("내향적이고 사색적인 성향에 맞는 ");
                    break;
            }
        } else {
            reason.append("당신에게 도움이 될 수 있는 ");
        }
        
        reason.append(program.getTitle()).append("을(를) 추천드립니다. ");
        
        if (personalityType != null) {
            reason.append("이 프로그램을 통해 새로운 경험과 성장의 기회를 얻으실 수 있을 것입니다.");
        } else {
            reason.append("이 프로그램을 통해 원하시는 활동을 즐기며 긍정적인 변화를 경험하실 수 있을 것입니다.");
        }
        
        return reason.toString();
    }
}