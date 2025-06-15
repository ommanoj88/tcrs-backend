package com.tcrs.tcrs_backend.entity;

public enum MonitoringType {
    COMPREHENSIVE,      // Monitor all aspects
    CREDIT_SCORE_ONLY,  // Only credit score changes
    PAYMENT_BEHAVIOR,   // Payment-related alerts
    TRADE_REFERENCES,   // Trade reference changes
    BUSINESS_PROFILE,   // Business information changes
    CUSTOM             // Custom monitoring rules
}