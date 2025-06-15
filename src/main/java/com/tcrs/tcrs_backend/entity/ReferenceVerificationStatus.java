package com.tcrs.tcrs_backend.entity;

public enum ReferenceVerificationStatus {
    PENDING,            // Awaiting verification
    VERIFIED,           // Successfully verified
    PARTIALLY_VERIFIED, // Some details verified
    UNVERIFIED,         // Could not verify
    DECLINED,           // Reference declined to respond
    INVALID,            // Invalid reference information
    EXPIRED             // Verification expired
}