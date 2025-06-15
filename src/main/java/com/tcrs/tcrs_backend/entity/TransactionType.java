package com.tcrs.tcrs_backend.entity;


public enum TransactionType {
    INVOICE_PAYMENT,    // Regular invoice payment
    CREDIT_NOTE,        // Credit note adjustment
    ADVANCE_PAYMENT,    // Payment in advance
    REFUND,            // Refund transaction
    PENALTY,           // Penalty payment
    SETTLEMENT         // Debt settlement
}
