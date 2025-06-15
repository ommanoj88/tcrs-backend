package com.tcrs.tcrs_backend.entity;

public enum AlertType {
    CREDIT_SCORE_CHANGE,        // Credit score increased/decreased
    CREDIT_SCORE_THRESHOLD,     // Credit score crossed threshold
    PAYMENT_DELAY,              // New payment delay detected
    OVERDUE_AMOUNT,             // Overdue amount exceeded threshold
    NEW_TRADE_REFERENCE,        // New trade reference added
    TRADE_REFERENCE_VERIFIED,   // Trade reference verified
    NEW_PAYMENT_HISTORY,        // New payment history added
    CREDIT_REPORT_GENERATED,    // New credit report generated
    BUSINESS_PROFILE_UPDATED,   // Business profile changed
    RISK_LEVEL_CHANGE,          // Risk category changed
    CREDIT_LIMIT_CHANGE,        // Recommended credit limit changed
    DISPUTE_REPORTED,           // New dispute reported
    SYSTEM_ALERT               // System-generated alert
}