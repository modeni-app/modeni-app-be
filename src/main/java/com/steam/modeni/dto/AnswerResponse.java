package com.steam.modeni.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AnswerResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserInfo user;
    private Long questionId;
    
    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String username;
    }
} 