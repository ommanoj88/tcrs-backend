package com.tcrs.tcrs_backend.dto.business;

import java.util.List;

public class BusinessSearchResponse {

    private List<BusinessResponse> businesses;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    // Constructors
    public BusinessSearchResponse() {}

    public BusinessSearchResponse(List<BusinessResponse> businesses, int currentPage,
                                  int totalPages, long totalElements, boolean hasNext, boolean hasPrevious) {
        this.businesses = businesses;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters and Setters
    public List<BusinessResponse> getBusinesses() { return businesses; }
    public void setBusinesses(List<BusinessResponse> businesses) { this.businesses = businesses; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
}