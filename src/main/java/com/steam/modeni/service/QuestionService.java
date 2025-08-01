package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.repository.AnswerRepository;
import com.steam.modeni.repository.QuestionRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    
    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAllByOrderByIdAsc();
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
        
        Long familyCode = user.getFamilyCode();
        
        // 시스템 질문(familyCode = 0) + 해당 가족의 질문들 조회
        List<Question> questions = questionRepository.findByFamilyCodeOrFamilyCode(0L, familyCode);
        
        // 사용자 가입일 이후의 질문만 필터링
        return questions.stream()
                .filter(question -> question.getCreatedAt().isAfter(user.getCreatedAt()) || 
                                  question.getCreatedAt().isEqual(user.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForFamily(Long familyCode) {
        // 시스템 질문(familyCode = 0) + 해당 가족의 질문들 조회
        return questionRepository.findByFamilyCodeOrFamilyCode(0L, familyCode);
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAnsweredQuestionsByFamily(Long familyCode) {
        // 가족 구성원들이 답변한 질문들만 조회 (중복 제거)
        List<Question> questions = answerRepository.findDistinctQuestionsByFamilyCode(familyCode);
        
        // familyCode를 실제 가족 코드로 변환하여 반환
        return questions.stream()
                .map(question -> convertQuestionForFamily(question, familyCode))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAnsweredQuestionsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 특정 사용자가 답변한 질문들만 조회 (중복 제거)
        List<Question> questions = answerRepository.findDistinctQuestionsByUser(user);
        
        // familyCode를 실제 가족 코드로 변환하여 반환
        return questions.stream()
                .map(question -> convertQuestionForFamily(question, user.getFamilyCode()))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Question getRandomQuestionForFamily(Long familyCode) {
        // 가족 구성원 조회
        List<User> familyMembers = userRepository.findByFamilyCode(familyCode);
        if (familyMembers.isEmpty()) {
            throw new RuntimeException("가족 구성원을 찾을 수 없습니다.");
        }
        
        // 전체 질문 조회
        List<Question> allQuestions = questionRepository.findAllByOrderByIdAsc();
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("질문을 찾을 수 없습니다.");
        }
        
        // 가족 구성원들이 답변하지 않은 질문들 필터링
        List<Question> unansweredQuestions = allQuestions.stream()
                .filter(question -> {
                    // 이 질문에 대해 가족 구성원 중 누구도 답변하지 않았는지 확인
                    return familyMembers.stream().noneMatch(user -> 
                        answerRepository.existsByQuestionAndUser(question, user));
                })
                .collect(Collectors.toList());
        
        if (unansweredQuestions.isEmpty()) {
            throw new RuntimeException("모든 질문에 대해 답변이 완료되었습니다.");
        }
        
        // 랜덤하게 질문 선택
        Random random = new Random();
        return unansweredQuestions.get(random.nextInt(unansweredQuestions.size()));
    }
    
    private Question convertQuestionForFamily(Question question, Long familyCode) {
        // familyCode를 실제 가족 코드로 변환
        question.setFamilyCode(familyCode);
        return question;
    }
} 