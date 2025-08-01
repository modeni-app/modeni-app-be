package com.steam.modeni.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum EmotionKeyword {
    // 긍정 감정 (9개)
    HAPPY("행복", EmotionType.POSITIVE),
    PROUD("뿌듯함", EmotionType.POSITIVE),
    JOYFUL("즐거움", EmotionType.POSITIVE),
    EXCITED("설렘", EmotionType.POSITIVE),
    RELAXED("여유로움", EmotionType.POSITIVE),
    ENERGETIC("활기참", EmotionType.POSITIVE),
    RELIEVED("안도감", EmotionType.POSITIVE),
    CALM("차분함", EmotionType.POSITIVE),
    PLEASED("기특함", EmotionType.POSITIVE),
    
    // 부정 감정 (10개)
    DISAPPOINTED("서운함", EmotionType.NEGATIVE),
    ANXIOUS("불안함", EmotionType.NEGATIVE),
    ANNOYED("짜증남", EmotionType.NEGATIVE),
    IMPATIENT("초조함", EmotionType.NEGATIVE),
    REGRETFUL("실망", EmotionType.NEGATIVE),
    REMORSEFUL("후회", EmotionType.NEGATIVE),
    DEPRESSED("우울함", EmotionType.NEGATIVE),
    SAD("슬픔", EmotionType.NEGATIVE),
    TIRED("지침", EmotionType.NEGATIVE),
    FRUSTRATED("답답함", EmotionType.NEGATIVE);
    
    private final String koreanName;
    private final EmotionType type;
    
    public enum EmotionType {
        POSITIVE, NEGATIVE
    }
    
    /**
     * 한글 이름으로 EmotionKeyword 찾기
     */
    public static EmotionKeyword fromKoreanName(String koreanName) {
        return Arrays.stream(values())
                .filter(emotion -> emotion.getKoreanName().equals(koreanName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 긍정 감정 목록 반환
     */
    public static List<EmotionKeyword> getPositiveEmotions() {
        return Arrays.stream(values())
                .filter(emotion -> emotion.getType() == EmotionType.POSITIVE)
                .collect(Collectors.toList());
    }
    
    /**
     * 부정 감정 목록 반환
     */
    public static List<EmotionKeyword> getNegativeEmotions() {
        return Arrays.stream(values())
                .filter(emotion -> emotion.getType() == EmotionType.NEGATIVE)
                .collect(Collectors.toList());
    }
    
    /**
     * 긍정 감정 한글 이름 목록 반환
     */
    public static List<String> getPositiveEmotionNames() {
        return getPositiveEmotions().stream()
                .map(EmotionKeyword::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * 부정 감정 한글 이름 목록 반환
     */
    public static List<String> getNegativeEmotionNames() {
        return getNegativeEmotions().stream()
                .map(EmotionKeyword::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * 모든 감정 한글 이름 목록 반환
     */
    public static List<String> getAllEmotionNames() {
        return Arrays.stream(values())
                .map(EmotionKeyword::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * 감정이 긍정적인지 확인
     */
    public boolean isPositive() {
        return this.type == EmotionType.POSITIVE;
    }
    
    /**
     * 감정이 부정적인지 확인
     */
    public boolean isNegative() {
        return this.type == EmotionType.NEGATIVE;
    }
}
