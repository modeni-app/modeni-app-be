package com.steam.modeni.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String prompt;
    private String model;
    private Double temperature;
    private Integer maxTokens;
} 