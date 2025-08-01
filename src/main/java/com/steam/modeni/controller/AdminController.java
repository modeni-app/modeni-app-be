package com.steam.modeni.controller;

import com.steam.modeni.service.CsvDataLoaderService;
import com.steam.modeni.repository.WelfareProgramRepository;
import com.steam.modeni.repository.WelfareRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CsvDataLoaderService csvDataLoaderService;
    private final WelfareProgramRepository welfareProgramRepository;
    private final WelfareRecommendationRepository welfareRecommendationRepository;

    /**
     * 복지 프로그램 데이터 현황 조회
     */
    @GetMapping("/welfare/status")
    public ResponseEntity<?> getWelfareDataStatus() {
        try {
            long programCount = welfareProgramRepository.count();
            long recommendationCount = welfareRecommendationRepository.count();
            
            return ResponseEntity.ok(Map.of(
                "welfareProgramCount", programCount,
                "welfareRecommendationCount", recommendationCount,
                "message", "복지 데이터 현황 조회 완료"
            ));
        } catch (Exception e) {
            log.error("복지 데이터 현황 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("데이터 현황 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 복지 추천 데이터 초기화 (외래키 순서 고려)
     */
    @DeleteMapping("/welfare/reset")
    public ResponseEntity<?> resetWelfareData() {
        try {
            // 1. 추천 데이터 먼저 삭제 (외래키 제약)
            long deletedRecommendations = welfareRecommendationRepository.count();
            welfareRecommendationRepository.deleteAll();
            
            // 2. 프로그램 데이터 삭제
            long deletedPrograms = welfareProgramRepository.count();
            welfareProgramRepository.deleteAll();
            
            log.info("복지 데이터 초기화 완료: 추천 {}개, 프로그램 {}개 삭제", deletedRecommendations, deletedPrograms);
            
            return ResponseEntity.ok(Map.of(
                "deletedRecommendations", deletedRecommendations,
                "deletedPrograms", deletedPrograms,
                "message", "복지 데이터 초기화 완료"
            ));
        } catch (Exception e) {
            log.error("복지 데이터 초기화 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("데이터 초기화에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * CSV 데이터 강제 로딩
     */
    @PostMapping("/welfare/load-csv")
    public ResponseEntity<?> loadCsvData() {
        try {
            log.info("CSV 데이터 강제 로딩 시작");
            
            // CSV 데이터 로딩 실행
            csvDataLoaderService.loadDongjakLibraryCourses();
            
            // 로딩 후 데이터 개수 확인
            long programCount = welfareProgramRepository.count();
            
            return ResponseEntity.ok(Map.of(
                "loadedProgramCount", programCount,
                "message", "CSV 데이터 로딩 완료"
            ));
        } catch (Exception e) {
            log.error("CSV 데이터 로딩 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("CSV 데이터 로딩에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 복지 데이터 전체 재설정 (초기화 + CSV 로딩)
     */
    @PostMapping("/welfare/reset-and-reload")
    public ResponseEntity<?> resetAndReloadWelfareData() {
        try {
            log.info("복지 데이터 전체 재설정 시작");
            
            // 1. 기존 데이터 초기화
            long deletedRecommendations = welfareRecommendationRepository.count();
            long deletedPrograms = welfareProgramRepository.count();
            
            welfareRecommendationRepository.deleteAll();
            welfareProgramRepository.deleteAll();
            
            // 2. CSV 데이터 로딩
            csvDataLoaderService.loadDongjakLibraryCourses();
            
            // 3. 결과 확인
            long newProgramCount = welfareProgramRepository.count();
            
            log.info("복지 데이터 전체 재설정 완료: 삭제 {}개 → 로딩 {}개", deletedPrograms, newProgramCount);
            
            return ResponseEntity.ok(Map.of(
                "deletedRecommendations", deletedRecommendations,
                "deletedPrograms", deletedPrograms,
                "loadedPrograms", newProgramCount,
                "message", "복지 데이터 전체 재설정 완료"
            ));
        } catch (Exception e) {
            log.error("복지 데이터 전체 재설정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("데이터 재설정에 실패했습니다: " + e.getMessage());
        }
    }
}
