package com.tcrs.tcrs_backend.dto.credit;

import jakarta.validation.constraints.NotNull;

public class CreditReportRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    private String purpose;     // Purpose of credit check
    private String comments;    // Additional comments

    // Constructors
    public CreditReportRequest() {}

    public CreditReportRequest(Long businessId, String purpose, String comments) {
        this.businessId = businessId;
        this.purpose = purpose;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}