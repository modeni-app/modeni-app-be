package com.steam.modeni.dto;

import lombok.Data;

@Data
public class DiaryRequest {
    private String content; // 선택사항 (텍스트 입력 고려하지 않음)
    private String emotionKeyword; // 버튼 선택: 행복, 뿌듯함, 즐거움, 설렘, 여유로움, 활기참, 안도감, 차분함, 기특함, 서운함, 불안함, 짜증남, 초조함, 실망, 후회, 우울함, 슬픔, 지침, 답답함
    private String wishActivity; // 버튼 선택: 산책하기, 요리하기, 청소하기, 독서하기, 그림그리기, 노래부르기, 카페가기, 일기쓰기, 운동하기, 사진찍기, 꽃구경, 잠자기, 영화보기, 맛집가기, 장보기, 음악듣기, 게임하기
}