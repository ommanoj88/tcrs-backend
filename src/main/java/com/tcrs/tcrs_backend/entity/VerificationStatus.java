package com.tcrs.tcrs_backend.entity;


public enum VerificationStatus {
    PENDING,           // Awaiting verification
    VERIFIED,          // Verified by system/admin
    DISPUTED,          // Under dispute
    REJECTED,          // Verification rejected
    AUTO_VERIFIED      // Automatically verified
}
