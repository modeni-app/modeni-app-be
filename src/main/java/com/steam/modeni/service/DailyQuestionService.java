package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
    
    // 가족별 일일 질문 저장 (familyId -> Question)
    private final Map<Long, Question> familyDailyQuestions = new HashMap<>();
    // 가족별 질문 날짜 저장 (familyId -> LocalDate)
    private final Map<Long, LocalDate> familyQuestionDates = new HashMap<>();
    
    /**
     * 매일 오전 9시에 모든 가족의 새로운 질문 선택
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void selectDailyQuestions() {
        LocalDate today = LocalDate.now();
        
        // 모든 가족의 질문 날짜를 오늘로 갱신 (새로운 질문 선택을 위해)
        for (Long familyId : familyDailyQuestions.keySet()) {
            familyQuestionDates.put(familyId, today.minusDays(1)); // 어제로 설정하여 새 질문 선택 유도
        }
        
        System.out.println("🌅 모든 가족의 일일 질문이 갱신 준비되었습니다.");
    }
    
    /**
     * 가족별 일일 질문 조회
     */
    @Transactional(readOnly = true)
    public Question getTodayQuestionForFamily(Long familyId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime nineAM = LocalTime.of(9, 0);
        
        // 현재 가족의 질문 날짜 확인
        LocalDate familyQuestionDate = familyQuestionDates.get(familyId);
        Question familyQuestion = familyDailyQuestions.get(familyId);
        
        // 질문이 없거나 날짜가 다른 경우 새로운 질문 선택
        boolean needNewQuestion = false;
        
        if (familyQuestion == null || familyQuestionDate == null) {
            needNewQuestion = true;
        } else if (!today.equals(familyQuestionDate)) {
            // 날짜가 바뀐 경우
            if (now.isAfter(nineAM) || now.equals(nineAM)) {
                // 오전 9시 이후면 새로운 질문
                needNewQuestion = true;
            }
            // 오전 9시 이전이면 어제 질문 유지
        }
        
        if (needNewQuestion) {
            selectNewQuestionForFamily(familyId);
            familyQuestion = familyDailyQuestions.get(familyId);
        }
        
        return familyQuestion;
    }
    
    /**
     * 특정 가족을 위한 새로운 질문 선택
     */
    private void selectNewQuestionForFamily(Long familyId) {
        List<Question> allQuestions = questionRepository.findAllByOrderByIdAsc();
        if (!allQuestions.isEmpty()) {
            // 가족 ID를 기반으로 한 시드를 사용하여 같은 가족은 항상 같은 질문을 받도록 함
            LocalDate today = LocalDate.now();
            long seed = familyId * 1000L + today.toEpochDay();
            Random random = new Random(seed);
            
            Question selectedQuestion = allQuestions.get(random.nextInt(allQuestions.size()));
            
            familyDailyQuestions.put(familyId, selectedQuestion);
            familyQuestionDates.put(familyId, today);
            
            System.out.println("🎯 가족 " + familyId + "의 오늘 질문이 선택되었습니다: " + selectedQuestion.getContent());
        }
    }
    
    /**
     * 특정 가족의 특정 질문이 오늘의 질문인지 확인
     */
    public boolean isQuestionForTodayAndFamily(Question question, Long familyId) {
        Question todayQuestion = getTodayQuestionForFamily(familyId);
        return todayQuestion != null && todayQuestion.getId().equals(question.getId());
    }
} 