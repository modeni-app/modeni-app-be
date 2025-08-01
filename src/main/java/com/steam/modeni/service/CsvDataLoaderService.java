package com.steam.modeni.service;

import com.steam.modeni.domain.entity.WelfareProgram;
import com.steam.modeni.domain.enums.Region;
import com.steam.modeni.repository.WelfareProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvDataLoaderService {

    private final WelfareProgramRepository welfareProgramRepository;

    public void loadDongjakLibraryCourses() {
        try {
            ClassPathResource resource = new ClassPathResource("cultural_programs.csv");
            List<WelfareProgram> programs = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // 헤더 스킵
                    }
                    
                    WelfareProgram program = parseCsvLine(line);
                    if (program != null) {
                        programs.add(program);
                    }
                }
            }

            welfareProgramRepository.saveAll(programs);
            log.info("동작구 도서관 문화 프로그램 {}개 로딩 완료", programs.size());

        } catch (Exception e) {
            log.error("CSV 파일 로딩 실패: {}", e.getMessage(), e);
        }
    }

    private WelfareProgram parseCsvLine(String line) {
        try {
            // CSV 파싱 (따옴표 내부의 콤마 처리)
            List<String> fields = parseCsvFields(line);
            
            if (fields.size() < 9) {
                log.warn("CSV 라인의 필드 수가 부족합니다 ({}개): {}", fields.size(), line);
                return null;
            }

            // CSV 컬럼: course_name,original_link,actual_link,group_idx,category_idx,teach_idx,target,location,schedule
            String courseName = fields.get(0).trim();
            String originalLink = fields.get(1).trim();
            String actualLink = fields.get(2).trim();
            String groupIdx = fields.get(3).trim();
            String categoryIdx = fields.get(4).trim();
            String teachIdx = fields.get(5).trim();
            String target = fields.get(6).trim();
            String location = fields.get(7).trim();
            String schedule = fields.get(8).trim();

            WelfareProgram program = new WelfareProgram();
            program.setTitle(courseName);
            program.setDescription(generateDescription(target, location, schedule));
            program.setOrganization("동작구립도서관");
            program.setCategory("문화");
            program.setTargetCity(Region.SEOUL);
            
            // 대상 연령 파싱
            setTargetAge(program, target);
            
            // 감정 키워드 설정
            program.setEmotionKeywords(generateEmotionKeywords(courseName, target));
            
            program.setApplicationUrl(actualLink);
            program.setContactNumber("02-820-1666"); // 동작구립도서관 대표번호
            program.setTargetDescription(target);
            program.setLocation(location);
            program.setSchedule(schedule);
            program.setIsActive(true);

            return program;

        } catch (Exception e) {
            log.warn("CSV 라인 파싱 실패: {}", line, e);
            return null;
        }
    }

    private List<String> parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // 마지막 필드 추가
        fields.add(currentField.toString().trim());
        return fields;
    }

    private void setTargetAge(WelfareProgram program, String target) {
        log.debug("대상 정보 파싱: {}", target);
        
        // 1. 직접적인 나이 범위 파싱 (예: "4~6세", "19~21년생")
        Pattern ageRangePattern = Pattern.compile("(\\d+)\\s*~\\s*(\\d+)\\s*세");
        Matcher ageRangeMatcher = ageRangePattern.matcher(target);
        if (ageRangeMatcher.find()) {
            int minAge = Integer.parseInt(ageRangeMatcher.group(1));
            int maxAge = Integer.parseInt(ageRangeMatcher.group(2));
            program.setTargetAgeMin(minAge);
            program.setTargetAgeMax(maxAge);
            log.debug("나이 범위 파싱: {}~{}세", minAge, maxAge);
            return;
        }
        
        // 2. 단일 나이 파싱 (예: "5세", "7세")
        Pattern singleAgePattern = Pattern.compile("(\\d+)\\s*세");
        Matcher singleAgeMatcher = singleAgePattern.matcher(target);
        if (singleAgeMatcher.find()) {
            int age = Integer.parseInt(singleAgeMatcher.group(1));
            program.setTargetAgeMin(age);
            program.setTargetAgeMax(age);
            log.debug("단일 나이 파싱: {}세", age);
            return;
        }
        
        // 3. 학년 범위 파싱 (예: "초등 3~4학년", "초등 1,2")
        Pattern gradeRangePattern = Pattern.compile("초등\\s*([1-6])\\s*[~,]\\s*([1-6])\\s*학년");
        Matcher gradeRangeMatcher = gradeRangePattern.matcher(target);
        if (gradeRangeMatcher.find()) {
            int startGrade = Integer.parseInt(gradeRangeMatcher.group(1));
            int endGrade = Integer.parseInt(gradeRangeMatcher.group(2));
            program.setTargetAgeMin(6 + startGrade); // 초등 1학년 = 7세
            program.setTargetAgeMax(6 + endGrade);
            log.debug("학년 범위 파싱: 초등 {}~{}학년 ({}~{}세)", startGrade, endGrade, 6+startGrade, 6+endGrade);
            return;
        }
        
        // 4. 단일 학년 파싱 (예: "초등 3학년", "초등3-4학년")
        Pattern singleGradePattern = Pattern.compile("초등\\s*([1-6])\\s*[-~]?\\s*([1-6])?\\s*학년");
        Matcher singleGradeMatcher = singleGradePattern.matcher(target);
        if (singleGradeMatcher.find()) {
            int startGrade = Integer.parseInt(singleGradeMatcher.group(1));
            String endGradeStr = singleGradeMatcher.group(2);
            
            if (endGradeStr != null && !endGradeStr.isEmpty()) {
                // 범위 학년 (예: "초등3-4학년")
                int endGrade = Integer.parseInt(endGradeStr);
                program.setTargetAgeMin(6 + startGrade);
                program.setTargetAgeMax(6 + endGrade);
                log.debug("학년 범위 파싱: 초등 {}~{}학년 ({}~{}세)", startGrade, endGrade, 6+startGrade, 6+endGrade);
            } else {
                // 단일 학년
                program.setTargetAgeMin(6 + startGrade);
                program.setTargetAgeMax(6 + startGrade);
                log.debug("단일 학년 파싱: 초등 {}학년 ({}세)", startGrade, 6+startGrade);
            }
            return;
        }
        
        // 5. 초등 전체 (예: "초등학교", "초등")
        if (target.contains("초등") && !target.contains("학년")) {
            program.setTargetAgeMin(7);
            program.setTargetAgeMax(13);
            log.debug("초등 전체: 7~13세");
            return;
        }
        
        // 6. 고학년 파싱 (예: "초등 고학년(4~6학년)")
        if (target.contains("고학년")) {
            program.setTargetAgeMin(10); // 초등 4학년
            program.setTargetAgeMax(12); // 초등 6학년
            log.debug("초등 고학년: 10~12세");
            return;
        }
        
        // 7. 저학년 파싱 (예: "초등 저학년")
        if (target.contains("저학년")) {
            program.setTargetAgeMin(7);  // 초등 1학년
            program.setTargetAgeMax(9);  // 초등 3학년
            log.debug("초등 저학년: 7~9세");
            return;
        }
        
        // 8. 유아/영유아 파싱
        if (target.contains("유아") || target.contains("영유아")) {
            program.setTargetAgeMin(4);
            program.setTargetAgeMax(7);
            log.debug("유아/영유아: 4~7세");
            return;
        }
        
        // 9. 어린이 파싱
        if (target.contains("어린이") || target.contains("아동")) {
            program.setTargetAgeMin(5);
            program.setTargetAgeMax(13);
            log.debug("어린이/아동: 5~13세");
            return;
        }
        
        // 10. 청소년 파싱
        if (target.contains("청소년") || target.contains("중학") || target.contains("고등")) {
            program.setTargetAgeMin(13);
            program.setTargetAgeMax(18);
            log.debug("청소년: 13~18세");
            return;
        }
        
        // 11. 성인 파싱
        if (target.contains("성인") || target.contains("어른")) {
            program.setTargetAgeMin(19);
            program.setTargetAgeMax(null); // 상한 없음
            log.debug("성인: 19세 이상");
            return;
        }
        
        // 12. 가족/부모 프로그램 (전 연령)
        if (target.contains("가족") || target.contains("부모") || target.contains("보호자")) {
            program.setTargetAgeMin(null); // 전 연령
            program.setTargetAgeMax(null);
            log.debug("가족/부모 프로그램: 전 연령");
            return;
        }
        
        // 13. 기본값 (파싱 실패 시)
        log.warn("연령 정보 파싱 실패, 전 연령으로 설정: {}", target);
        program.setTargetAgeMin(null);
        program.setTargetAgeMax(null);
    }

    private String generateEmotionKeywords(String courseName, String target) {
        List<String> keywords = new ArrayList<>();
        
        // 프로그램 이름에서 키워드 추출 (실제 CSV 데이터 기반)
        
        // 독서/책 관련
        if (courseName.contains("독서") || courseName.contains("책") || courseName.contains("그림책") || courseName.contains("읽기")) {
            keywords.add("독서하기");
            keywords.add("교육");
            keywords.add("차분함");
        }
        
        // 멘토링/교육 관련
        if (courseName.contains("멘토") || courseName.contains("기적") || courseName.contains("교실")) {
            keywords.add("학습");
            keywords.add("성장");
            keywords.add("뿌듯함");
        }
        
        // 캠프/여름 프로그램
        if (courseName.contains("캠프") || courseName.contains("여름")) {
            keywords.add("즐거움");
            keywords.add("활기참");
            keywords.add("설렘");
        }
        
        // 작가와의 만남/문학
        if (courseName.contains("작가") || courseName.contains("만남") || courseName.contains("문학")) {
            keywords.add("만남");
            keywords.add("문화");
            keywords.add("호기심");
        }
        
        // 취미/나눔 활동
        if (courseName.contains("취미") || courseName.contains("나눔") || courseName.contains("뜨개")) {
            keywords.add("취미");
            keywords.add("여유로움");
            keywords.add("행복");
        }
        
        // 가족/부모 프로그램
        if (courseName.contains("가족") || courseName.contains("부모") || courseName.contains("엄마") || courseName.contains("아빠")) {
            keywords.add("가족");
            keywords.add("소통");
            keywords.add("행복");
        }
        
        // 탐정/모험 활동
        if (courseName.contains("탐정") || courseName.contains("궁궐") || courseName.contains("비밀")) {
            keywords.add("모험");
            keywords.add("호기심");
            keywords.add("즐거움");
        }
        
        // 놀이/게임 활동
        if (courseName.contains("놀이") || courseName.contains("게임") || courseName.contains("꼼지락")) {
            keywords.add("놀이");
            keywords.add("즐거움");
            keywords.add("활기참");
        }
        
        // 예술/창작 활동
        if (courseName.contains("예술") || courseName.contains("오아시스") || courseName.contains("창작")) {
            keywords.add("예술");
            keywords.add("창작");
            keywords.add("뿌듯함");
        }
        
        // 요리/레시피
        if (courseName.contains("요리") || courseName.contains("레시피") || courseName.contains("달콤")) {
            keywords.add("요리하기");
            keywords.add("창작");
            keywords.add("행복");
        }
        
        // 과학/환경
        if (courseName.contains("과학") || courseName.contains("환경") || courseName.contains("플라스틱")) {
            keywords.add("과학");
            keywords.add("학습");
            keywords.add("호기심");
        }
        
        // 역사/문화
        if (courseName.contains("역사") || courseName.contains("문화") || courseName.contains("현대사") || courseName.contains("OTT")) {
            keywords.add("역사");
            keywords.add("문화");
            keywords.add("학습");
        }
        
        // 다문화/국제
        if (courseName.contains("다문화") || courseName.contains("살롱") || courseName.contains("영어")) {
            keywords.add("소통");
            keywords.add("문화");
            keywords.add("학습");
        }
        
        // 인문학/철학
        if (courseName.contains("인문학") || courseName.contains("생각") || courseName.contains("탐험")) {
            keywords.add("사색");
            keywords.add("학습");
            keywords.add("차분함");
        }
        
        // 대상별 기본 키워드
        if (target.contains("청소년")) {
            keywords.add("성장");
            keywords.add("진로");
        } else if (target.contains("성인")) {
            keywords.add("취미");
            keywords.add("평생학습");
        } else if (target.contains("아동") || target.contains("어린이")) {
            keywords.add("교육");
            keywords.add("성장");
        }
        
        // 기본 키워드 추가
        keywords.add("문화");
        keywords.add("학습");
        
        return String.join(",", keywords);
    }

    private String generateDescription(String target, String location, String schedule) {
        StringBuilder desc = new StringBuilder();
        desc.append("동작구립도서관에서 진행하는 문화 프로그램입니다.\n\n");
        desc.append("📍 대상: ").append(target).append("\n");
        desc.append("📍 장소: ").append(location).append("\n");
        desc.append("📍 일정: ").append(schedule).append("\n\n");
        desc.append("다양한 연령대가 함께 참여할 수 있는 의미있는 문화 활동으로, ");
        desc.append("학습과 즐거움을 동시에 얻을 수 있는 프로그램입니다.");
        
        return desc.toString();
    }
}