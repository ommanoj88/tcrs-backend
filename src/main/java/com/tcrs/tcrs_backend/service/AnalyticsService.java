package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.analytics.DashboardAnalyticsDTO;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CreditReportRepository creditReportRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private TradeReferenceRepository tradeReferenceRepository;

    @Autowired
    private UserRepository userRepository;

    // Optional - will be created when Alert system is implemented
    // @Autowired(required = false)
    // private AlertRepository alertRepository;

    // Optional - for future use
    // @Autowired(required = false)
    // private AnalyticsRepository analyticsRepository;

    public DashboardAnalyticsDTO getDashboardAnalytics() {
        logger.info("Generating dashboard analytics");

        try {
            DashboardAnalyticsDTO analytics = new DashboardAnalyticsDTO();

            // Generate all analytics sections
            analytics.setOverview(generateOverviewMetrics());
            analytics.setBusinessAnalytics(generateBusinessAnalytics());
            analytics.setCreditAnalytics(generateCreditAnalytics());
            analytics.setPaymentAnalytics(generatePaymentAnalytics());
            analytics.setAlertAnalytics(generateAlertAnalytics());
            analytics.setTrends(generateTrendData());
            analytics.setGeographicDistribution(generateGeographicDistribution());
            analytics.setIndustryDistribution(generateIndustryDistribution());

            logger.info("Dashboard analytics generated successfully");
            return analytics;

        } catch (Exception e) {
            logger.error("Error generating dashboard analytics", e);
            throw new RuntimeException("Failed to generate analytics", e);
        }
    }

    private DashboardAnalyticsDTO.OverviewMetrics generateOverviewMetrics() {
        try {
            Long totalBusinesses = businessRepository.count();
            Long totalCreditReports = creditReportRepository.count();
            Long totalPaymentRecords = paymentHistoryRepository.count();
            Long totalTradeReferences = tradeReferenceRepository.count();
            Long totalAlerts = 0L; // Default when no alert system yet
            Long activeUsers = userRepository.count();

            // Calculate system health based on various factors
            BigDecimal systemHealth = calculateSystemHealth();

            return new DashboardAnalyticsDTO.OverviewMetrics(
                    totalBusinesses,
                    totalCreditReports,
                    totalPaymentRecords,
                    totalTradeReferences,
                    totalAlerts,
                    activeUsers,
                    systemHealth,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            logger.error("Error generating overview metrics", e);
            return new DashboardAnalyticsDTO.OverviewMetrics();
        }
    }

    private DashboardAnalyticsDTO.BusinessAnalytics generateBusinessAnalytics() {
        try {
            Long totalBusinesses = businessRepository.count();

            // New businesses this month
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            Long newBusinessesThisMonth = businessRepository.countByCreatedAtAfter(startOfMonth);

            // Verified businesses - use a simple count for now
            Long verifiedBusinesses = businessRepository.countByGstinVerifiedTrue();
            BigDecimal verificationRate = totalBusinesses > 0 ?
                    new BigDecimal(verifiedBusinesses).divide(new BigDecimal(totalBusinesses), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)) :
                    BigDecimal.ZERO;

            // Business type distribution
            Map<String, Long> businessTypeDistribution = businessRepository.findAll()
                    .stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getBusinessType() != null ? b.getBusinessType().toString() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Industry distribution
            Map<String, Long> industryDistribution = businessRepository.findAll()
                    .stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getIndustryCategory() != null ? b.getIndustryCategory().toString() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Business growth trend (last 12 months)
            List<DashboardAnalyticsDTO.TrendDataPoint> businessGrowthTrend = generateBusinessGrowthTrend();

            return new DashboardAnalyticsDTO.BusinessAnalytics(
                    totalBusinesses,
                    newBusinessesThisMonth,
                    verifiedBusinesses,
                    verificationRate,
                    businessTypeDistribution,
                    industryDistribution,
                    businessGrowthTrend
            );
        } catch (Exception e) {
            logger.error("Error generating business analytics", e);
            return new DashboardAnalyticsDTO.BusinessAnalytics();
        }
    }

    private DashboardAnalyticsDTO.CreditAnalytics generateCreditAnalytics() {
        try {
            List<Business> businesses = businessRepository.findAll();
            List<BigDecimal> creditScores = new ArrayList<>();

            // Generate sample credit scores for demonstration
            for (Business business : businesses) {
                BigDecimal score = BigDecimal.valueOf(300 + Math.random() * 550);
                creditScores.add(score);
            }

            if (creditScores.isEmpty()) {
                return new DashboardAnalyticsDTO.CreditAnalytics();
            }

            // Calculate statistics
            BigDecimal averageCreditScore = creditScores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(creditScores.size()), 2, RoundingMode.HALF_UP);

            // Calculate median
            List<BigDecimal> sortedScores = creditScores.stream().sorted().collect(Collectors.toList());
            BigDecimal medianCreditScore = sortedScores.size() % 2 == 0 ?
                    sortedScores.get(sortedScores.size() / 2 - 1).add(sortedScores.get(sortedScores.size() / 2))
                            .divide(new BigDecimal(2), 2, RoundingMode.HALF_UP) :
                    sortedScores.get(sortedScores.size() / 2);

            // Risk categorization
            Long highRiskBusinesses = creditScores.stream()
                    .mapToLong(score -> score.compareTo(new BigDecimal(500)) < 0 ? 1 : 0)
                    .sum();

            Long lowRiskBusinesses = creditScores.stream()
                    .mapToLong(score -> score.compareTo(new BigDecimal(700)) >= 0 ? 1 : 0)
                    .sum();

            // Credit score distribution
            Map<String, Long> creditScoreDistribution = new HashMap<>();
            creditScoreDistribution.put("300-400", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(300)) >= 0 && s.compareTo(new BigDecimal(400)) < 0 ? 1 : 0).sum());
            creditScoreDistribution.put("400-500", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(400)) >= 0 && s.compareTo(new BigDecimal(500)) < 0 ? 1 : 0).sum());
            creditScoreDistribution.put("500-600", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(500)) >= 0 && s.compareTo(new BigDecimal(600)) < 0 ? 1 : 0).sum());
            creditScoreDistribution.put("600-700", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(600)) >= 0 && s.compareTo(new BigDecimal(700)) < 0 ? 1 : 0).sum());
            creditScoreDistribution.put("700-800", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(700)) >= 0 && s.compareTo(new BigDecimal(800)) < 0 ? 1 : 0).sum());
            creditScoreDistribution.put("800+", creditScores.stream().mapToLong(s -> s.compareTo(new BigDecimal(800)) >= 0 ? 1 : 0).sum());

            // Risk category distribution
            Map<String, Long> riskCategoryDistribution = new HashMap<>();
            for (BigDecimal score : creditScores) {
                String risk = getRiskCategory(score);
                riskCategoryDistribution.merge(risk, 1L, Long::sum);
            }

            // Credit score trend - empty for now
            List<DashboardAnalyticsDTO.TrendDataPoint> creditScoreTrend = new ArrayList<>();

            Long creditReportsGenerated = creditReportRepository.count();

            return new DashboardAnalyticsDTO.CreditAnalytics(
                    averageCreditScore,
                    medianCreditScore,
                    highRiskBusinesses,
                    lowRiskBusinesses,
                    creditScoreDistribution,
                    riskCategoryDistribution,
                    creditScoreTrend,
                    creditReportsGenerated
            );
        } catch (Exception e) {
            logger.error("Error generating credit analytics", e);
            return new DashboardAnalyticsDTO.CreditAnalytics();
        }
    }

    private DashboardAnalyticsDTO.PaymentAnalytics generatePaymentAnalytics() {
        try {
            List<PaymentHistory> payments = paymentHistoryRepository.findAll();

            if (payments.isEmpty()) {
                return new DashboardAnalyticsDTO.PaymentAnalytics();
            }

            // On-time payment rate
            long onTimePayments = payments.stream()
                    .mapToLong(p -> {
                        if (p.getPaymentStatus() == PaymentStatus.PAID) {
                            Integer daysDelayed = p.getDaysDelayed();
                            return (daysDelayed == null || daysDelayed <= 0) ? 1 : 0;
                        }
                        return 0;
                    }).sum();

            BigDecimal onTimePaymentRate = new BigDecimal(onTimePayments)
                    .divide(new BigDecimal(payments.size()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));

            // Average payment delay
            BigDecimal averagePaymentDelay = payments.stream()
                    .filter(p -> p.getDaysDelayed() != null && p.getDaysDelayed() > 0)
                    .map(p -> new BigDecimal(p.getDaysDelayed()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(Math.max(1, payments.stream().mapToInt(p -> p.getDaysDelayed() != null && p.getDaysDelayed() > 0 ? 1 : 0).sum())), 2, RoundingMode.HALF_UP);

            // Total overdue amount
            BigDecimal totalOverdueAmount = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.OVERDUE || p.getPaymentStatus() == PaymentStatus.DEFAULTED)
                    .map(PaymentHistory::getTransactionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Overdue payments count
            Long overduePayments = payments.stream()
                    .mapToLong(p -> (p.getPaymentStatus() == PaymentStatus.OVERDUE || p.getPaymentStatus() == PaymentStatus.DEFAULTED) ? 1 : 0)
                    .sum();

            // Payment status distribution
            Map<String, Long> paymentStatusDistribution = payments.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getPaymentStatus() != null ? p.getPaymentStatus().toString() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Payment performance trend - empty for now
            List<DashboardAnalyticsDTO.TrendDataPoint> paymentPerformanceTrend = new ArrayList<>();

            // Top defaulters
            List<DashboardAnalyticsDTO.TopDefaulter> topDefaulters = generateTopDefaulters();

            return new DashboardAnalyticsDTO.PaymentAnalytics(
                    onTimePaymentRate,
                    averagePaymentDelay,
                    totalOverdueAmount,
                    overduePayments,
                    paymentStatusDistribution,
                    paymentPerformanceTrend,
                    topDefaulters
            );
        } catch (Exception e) {
            logger.error("Error generating payment analytics", e);
            return new DashboardAnalyticsDTO.PaymentAnalytics();
        }
    }

    private DashboardAnalyticsDTO.AlertAnalytics generateAlertAnalytics() {
        // Return empty alert analytics for now
        return new DashboardAnalyticsDTO.AlertAnalytics(
                0L, // totalAlertsGenerated
                0L, // unreadAlerts
                0L, // criticalAlerts
                BigDecimal.ZERO, // alertAcknowledgmentRate
                new HashMap<>(), // alertTypeDistribution
                new HashMap<>(), // alertSeverityDistribution
                new ArrayList<>() // alertTrend
        );
    }

    private DashboardAnalyticsDTO.TrendData generateTrendData() {
        return new DashboardAnalyticsDTO.TrendData(
                generateBusinessGrowthTrend(),
                new ArrayList<>(), // creditReportGeneration
                new ArrayList<>(), // paymentActivity
                new ArrayList<>(), // userActivity
                new ArrayList<>()  // alertActivity
        );
    }

    private List<DashboardAnalyticsDTO.GeographicData> generateGeographicDistribution() {
        try {
            Map<String, List<Business>> businessesByState = businessRepository.findAll()
                    .stream()
                    .filter(b -> b.getState() != null)
                    .collect(Collectors.groupingBy(Business::getState));

            List<DashboardAnalyticsDTO.GeographicData> geoData = new ArrayList<>();

            for (Map.Entry<String, List<Business>> entry : businessesByState.entrySet()) {
                String state = entry.getKey();
                List<Business> businesses = entry.getValue();

                // Generate sample average credit score
                BigDecimal avgCreditScore = BigDecimal.valueOf(300 + Math.random() * 550);

                geoData.add(new DashboardAnalyticsDTO.GeographicData(
                        state,
                        null,
                        (long) businesses.size(),
                        avgCreditScore,
                        null,
                        null
                ));
            }

            return geoData;
        } catch (Exception e) {
            logger.error("Error generating geographic distribution", e);
            return new ArrayList<>();
        }
    }

    private List<DashboardAnalyticsDTO.IndustryData> generateIndustryDistribution() {
        try {
            Map<String, List<Business>> businessesByIndustry = businessRepository.findAll()
                    .stream()
                    .filter(b -> b.getIndustryCategory() != null)
                    .collect(Collectors.groupingBy(b -> b.getIndustryCategory().toString()));

            List<DashboardAnalyticsDTO.IndustryData> industryData = new ArrayList<>();

            for (Map.Entry<String, List<Business>> entry : businessesByIndustry.entrySet()) {
                String industry = entry.getKey();
                List<Business> businesses = entry.getValue();

                // Generate sample metrics
                BigDecimal avgCreditScore = BigDecimal.valueOf(300 + Math.random() * 550);
                BigDecimal avgPaymentDelay = BigDecimal.valueOf(Math.random() * 30);

                // Calculate risk level based on credit score
                Long riskLevel = 3L; // Default medium risk
                if (avgCreditScore.compareTo(new BigDecimal(700)) >= 0) {
                    riskLevel = 1L;
                } else if (avgCreditScore.compareTo(new BigDecimal(600)) >= 0) {
                    riskLevel = 2L;
                } else if (avgCreditScore.compareTo(new BigDecimal(500)) >= 0) {
                    riskLevel = 3L;
                } else if (avgCreditScore.compareTo(new BigDecimal(400)) >= 0) {
                    riskLevel = 4L;
                } else {
                    riskLevel = 5L;
                }

                industryData.add(new DashboardAnalyticsDTO.IndustryData(
                        industry,
                        (long) businesses.size(),
                        avgCreditScore,
                        avgPaymentDelay,
                        riskLevel
                ));
            }

            return industryData;
        } catch (Exception e) {
            logger.error("Error generating industry distribution", e);
            return new ArrayList<>();
        }
    }

    // Helper methods
    private List<DashboardAnalyticsDTO.TrendDataPoint> generateBusinessGrowthTrend() {
        List<DashboardAnalyticsDTO.TrendDataPoint> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            Long count = businessRepository.countByCreatedAtBetween(monthStart, monthEnd);
            String period = monthStart.format(formatter);

            trend.add(new DashboardAnalyticsDTO.TrendDataPoint(
                    period,
                    new BigDecimal(count),
                    count,
                    monthStart.getMonth().toString()
            ));
        }

        return trend;
    }

    private List<DashboardAnalyticsDTO.TopDefaulter> generateTopDefaulters() {
        List<DashboardAnalyticsDTO.TopDefaulter> topDefaulters = new ArrayList<>();

        try {
            List<PaymentHistory> overduePayments = paymentHistoryRepository.findByPaymentStatusIn(
                    Arrays.asList(PaymentStatus.OVERDUE, PaymentStatus.DEFAULTED)
            );

            Map<Long, BigDecimal> businessOverdueMap = overduePayments.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getBusiness().getId(),
                            Collectors.mapping(
                                    PaymentHistory::getTransactionAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                            )
                    ));

            businessOverdueMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        try {
                            Business business = businessRepository.findById(entry.getKey()).orElse(null);
                            if (business != null) {
                                BigDecimal creditScore = BigDecimal.valueOf(300 + Math.random() * 550);
                                int avgDaysPastDue = (int) overduePayments.stream()
                                        .filter(p -> p.getBusiness().getId().equals(business.getId()))
                                        .mapToInt(p -> p.getDaysDelayed() != null ? p.getDaysDelayed() : 0)
                                        .average()
                                        .orElse(0);

                                topDefaulters.add(new DashboardAnalyticsDTO.TopDefaulter(
                                        business.getId(),
                                        business.getBusinessName(),
                                        entry.getValue(),
                                        avgDaysPastDue,
                                        creditScore
                                ));
                            }
                        } catch (Exception e) {
                            logger.warn("Error processing top defaulter for business {}", entry.getKey());
                        }
                    });
        } catch (Exception e) {
            logger.error("Error generating top defaulters", e);
        }

        return topDefaulters;
    }

    private BigDecimal calculateSystemHealth() {
        try {
            // Simplified system health calculation
            Long totalBusinesses = businessRepository.count();
            Long verifiedBusinesses = businessRepository.countByGstinVerifiedTrue();

            BigDecimal verificationRate = totalBusinesses > 0 ?
                    new BigDecimal(verifiedBusinesses).divide(new BigDecimal(totalBusinesses), 4, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // Simple health score based on verification rate
            BigDecimal healthScore = verificationRate.multiply(new BigDecimal(100));

            return healthScore.setScale(1, RoundingMode.HALF_UP);
        } catch (Exception e) {
            logger.error("Error calculating system health", e);
            return new BigDecimal(75);
        }
    }

    private String getRiskCategory(BigDecimal score) {
        if (score.compareTo(new BigDecimal(700)) >= 0) {
            return "LOW";
        } else if (score.compareTo(new BigDecimal(600)) >= 0) {
            return "MODERATE";
        } else if (score.compareTo(new BigDecimal(500)) >= 0) {
            return "MEDIUM";
        } else if (score.compareTo(new BigDecimal(400)) >= 0) {
            return "HIGH";
        } else {
            return "VERY_HIGH";
        }
    }
}