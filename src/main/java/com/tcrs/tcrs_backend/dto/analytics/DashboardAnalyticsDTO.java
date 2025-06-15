package com.tcrs.tcrs_backend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDTO {

    // Overview Metrics
    private OverviewMetrics overview;

    // Business Analytics
    private BusinessAnalytics businessAnalytics;

    // Credit Analytics
    private CreditAnalytics creditAnalytics;

    // Payment Analytics
    private PaymentAnalytics paymentAnalytics;

    // Alert Analytics
    private AlertAnalytics alertAnalytics;

    // Trend Data
    private TrendData trends;

    // Geographic Distribution
    private List<GeographicData> geographicDistribution;

    // Industry Distribution
    private List<IndustryData> industryDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewMetrics {
        private Long totalBusinesses;
        private Long totalCreditReports;
        private Long totalPaymentRecords;
        private Long totalTradeReferences;
        private Long totalAlerts;
        private Long activeUsers;
        private BigDecimal systemHealth; // 0-100 percentage
        private LocalDateTime lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAnalytics {
        private Long totalBusinesses;
        private Long newBusinessesThisMonth;
        private Long verifiedBusinesses;
        private BigDecimal verificationRate;
        private Map<String, Long> businessTypeDistribution;
        private Map<String, Long> industryDistribution;
        private List<TrendDataPoint> businessGrowthTrend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditAnalytics {
        private BigDecimal averageCreditScore;
        private BigDecimal medianCreditScore;
        private Long highRiskBusinesses;
        private Long lowRiskBusinesses;
        private Map<String, Long> creditScoreDistribution;
        private Map<String, Long> riskCategoryDistribution;
        private List<TrendDataPoint> creditScoreTrend;
        private Long creditReportsGenerated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentAnalytics {
        private BigDecimal onTimePaymentRate;
        private BigDecimal averagePaymentDelay;
        private BigDecimal totalOverdueAmount;
        private Long overduePayments;
        private Map<String, Long> paymentStatusDistribution;
        private List<TrendDataPoint> paymentPerformanceTrend;
        private List<TopDefaulter> topDefaulters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertAnalytics {
        private Long totalAlertsGenerated;
        private Long unreadAlerts;
        private Long criticalAlerts;
        private BigDecimal alertAcknowledgmentRate;
        private Map<String, Long> alertTypeDistribution;
        private Map<String, Long> alertSeverityDistribution;
        private List<TrendDataPoint> alertTrend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private List<TrendDataPoint> businessRegistrations;
        private List<TrendDataPoint> creditReportGeneration;
        private List<TrendDataPoint> paymentActivity;
        private List<TrendDataPoint> userActivity;
        private List<TrendDataPoint> alertActivity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        private String period; // "2024-01", "2024-W01", "2024-01-01"
        private BigDecimal value;
        private Long count;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicData {
        private String state;
        private String city;
        private Long businessCount;
        private BigDecimal averageCreditScore;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndustryData {
        private String industry;
        private Long businessCount;
        private BigDecimal averageCreditScore;
        private BigDecimal averagePaymentDelay;
        private Long riskLevel; // 1-5
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopDefaulter {
        private Long businessId;
        private String businessName;
        private BigDecimal overdueAmount;
        private Integer daysPastDue;
        private BigDecimal creditScore;
    }
}