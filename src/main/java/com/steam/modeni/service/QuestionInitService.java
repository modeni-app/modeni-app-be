package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionInitService implements ApplicationRunner {
    
    private final QuestionRepository questionRepository;
    
    private static final List<String> INITIAL_QUESTIONS = Arrays.asList(
        "우리 가족의 숨겨진 재능은 무엇이라고 생각하나요?",
        "스트레스를 풀 때 주로 무엇을 하나요?",
        "당신에게 있어서 가족이란 어떤 존재라고 생각하나요?",
        "가족들과 보낸 시간 중에 가장 행복했던 기억은 무엇인가요?",
        "가족들이 각각 닮은 동물과 그 이유는 무엇인가요?",
        "힘들거나 지칠 때 어떤 말이나 행동이 가장 큰 힘이 되나요?",
        "어린 시절 가장 기억에 남는 가족과의 추억은 무엇인가요?",
        "우리 가족이 함께 가보고 싶은 여행지가 있다면 어디인가요? 이유도 함께 말해주세요.",
        "가족끼리 하루 동안 아무 제약 없이 놀 수 있다면 무엇을 하고 싶나요?",
        "우리 가족 중, 오늘 가장 고마웠던 사람은 누구인가요?",
        "요즘 마음이 힘들 땐, 어떤 말이나 행동이 가장 위로가 되나요?"
    );
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeQuestions();
    }
    
    private void initializeQuestions() {
        // 이미 질문이 있다면 초기화하지 않음
        if (questionRepository.count() > 0) {
            return;
        }
        
        // 시스템 질문 생성 (familyCode = "SYSTEM")
        for (int i = 0; i < INITIAL_QUESTIONS.size(); i++) {
            Question question = new Question();
            question.setContent(INITIAL_QUESTIONS.get(i));
            question.setFamilyCode("SYSTEM"); // 시스템 질문은 familyCode = "SYSTEM"
            questionRepository.save(question);
        }
        
        System.out.println("✅ 초기 질문 데이터가 성공적으로 저장되었습니다. (총 " + INITIAL_QUESTIONS.size() + "개)");
    }
}