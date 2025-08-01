package com.steam.modeni.domain.enums;

public enum PersonalityType {
    LOGICAL_BLUE("이성적 분석형", "파랑이", "감정보다는 논리 중심, 갈등을 해결하려 함"),
    EMOTIONAL_RED("감정 공감형", "빨강이", "정 교류 중시, 상처에도 예민"),
    CONTROL_GRAY("통제 보호형", "회색이", "통제, 지도에 익숙하고 보호욕 강함"),
    INDEPENDENT_NAVY("자율 독립형", "남색이", "자기 선택을 중요시하고 간섭을 싫어함"),
    AFFECTIONATE_YELLOW("애정 표현형", "노랑이", "자주 표현하고 스킨십/말로 사랑을 전달"),
    INTROSPECTIVE_GREEN("내면형", "초록이", "표현은 적지만 속은 깊음, 혼자 해결하려 함");

    private final String fullName;
    private final String nickname;
    private final String description;

    PersonalityType(String fullName, String nickname, String description) {
        this.fullName = fullName;
        this.nickname = nickname;
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDescription() {
        return description;
    }
}