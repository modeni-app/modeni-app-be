package com.steam.modeni.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum WishActivity {
    WALKING("산책하기", ActivityCategory.OUTDOOR),
    COOKING("요리하기", ActivityCategory.INDOOR),
    CLEANING("청소하기", ActivityCategory.INDOOR),
    READING("독서하기", ActivityCategory.INDOOR),
    DRAWING("그림그리기", ActivityCategory.CREATIVE),
    SINGING("노래부르기", ActivityCategory.CREATIVE),
    CAFE("카페가기", ActivityCategory.OUTDOOR),
    WRITING_DIARY("일기쓰기", ActivityCategory.INDOOR),
    EXERCISING("운동하기", ActivityCategory.OUTDOOR),
    TAKING_PHOTOS("사진찍기", ActivityCategory.CREATIVE),
    FLOWER_VIEWING("꽃구경", ActivityCategory.OUTDOOR),
    SLEEPING("잠자기", ActivityCategory.INDOOR),
    WATCHING_MOVIE("영화보기", ActivityCategory.INDOOR),
    RESTAURANT("맛집가기", ActivityCategory.OUTDOOR),
    SHOPPING("장보기", ActivityCategory.OUTDOOR),
    LISTENING_MUSIC("음악듣기", ActivityCategory.INDOOR),
    GAMING("게임하기", ActivityCategory.INDOOR);
    
    private final String koreanName;
    private final ActivityCategory category;
    
    public enum ActivityCategory {
        INDOOR("실내활동"),
        OUTDOOR("실외활동"), 
        CREATIVE("창작활동");
        
        @Getter
        private final String koreanName;
        
        ActivityCategory(String koreanName) {
            this.koreanName = koreanName;
        }
    }
    
    /**
     * 한글 이름으로 WishActivity 찾기
     */
    public static WishActivity fromKoreanName(String koreanName) {
        return Arrays.stream(values())
                .filter(activity -> activity.getKoreanName().equals(koreanName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 카테고리별 활동 목록 반환
     */
    public static List<WishActivity> getActivitiesByCategory(ActivityCategory category) {
        return Arrays.stream(values())
                .filter(activity -> activity.getCategory() == category)
                .collect(Collectors.toList());
    }
    
    /**
     * 실내 활동 목록 반환
     */
    public static List<WishActivity> getIndoorActivities() {
        return getActivitiesByCategory(ActivityCategory.INDOOR);
    }
    
    /**
     * 실외 활동 목록 반환
     */
    public static List<WishActivity> getOutdoorActivities() {
        return getActivitiesByCategory(ActivityCategory.OUTDOOR);
    }
    
    /**
     * 창작 활동 목록 반환
     */
    public static List<WishActivity> getCreativeActivities() {
        return getActivitiesByCategory(ActivityCategory.CREATIVE);
    }
    
    /**
     * 모든 활동 한글 이름 목록 반환
     */
    public static List<String> getAllActivityNames() {
        return Arrays.stream(values())
                .map(WishActivity::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 활동 한글 이름 목록 반환
     */
    public static List<String> getActivityNamesByCategory(ActivityCategory category) {
        return getActivitiesByCategory(category).stream()
                .map(WishActivity::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * 실내 활동인지 확인
     */
    public boolean isIndoor() {
        return this.category == ActivityCategory.INDOOR;
    }
    
    /**
     * 실외 활동인지 확인
     */
    public boolean isOutdoor() {
        return this.category == ActivityCategory.OUTDOOR;
    }
    
    /**
     * 창작 활동인지 확인
     */
    public boolean isCreative() {
        return this.category == ActivityCategory.CREATIVE;
    }
}
