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
} 