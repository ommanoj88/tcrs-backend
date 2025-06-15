package com.tcrs.tcrs_backend.entity;

public enum PaymentStatus {
    PENDING,        // Payment not yet due
    OVERDUE,        // Payment past due date
    PARTIAL,        // Partially paid
    PAID,           // Fully paid
    DEFAULTED,      // Failed to pay after significant delay
    DISPUTED,       // Payment under dispute
    WRITTEN_OFF     // Debt written off
}