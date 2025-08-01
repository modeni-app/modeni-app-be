package com.steam.modeni.service;

import com.steam.modeni.domain.entity.WelfareProgram;
import com.steam.modeni.domain.enums.City;
import com.steam.modeni.repository.WelfareProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelfareProgramDataLoader implements ApplicationRunner {

    private final WelfareProgramRepository welfareProgramRepository;

    private final CsvDataLoaderService csvDataLoaderService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (welfareProgramRepository.count() == 0) {
            // 1. 실제 동작구 도서관 문화 프로그램 로드
            csvDataLoaderService.loadDongjakLibraryCourses();
            
            // 2. 추가 샘플 데이터 로드
            loadSampleWelfarePrograms();
            
            log.info("복지 프로그램 데이터 로딩 완료 (총 {}개)", welfareProgramRepository.count());
        }
    }

    private void loadSampleWelfarePrograms() {
        List<WelfareProgram> programs = List.of(
            createProgram("청소년 심리 상담 서비스", "청소년을 위한 무료 심리 상담 및 멘탈 케어 프로그램", 
                         "서울시 청소년 상담복지센터", "상담", City.SEOUL, 13, 19, 
                         "우울,스트레스,불안,고민", "https://example.com/youth-counseling", "02-1234-5678"),
            
            createProgram("문화 예술 체험 프로그램", "가족이 함께 참여할 수 있는 다양한 문화 예술 활동", 
                         "서울문화재단", "문화", City.SEOUL, null, null, 
                         "행복,흥미,가족,추억", "https://example.com/culture", "02-2345-6789"),
            
            createProgram("청년 취업 지원 프로그램", "20-30대 청년을 위한 취업 준비 및 진로 상담", 
                         "고용노동부", "취업", City.SEOUL, 20, 35, 
                         "취업,진로,고민,미래", "https://example.com/job-support", "02-3456-7890"),
            
            createProgram("가족 운동 교실", "가족이 함께 참여하는 건강 증진 운동 프로그램", 
                         "국민체육진흥공단", "운동", City.SEOUL, null, null, 
                         "건강,가족,활력,운동", "https://example.com/family-sports", "02-4567-8901"),
            
            createProgram("부모 교육 프로그램", "자녀 양육 및 교육을 위한 부모 교육 과정", 
                         "서울시 건강가정지원센터", "교육", City.SEOUL, 30, 50, 
                         "교육,양육,고민,스트레스", "https://example.com/parent-education", "02-5678-9012"),
            
            createProgram("청소년 문화 체험", "청소년을 위한 다양한 문화 체험 및 여가 활동", 
                         "청소년활동진흥원", "문화", City.BUSAN, 13, 19, 
                         "흥미,체험,친구,즐거움", "https://example.com/youth-culture", "051-1234-5678"),
            
            createProgram("성인 재교육 프로그램", "직업 전환 및 평생 교육을 위한 성인 교육", 
                         "평생교육진흥원", "교육", City.DAEGU, 25, 45, 
                         "교육,성장,도전,미래", "https://example.com/adult-education", "053-2345-6789"),
            
            createProgram("정신건강 상담 서비스", "스트레스 및 우울증 관리를 위한 전문 상담", 
                         "정신건강복지센터", "상담", City.INCHEON, 18, null, 
                         "우울,스트레스,불안,치료", "https://example.com/mental-health", "032-3456-7890"),
            
            createProgram("시니어 여가 프로그램", "50대 이상을 위한 다양한 여가 및 취미 활동", 
                         "시니어클럽", "여가", City.GWANGJU, 50, null, 
                         "여가,취미,건강,사교", "https://example.com/senior-leisure", "062-4567-8901"),
            
            createProgram("가족 캠핑 체험", "자연 속에서 가족이 함께하는 힐링 캠핑 프로그램", 
                         "산림청", "여가", City.DAEJEON, null, null, 
                         "힐링,가족,자연,휴식", "https://example.com/family-camping", "042-5678-9012")
        );

        welfareProgramRepository.saveAll(programs);
    }

    private WelfareProgram createProgram(String title, String description, String organization, 
                                       String category, City targetCity, Integer ageMin, Integer ageMax, 
                                       String emotionKeywords, String applicationUrl, String contactNumber) {
        WelfareProgram program = new WelfareProgram();
        program.setTitle(title);
        program.setDescription(description);
        program.setOrganization(organization);
        program.setCategory(category);
        program.setTargetCity(targetCity);
        program.setTargetAgeMin(ageMin);
        program.setTargetAgeMax(ageMax);
        program.setEmotionKeywords(emotionKeywords);
        program.setApplicationUrl(applicationUrl);
        program.setContactNumber(contactNumber);
        program.setIsActive(true);
        return program;
    }
}