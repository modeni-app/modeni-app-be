package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyQuestionService {
    
    private final QuestionRepository questionRepository;
    
    // 가족별 일일 질문을 저장하는 메모리 캐시
    private final Map<Long, Question> familyDailyQuestions = new HashMap<>();
    private final Map<Long, LocalDate> familyQuestionDates = new HashMap<>();
    
    /**
     * 특정 질문이 특정 가족의 오늘 질문인지 확인
     */
    public boolean isQuestionForTodayAndFamily(Question question, Long familyCode) {
        Question todayQuestion = getTodayQuestionForFamily(familyCode);
        return todayQuestion != null && todayQuestion.getId().equals(question.getId());
    }
    
    /**
     * 특정 가족의 오늘 질문 조회
     */
    public Question getTodayQuestionForFamily(Long familyCode) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalTime nineAM = LocalTime.of(9, 0);
        
        Question familyQuestion = familyDailyQuestions.get(familyCode);
        LocalDate familyQuestionDate = familyQuestionDates.get(familyCode);
        
        boolean needNewQuestion = false;
        
        // 첫 번째 요청이거나 질문이 없는 경우
        if (familyQuestion == null || familyQuestionDate == null) {
            needNewQuestion = true;
        } else if (!today.equals(familyQuestionDate)) {
            // 날짜가 바뀐 경우
            if (currentTime.isAfter(nineAM) || currentTime.equals(nineAM)) {
                // 오전 9시 이후면 새로운 질문
                needNewQuestion = true;
            }
            // 오전 9시 이전이면 어제 질문 유지
        }
        
        if (needNewQuestion) {
            selectNewQuestionForFamily(familyCode);
            familyQuestion = familyDailyQuestions.get(familyCode);
        }
        
        // 질문이 있다면 가족 코드를 실제 가족 코드로 설정하여 새 객체 반환
        if (familyQuestion != null) {
            Question responseQuestion = new Question();
            responseQuestion.setId(familyQuestion.getId());
            responseQuestion.setContent(familyQuestion.getContent());
            responseQuestion.setFamilyCode(familyCode); // 실제 가족 코드로 설정
            responseQuestion.setCreatedAt(familyQuestion.getCreatedAt());
            return responseQuestion;
        }
        
        return null;
    }
    
    /**
     * 특정 가족을 위한 새로운 질문 선택
     */
    private void selectNewQuestionForFamily(Long familyCode) {
        List<Question> allQuestions = questionRepository.findAllByOrderByIdAsc();
        if (!allQuestions.isEmpty()) {
            // 가족 코드를 기반으로 한 시드를 사용하여 같은 가족은 항상 같은 질문을 받도록 함
            LocalDate today = LocalDate.now();
            long seed = familyCode * 1000L + today.toEpochDay();
            Random random = new Random(seed);
            
            Question selectedQuestion = allQuestions.get(random.nextInt(allQuestions.size()));
            
            familyDailyQuestions.put(familyCode, selectedQuestion);
            familyQuestionDates.put(familyCode, today);
            
            System.out.println("🎯 가족 " + familyCode + "의 오늘 질문이 선택되었습니다: " + selectedQuestion.getContent());
        }
    }
}