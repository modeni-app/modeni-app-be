package com.steam.modeni.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class DiaryRequest {
    private String content; // 선택사항 (텍스트 입력)
    
    // 감정 키워드 복수 선택 (최대 3개)
    @NotEmpty(message = "감정을 최소 1개 이상 선택해주세요.")
    @Size(max = 3, message = "감정은 최대 3개까지 선택 가능합니다.")
    private List<String> emotionKeywords; // ["행복", "뿌듯함", "즐거움"]
    
    // 희망활동 복수 선택 (최대 2개)
    @NotEmpty(message = "희망활동을 최소 1개 이상 선택해주세요.")
    @Size(max = 2, message = "희망활동은 최대 2개까지 선택 가능합니다.")
    private List<String> wishActivities; // ["산책하기", "요리하기"]
    
    // === 하위 호환성을 위한 단일 값 지원 (Deprecated) ===
    
    @Deprecated
    public String getEmotionKeyword() {
        return emotionKeywords != null && !emotionKeywords.isEmpty() ? emotionKeywords.get(0) : null;
    }
    
    @Deprecated
    public void setEmotionKeyword(String emotionKeyword) {
        this.emotionKeywords = emotionKeyword != null ? List.of(emotionKeyword) : null;
    }
    
    @Deprecated
    public String getWishActivity() {
        return wishActivities != null && !wishActivities.isEmpty() ? wishActivities.get(0) : null;
    }
    
    @Deprecated
    public void setWishActivity(String wishActivity) {
        this.wishActivities = wishActivity != null ? List.of(wishActivity) : null;
    }
}