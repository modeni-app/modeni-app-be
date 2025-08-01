package com.steam.modeni.service;

import com.steam.modeni.domain.entity.WelfareProgram;
import com.steam.modeni.domain.enums.City;
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
            ClassPathResource resource = new ClassPathResource("dongjak_library_courses.csv");
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
                return null;
            }

            String courseName = fields.get(0);
            String actualLink = fields.get(2);
            String target = fields.get(6);
            String location = fields.get(7);
            String schedule = fields.get(8);

            WelfareProgram program = new WelfareProgram();
            program.setTitle(courseName);
            program.setDescription(generateDescription(target, location, schedule));
            program.setOrganization("동작구립도서관");
            program.setCategory("문화");
            program.setTargetCity(City.SEOUL);
            
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
        // 연령 정보 추출
        if (target.contains("유아") || target.contains("영유아")) {
            program.setTargetAgeMin(3);
            program.setTargetAgeMax(7);
        } else if (target.contains("초등")) {
            program.setTargetAgeMin(7);
            program.setTargetAgeMax(13);
            
            // 더 세부적인 학년 정보 파싱
            Pattern gradePattern = Pattern.compile("([1-6])\\s*~\\s*([1-6])\\s*학년");
            Matcher matcher = gradePattern.matcher(target);
            if (matcher.find()) {
                int startGrade = Integer.parseInt(matcher.group(1));
                int endGrade = Integer.parseInt(matcher.group(2));
                program.setTargetAgeMin(6 + startGrade);
                program.setTargetAgeMax(6 + endGrade);
            } else {
                // 단일 학년 파싱
                Pattern singleGradePattern = Pattern.compile("([1-6])\\s*학년");
                Matcher singleMatcher = singleGradePattern.matcher(target);
                if (singleMatcher.find()) {
                    int grade = Integer.parseInt(singleMatcher.group(1));
                    program.setTargetAgeMin(6 + grade);
                    program.setTargetAgeMax(6 + grade);
                }
            }
        } else if (target.contains("청소년") || target.contains("중")) {
            program.setTargetAgeMin(13);
            program.setTargetAgeMax(18);
        } else if (target.contains("성인") || target.contains("어른")) {
            program.setTargetAgeMin(19);
            program.setTargetAgeMax(null); // 상한 없음
        } else if (target.contains("가족") || target.contains("부모")) {
            program.setTargetAgeMin(null); // 전 연령
            program.setTargetAgeMax(null);
        }
    }

    private String generateEmotionKeywords(String courseName, String target) {
        List<String> keywords = new ArrayList<>();
        
        // 프로그램 이름에서 키워드 추출
        if (courseName.contains("독서") || courseName.contains("책")) {
            keywords.add("독서");
            keywords.add("교육");
        }
        if (courseName.contains("영어")) {
            keywords.add("영어");
            keywords.add("학습");
        }
        if (courseName.contains("과학")) {
            keywords.add("과학");
            keywords.add("호기심");
        }
        if (courseName.contains("요리")) {
            keywords.add("요리");
            keywords.add("창작");
        }
        if (courseName.contains("놀이") || courseName.contains("게임")) {
            keywords.add("즐거움");
            keywords.add("놀이");
        }
        if (courseName.contains("가족") || courseName.contains("부모")) {
            keywords.add("가족");
            keywords.add("소통");
        }
        if (courseName.contains("작가") || courseName.contains("만남")) {
            keywords.add("만남");
            keywords.add("문화");
        }
        if (courseName.contains("예술") || courseName.contains("문화")) {
            keywords.add("예술");
            keywords.add("문화");
        }
        if (courseName.contains("역사")) {
            keywords.add("역사");
            keywords.add("학습");
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