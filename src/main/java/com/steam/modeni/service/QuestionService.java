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
        return questionRepository.findAllByOrderByIdAsc();
    }
    
    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));
    }
    
    @Transactional(readOnly = true)
    public Question getRandomQuestionForFamily(Long familyId) {
        // TODO: 임시 구현 - 나중에 가족별 맞춤 질문 로직으로 개선 필요
        List<Question> allQuestions = questionRepository.findAllByOrderByIdAsc();
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("사용 가능한 질문이 없습니다.");
        }
        
        Random random = new Random();
        return allQuestions.get(random.nextInt(allQuestions.size()));
    }
}
