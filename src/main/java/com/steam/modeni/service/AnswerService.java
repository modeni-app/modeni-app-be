package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Answer;
import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.AnswerResponse;
import com.steam.modeni.repository.AnswerRepository;
import com.steam.modeni.repository.QuestionRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerService {
    
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final DailyQuestionService dailyQuestionService;
    
    public Map<String, Object> createAnswer(Long questionId, Long userId, String content) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 요청 데이터 검증
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("답변 내용은 필수입니다.");
        }
        
        // 사용자의 가족 코드 확인
        String familyCode = user.getFamilyCode();
        
        // 질문이 시스템 질문이거나 사용자의 가족 질문인지 확인
        if (question.getFamilyCode() != null && !question.getFamilyCode().equals(familyCode) && !question.getFamilyCode().equals(0L)) {
            throw new RuntimeException("이 질문은 다른 가족의 질문입니다.");
        }
        
        // 이미 답변했는지 확인
        if (answerRepository.existsByQuestionAndUser(question, user)) {
            throw new RuntimeException("이미 이 질문에 답변하셨습니다. 한 질문당 한 번만 답변 가능합니다.");
        }
        
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setUser(user);
        answer.setContent(content);
        
        Answer savedAnswer = answerRepository.save(answer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedAnswer.getId());
        response.put("created_at", savedAnswer.getCreatedAt());
        response.put("message", "답변이 성공적으로 등록되었습니다.");
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public AnswerResponse getAnswerById(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        
        return convertToAnswerResponse(answer);
    }
    
    public Map<String, Object> updateAnswer(Long id, String content) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        
        answer.setContent(content);
        answerRepository.save(answer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "답변이 성공적으로 수정되었습니다.");
        return response;
    }
    
    public Map<String, Object> deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        
        answerRepository.delete(answer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "답변이 성공적으로 삭제되었습니다.");
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));
        
        List<Answer> answers = answerRepository.findByQuestionOrderByCreatedAtAsc(question);
        return answers.stream()
                .map(this::convertToAnswerResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AnswerResponse> getTodayAnswersForFamily(String familyCode) {
        Question todayQuestion = dailyQuestionService.getTodayQuestionForFamily(familyCode);
        if (todayQuestion == null) {
            throw new RuntimeException("오늘의 질문을 찾을 수 없습니다.");
        }
        
        List<Answer> answers = answerRepository.findByQuestionOrderByCreatedAtAsc(todayQuestion);
        // 같은 가족의 답변만 필터링
        return answers.stream()
                .filter(answer -> answer.getUser().getFamilyCode().equals(familyCode))
                .map(this::convertToAnswerResponse)
                .collect(Collectors.toList());
    }
    
    private AnswerResponse convertToAnswerResponse(Answer answer) {
        AnswerResponse.UserInfo userInfo = new AnswerResponse.UserInfo(
                answer.getUser().getId(),
                answer.getUser().getName(),
                answer.getUser().getUserId()
        );
        
        return new AnswerResponse(
                answer.getId(),
                answer.getContent(),
                answer.getCreatedAt(),
                userInfo,
                answer.getQuestion().getId()
        );
    }
} 