package com.steam.modeni.controller;

import com.steam.modeni.domain.entity.Mission;
import com.steam.modeni.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {
    
    private final MissionService missionService;
    
    /**
     * 가족에게 주간 미션 지급
     */
    @PostMapping("/assign")
    public ResponseEntity<Object> assignWeeklyMission(@RequestBody Map<String, Object> request) {
        try {
            Long familyCode = Long.valueOf(request.get("familyCode").toString());
            
            Mission mission = missionService.assignWeeklyMission(familyCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "새로운 미션이 지급되었습니다.");
            response.put("mission", mission);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 가족의 현재 진행 중인 미션들 조회
     */
    @GetMapping("/current/{familyCode}")
    public ResponseEntity<Object> getCurrentMissions(@PathVariable Long familyCode) {
        try {
            List<Mission> missions = missionService.getCurrentMissions(familyCode);
            return ResponseEntity.ok(missions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 가족의 완료된 미션들 조회
     */
    @GetMapping("/completed/{familyCode}")
    public ResponseEntity<Object> getCompletedMissions(@PathVariable Long familyCode) {
        try {
            List<Mission> missions = missionService.getCompletedMissions(familyCode);
            return ResponseEntity.ok(missions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 가족의 모든 미션 조회
     */
    @GetMapping("/all/{familyCode}")
    public ResponseEntity<Object> getAllMissions(@PathVariable Long familyCode) {
        try {
            List<Mission> missions = missionService.getAllMissions(familyCode);
            return ResponseEntity.ok(missions);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 미션 상세 조회
     */
    @GetMapping("/{missionId}")
    public ResponseEntity<Object> getMissionById(@PathVariable Long missionId) {
        try {
            Mission mission = missionService.getMissionById(missionId);
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
