package com.steam.modeni.domain.enums;

public enum FamilyRole {
    MOTHER("엄마"),
    FATHER("아빠"),
    DAUGHTER("딸"),
    SON("아들"),
    OTHER("기타");
    
    private final String displayName;
    
    FamilyRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 