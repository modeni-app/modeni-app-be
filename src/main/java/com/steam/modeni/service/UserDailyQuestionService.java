package com.steam.modeni.service;

import com.steam.modeni.domain.entity.Question;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.domain.entity.UserDailyQuestion;
import com.steam.modeni.repository.QuestionRepository;
import com.steam.modeni.repository.UserDailyQuestionRepository;
import com.steam.modeni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDailyQuestionService {
    
    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    
    /**
     * 사용자의 오늘 질문 조회 (없으면 생성)
     */
    public UserDailyQuestion getTodayQuestionForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        LocalDate today = LocalDate.now();
        
        // 오늘 질문이 이미 있는지 확인
        Optional<UserDailyQuestion> existingQuestion = 
                userDailyQuestionRepository.findByUserAndQuestionDate(user, today);
        
        if (existingQuestion.isPresent()) {
            return existingQuestion.get();
        }
        
        // 오늘 질문이 없으면 새로 생성
        return createDailyQuestionForUser(user, today);
    }
    
    /**
     * 특정 사용자의 모든 질문 이력 조회
     */
    @Transactional(readOnly = true)
    public List<UserDailyQuestion> getQuestionHistoryForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return userDailyQuestionRepository.findByUserOrderByQuestionDateAsc(user);
    }
    
    /**
     * 가족별 질문 이력 조회 (같은 가족은 동일한 질문을 받으므로 효율적)
     */
    @Transactional
    public List<UserDailyQuestion> getQuestionHistoryForFamily(Long familyCode) {
        // 해당 가족의 첫 번째 사용자를 찾아서 그 사용자의 질문 이력을 조회
        List<User> familyMembers = userRepository.findByFamilyCode(familyCode);
        if (familyMembers.isEmpty()) {
            throw new RuntimeException("해당 가족을 찾을 수 없습니다.");
        }
        
        // 가장 먼저 가입한 사용자의 질문 이력을 기준으로 조회
        User oldestMember = familyMembers.stream()
                .min((u1, u2) -> u1.getCreatedAt().compareTo(u2.getCreatedAt()))
                .orElseThrow(() -> new RuntimeException("가족 구성원을 찾을 수 없습니다."));
        
        // 먼저 누락된 질문들을 생성 (가입일~오늘까지)
        generateMissingQuestionsForUser(oldestMember.getId());
        
        // 그 다음 전체 질문 이력 조회 (오늘 질문 포함)
        return userDailyQuestionRepository.findByUserOrderByQuestionDateAsc(oldestMember);
    }
    
    /**
     * 가족별 특정 기간 질문 이력 조회
     */
    @Transactional(readOnly = true)
    public List<UserDailyQuestion> getQuestionHistoryForFamilyByDateRange(Long familyCode, 
                                                                          LocalDate startDate, 
                                                                          LocalDate endDate) {
        List<User> familyMembers = userRepository.findByFamilyCode(familyCode);
        if (familyMembers.isEmpty()) {
            throw new RuntimeException("해당 가족을 찾을 수 없습니다.");
        }
        
        // 가장 먼저 가입한 사용자의 질문 이력을 기준으로 조회
        User oldestMember = familyMembers.stream()
                .min((u1, u2) -> u1.getCreatedAt().compareTo(u2.getCreatedAt()))
                .orElseThrow(() -> new RuntimeException("가족 구성원을 찾을 수 없습니다."));
        
        return userDailyQuestionRepository.findByUserAndDateRange(oldestMember, startDate, endDate);
    }
    
    /**
     * 사용자의 특정 기간 질문 이력 조회
     */
    @Transactional(readOnly = true)
    public List<UserDailyQuestion> getQuestionHistoryForUserByDateRange(Long userId, 
                                                                        LocalDate startDate, 
                                                                        LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return userDailyQuestionRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    /**
     * 가족의 누락된 질문들 생성 (가족 중 가장 먼저 가입한 사용자 기준)
     */
    public void generateMissingQuestionsForFamily(Long familyCode) {
        List<User> familyMembers = userRepository.findByFamilyCode(familyCode);
        if (familyMembers.isEmpty()) {
            throw new RuntimeException("해당 가족을 찾을 수 없습니다.");
        }
        
        // 가장 먼저 가입한 사용자를 기준으로 질문 생성
        User oldestMember = familyMembers.stream()
                .min((u1, u2) -> u1.getCreatedAt().compareTo(u2.getCreatedAt()))
                .orElseThrow(() -> new RuntimeException("가족 구성원을 찾을 수 없습니다."));
        
        generateMissingQuestionsForUser(oldestMember.getId());
    }
    
    /**
     * 사용자의 가입일부터 오늘까지 누락된 질문들을 생성
     */
    public void generateMissingQuestionsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        LocalDate userJoinDate = user.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        
        // 가입일부터 오늘까지 각 날짜에 대해 질문 생성
        LocalDate currentDate = userJoinDate;
        while (!currentDate.isAfter(today)) {
            Optional<UserDailyQuestion> existing = 
                    userDailyQuestionRepository.findByUserAndQuestionDate(user, currentDate);
            
            if (existing.isEmpty()) {
                createDailyQuestionForUser(user, currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
        }
    }
    
    /**
     * 특정 날짜의 사용자 질문 생성
     */
    private UserDailyQuestion createDailyQuestionForUser(User user, LocalDate questionDate) {
        // 가입일로부터 며칠째인지 계산
        LocalDate userJoinDate = user.getCreatedAt().toLocalDate();
        int dayNumber = (int) ChronoUnit.DAYS.between(userJoinDate, questionDate) + 1;
        
        // 시스템 질문들 조회
        List<Question> allQuestions = questionRepository.findByFamilyCode(0L);
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("시스템 질문을 찾을 수 없습니다.");
        }
        
        // 가족 코드와 날짜를 기반으로 한 시드를 사용하여 같은 가족은 같은 질문을 받도록 함
        long seed = user.getFamilyCode() * 1000L + questionDate.toEpochDay();
        Random random = new Random(seed);
        
        Question selectedQuestion = allQuestions.get(random.nextInt(allQuestions.size()));
        
        // UserDailyQuestion 생성 및 저장
        UserDailyQuestion userDailyQuestion = new UserDailyQuestion(
                user, selectedQuestion, questionDate, dayNumber);
        
        UserDailyQuestion saved = userDailyQuestionRepository.save(userDailyQuestion);
        
        System.out.println("📅 사용자 " + user.getUserId() + "의 " + questionDate + " 질문이 생성되었습니다: " 
                         + selectedQuestion.getContent());
        
        return saved;
    }
}
