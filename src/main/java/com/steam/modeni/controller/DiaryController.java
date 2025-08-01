package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Diary;
import com.steam.modeni.domain.entity.User;
import com.steam.modeni.dto.DiaryRequest;
import com.steam.modeni.service.DiaryService;
import com.steam.modeni.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createDiary(@RequestBody DiaryRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            Diary diary = diaryService.createDiary(user, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "감정 일기가 성공적으로 작성되었습니다. 맞춤 복지 정보를 분석 중입니다.",
                "diary", diary,
                "diaryId", diary.getId()
            ));
        } catch (Exception e) {
            log.error("감정 일기 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("감정 일기 작성에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDiaries(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            List<Diary> diaries = diaryService.getUserDiaries(user);
            
            return ResponseEntity.ok(Map.of(
                "diaries", diaries,
                "totalCount", diaries.size()
            ));
        } catch (Exception e) {
            log.error("일기 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("일기 목록을 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayDiary(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            Optional<Diary> todayDiary = diaryService.getTodayDiary(user);
            
            return ResponseEntity.ok(Map.of(
                "hasToday", todayDiary.isPresent(),
                "diary", todayDiary.orElse(null)
            ));
        } catch (Exception e) {
            log.error("오늘 일기 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("오늘 일기를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/range")
    public ResponseEntity<?> getDiariesByDateRange(@RequestParam String startDate,
                                                  @RequestParam String endDate,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            
            List<Diary> diaries = diaryService.getDiariesByDateRange(user, start, end);
            
            return ResponseEntity.ok(Map.of(
                "diaries", diaries,
                "count", diaries.size(),
                "startDate", startDate,
                "endDate", endDate
            ));
        } catch (Exception e) {
            log.error("기간별 일기 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("기간별 일기를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiary(@PathVariable Long id,
                                        @RequestBody DiaryRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            Diary updatedDiary = diaryService.updateDiary(id, user, request);
            
            return ResponseEntity.ok(Map.of(
                "message", "감정 일기가 성공적으로 수정되었습니다.",
                "diary", updatedDiary
            ));
        } catch (Exception e) {
            log.error("일기 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("일기 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            diaryService.deleteDiary(id, user);
            
            return ResponseEntity.ok(Map.of(
                "message", "감정 일기가 성공적으로 삭제되었습니다.",
                "deletedDiaryId", id
            ));
        } catch (Exception e) {
            log.error("일기 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("일기 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDiariesByEmotion(@RequestParam String emotion,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            List<Diary> diaries = diaryService.searchDiariesByEmotion(user, emotion);
            
            return ResponseEntity.ok(Map.of(
                "diaries", diaries,
                "emotion", emotion,
                "count", diaries.size()
            ));
        } catch (Exception e) {
            log.error("감정별 일기 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("감정별 일기 검색에 실패했습니다: " + e.getMessage());
        }
    }
}