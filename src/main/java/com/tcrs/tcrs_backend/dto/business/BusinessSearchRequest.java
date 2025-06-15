package com.tcrs.tcrs_backend.dto.business;

import com.tcrs.tcrs_backend.entity.BusinessType;
import com.tcrs.tcrs_backend.entity.IndustryCategory;

public class BusinessSearchRequest {

    private String query;           // General search term
    private String businessName;    // Specific business name
    private String gstin;          // GSTIN search
    private String pan;            // PAN search
    private String city;           // City filter
    private String state;          // State filter
    private BusinessType businessType;
    private IndustryCategory industryCategory;
    private int page = 0;
    private int size = 10;
    private String sortBy = "businessName";
    private String sortDirection = "asc";

    // Constructors
    public BusinessSearchRequest() {}

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }

    public IndustryCategory getIndustryCategory() { return industryCategory; }
    public void setIndustryCategory(IndustryCategory industryCategory) { this.industryCategory = industryCategory; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}