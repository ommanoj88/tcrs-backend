package com.tcrs.tcrs_backend.entity;

public enum RecommendationLevel {
    HIGHLY_RECOMMENDED,     // Strongly recommend
    RECOMMENDED,            // Recommend with confidence
    CONDITIONALLY_RECOMMENDED, // Recommend with conditions
    NOT_RECOMMENDED,        // Do not recommend
    UNKNOWN                 // Insufficient information
}
