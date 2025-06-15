package com.tcrs.tcrs_backend.entity;

public enum PaymentBehavior {
    EXCELLENT,          // Always pays on time or early
    GOOD,               // Usually pays on time
    SATISFACTORY,       // Pays within acceptable delay
    POOR,               // Frequently delays payment
    VERY_POOR,          // Consistently late payments
    DEFAULTED           // Has defaulted on payments
}