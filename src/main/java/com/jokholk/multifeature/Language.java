package com.jokholk.multifeature;

public enum Language {
    ENGLISH, VIETNAMESE;

    public static Language fromString(String s) {
        if (s == null) return ENGLISH;
        return switch (s.toUpperCase()) {
            case "VIETNAMESE", "VI", "VN" -> VIETNAMESE;
            default -> ENGLISH;
        };
    }
}
