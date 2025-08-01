package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    
    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));
    }
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String familyCode = user.getFamilyCode();
        
        // 시스템 질문(familyCode = "SYSTEM") + 해당 가족의 질문들 조회
        List<Question> questions = questionRepository.findByFamilyCodeOrFamilyCode("SYSTEM", familyCode);
        
        // 사용자 가입일 이후의 질문만 필터링
        return questions.stream()
                .filter(question -> question.getCreatedAt().isAfter(user.getCreatedAt()) || 
                                  question.getCreatedAt().isEqual(user.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForFamily(String familyCode) {
        // 시스템 질문(familyCode = "SYSTEM") + 해당 가족의 질문들 조회
        return questionRepository.findByFamilyCodeOrFamilyCode("SYSTEM", familyCode);
    }
    
    @Transactional(readOnly = true)
    public Question getRandomQuestionForFamily(Long familyId) {
        // 전체 질문 조회
        List<Question> allQuestions = questionRepository.findAll();
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("질문을 찾을 수 없습니다.");
        }
        
        // 가족 ID 기반으로 랜덤 질문 선택
        Random random = new Random(familyId);
        return allQuestions.get(random.nextInt(allQuestions.size()));
    }
    
    private Question convertQuestionForFamily(Question question, String familyCode) {
        Question convertedQuestion = new Question();
        convertedQuestion.setId(question.getId());
        convertedQuestion.setContent(question.getContent());
        convertedQuestion.setCreatedAt(question.getCreatedAt());
        convertedQuestion.setFamilyCode(familyCode);
        return convertedQuestion;
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAnsweredQuestionsByFamily(String familyCode) {
        // 해당 가족의 답변이 있는 질문들 조회
        return questionRepository.findQuestionsWithAnswersByFamilyCode(familyCode);
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAnsweredQuestionsByUser(Long userId) {
        // 해당 사용자가 답변한 질문들 조회
        return questionRepository.findQuestionsWithAnswersByUserId(userId);
    }
}