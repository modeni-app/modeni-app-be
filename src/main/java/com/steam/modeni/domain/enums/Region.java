package com.steam.modeni.domain.enums;

public enum Region {
    // 특별시/광역시
    SEOUL("서울시"),
    BUSAN("부산시"),
    DAEGU("대구시"),
    INCHEON("인천시"),
    GWANGJU("광주시"),
    DAEJEON("대전시"),
    ULSAN("울산시"),
    SEJONG("세종시"),
    
    // 경기도 주요 시
    SUWON("수원시"),
    SEONGNAM("성남시"),
    YONGIN("용인시"),
    ANYANG("안양시"),
    ANSAN("안산시"),
    GOYANG("고양시"),
    HWASEONG("화성시"),
    BUCHEON("부천시"),
    GIMPO("김포시"),
    SIHEUNG("시흥시"),
    
    // 강원도 주요 시
    CHUNCHEON("춘천시"),
    WONJU("원주시"),
    GANGNEUNG("강릉시"),
    
    // 충청도 주요 시
    CHEONGJU("청주시"),
    CHEONAN("천안시"),
    ASAN("아산시"),
    
    // 전라도 주요 시
    JEONJU("전주시"),
    IKSAN("익산시"),
    GUNSAN("군산시"),
    YEOSU("여수시"),
    SUNCHEON("순천시"),
    MOKPO("목포시"),
    
    // 경상도 주요 시
    POHANG("포항시"),
    GYEONGJU("경주시"),
    GIMHAE("김해시"),
    CHANGWON("창원시"),
    JINJU("진주시"),
    
    // 제주도
    JEJU("제주시"),
    SEOGWIPO("서귀포시");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}