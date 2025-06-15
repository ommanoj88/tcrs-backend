package com.tcrs.tcrs_backend.entity;

public enum VerificationMethod {
    PHONE_CALL,         // Verified via phone call
    EMAIL,              // Verified via email
    WRITTEN_RESPONSE,   // Written response received
    SITE_VISIT,         // Physical site verification
    DIGITAL_VERIFICATION, // Digital/online verification
    DOCUMENT_VERIFICATION, // Document-based verification
    THIRD_PARTY         // Third-party verification service
}