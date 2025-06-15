package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.credit.CreditReportRequest;
import com.tcrs.tcrs_backend.dto.credit.CreditReportResponse;
import com.tcrs.tcrs_backend.dto.payment.PaymentAnalyticsResponse;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
import com.tcrs.tcrs_backend.repository.CreditReportRepository;
import com.tcrs.tcrs_backend.repository.PaymentHistoryRepository;
import com.tcrs.tcrs_backend.repository.UserRepository;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnhancedCreditReportService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedCreditReportService.class);
    private static final int REPORT_VALIDITY_DAYS = 30;

    @Autowired
    private CreditReportRepository creditReportRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    public CreditReportResponse generateEnhancedCreditReport(CreditReportRequest request) {
        logger.info("Generating enhanced credit report for business ID: {}", request.getBusinessId());

        // Get current user
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User requestedBy = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Get business
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        if (!business.getIsActive()) {
            throw new BadRequestException("Cannot generate credit report for inactive business");
        }

        // Check if valid report already exists
        Optional<CreditReport> existingReport = creditReportRepository.findValidReportByBusiness(
                business, ReportStatus.GENERATED, LocalDateTime.now());

        if (existingReport.isPresent()) {
            logger.info("Valid report already exists for business: {}", business.getBusinessName());
            return convertToResponse(existingReport.get());
        }

        // Get payment analytics for enhanced scoring
        PaymentAnalyticsResponse paymentAnalytics = null;
        try {
            paymentAnalytics = paymentHistoryService.getPaymentAnalytics(business.getId());
        } catch (Exception e) {
            logger.warn("Could not retrieve payment analytics for business {}: {}", business.getId(), e.getMessage());
        }

        // Generate new enhanced credit report
        CreditReport creditReport = new CreditReport();
        creditReport.setBusiness(business);
        creditReport.setRequestedBy(requestedBy);
        creditReport.setReportNumber(generateReportNumber());
        creditReport.setReportStatus(ReportStatus.GENERATED);
        creditReport.setReportValidUntil(LocalDateTime.now().plusDays(REPORT_VALIDITY_DAYS));

        // Calculate enhanced credit scores with payment history
        calculateEnhancedCreditScores(creditReport, business, paymentAnalytics);

        // Generate enhanced report content
        generateEnhancedReportContent(creditReport, business, paymentAnalytics);

        // Save credit report
        CreditReport savedReport = creditReportRepository.save(creditReport);

        logger.info("Enhanced credit report generated successfully with number: {}", savedReport.getReportNumber());

        return convertToResponse(savedReport);
    }

    private void calculateEnhancedCreditScores(CreditReport creditReport, Business business,
                                               PaymentAnalyticsResponse paymentAnalytics) {
        logger.debug("Calculating enhanced credit scores for business: {}", business.getBusinessName());

        // Calculate component scores with payment history integration
        BigDecimal complianceScore = calculateEnhancedComplianceScore(business);
        BigDecimal businessStabilityScore = calculateEnhancedBusinessStabilityScore(business);
        BigDecimal financialStrengthScore = calculateEnhancedFinancialStrengthScore(business, paymentAnalytics);
        BigDecimal paymentBehaviorScore = calculateEnhancedPaymentBehaviorScore(business, paymentAnalytics);

        // Set component scores
        creditReport.setComplianceScore(complianceScore);
        creditReport.setBusinessStabilityScore(businessStabilityScore);
        creditReport.setFinancialStrengthScore(financialStrengthScore);
        creditReport.setPaymentBehaviorScore(paymentBehaviorScore);

        // Calculate overall credit score with enhanced weighting
        BigDecimal overallScore = calculateEnhancedOverallCreditScore(
                complianceScore, businessStabilityScore, financialStrengthScore, paymentBehaviorScore);

        creditReport.setCreditScore(overallScore);
        creditReport.setCreditScoreGrade(determineCreditGrade(overallScore));
        creditReport.setRiskCategory(determineRiskCategory(overallScore));
        creditReport.setCreditLimitRecommendation(calculateEnhancedCreditLimitRecommendation(overallScore, business, paymentAnalytics));

        // Set enhanced business metrics
        creditReport.setYearsInBusiness(calculateYearsInBusiness(business));
        creditReport.setGstComplianceStatus(business.getGstinVerified());
        creditReport.setPanVerificationStatus(business.getPanVerified());

        // Set payment-based trade reference counts
        if (paymentAnalytics != null) {
            creditReport.setTradeReferencesCount(paymentAnalytics.getTotalTransactions());

            // Calculate positive/negative based on payment performance
            int positiveReferences = calculatePositiveReferences(paymentAnalytics);
            creditReport.setPositiveReferencesCount(positiveReferences);
            creditReport.setNegativeReferencesCount(paymentAnalytics.getTotalTransactions() - positiveReferences);
        } else {
            // Fallback to mock data if no payment history
            creditReport.setTradeReferencesCount(generateMockTradeReferencesCount());
            creditReport.setPositiveReferencesCount(generateMockPositiveReferencesCount(creditReport.getTradeReferencesCount()));
            creditReport.setNegativeReferencesCount(creditReport.getTradeReferencesCount() - creditReport.getPositiveReferencesCount());
        }

        logger.debug("Enhanced credit scores calculated - Overall: {}, Grade: {}, Risk: {}",
                overallScore, creditReport.getCreditScoreGrade(), creditReport.getRiskCategory());
    }

    private BigDecimal calculateEnhancedPaymentBehaviorScore(Business business, PaymentAnalyticsResponse paymentAnalytics) {
        if (paymentAnalytics == null || paymentAnalytics.getTotalTransactions() == null || paymentAnalytics.getTotalTransactions() == 0) {
            // Fallback to basic scoring if no payment history
            return calculateBasicPaymentBehaviorScore(business);
        }

        BigDecimal score = BigDecimal.ZERO;

        // On-time payment percentage (40% weight)
        if (paymentAnalytics.getOnTimePaymentPercentage() != null) {
            BigDecimal onTimeScore = BigDecimal.valueOf(paymentAnalytics.getOnTimePaymentPercentage())
                    .multiply(new BigDecimal("0.4"));
            score = score.add(onTimeScore);
        }

        // Payment speed score (25% weight)
        if (paymentAnalytics.getPaymentSpeedScore() != null) {
            BigDecimal speedScore = BigDecimal.valueOf(paymentAnalytics.getPaymentSpeedScore())
                    .multiply(new BigDecimal("0.25"));
            score = score.add(speedScore);
        }

        // Dispute frequency score (20% weight)
        if (paymentAnalytics.getDisputeFrequencyScore() != null) {
            BigDecimal disputeScore = BigDecimal.valueOf(paymentAnalytics.getDisputeFrequencyScore())
                    .multiply(new BigDecimal("0.20"));
            score = score.add(disputeScore);
        }

        // Transaction volume bonus (15% weight)
        BigDecimal volumeScore = calculateVolumeScore(paymentAnalytics.getTotalTransactions())
                .multiply(new BigDecimal("0.15"));
        score = score.add(volumeScore);

        return score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEnhancedFinancialStrengthScore(Business business, PaymentAnalyticsResponse paymentAnalytics) {
        BigDecimal baseScore = calculateBasicFinancialStrengthScore(business);

        if (paymentAnalytics == null) {
            return baseScore;
        }

        // Transaction value adjustment
        if (paymentAnalytics.getTotalTransactionValue() != null) {
            BigDecimal transactionValue = paymentAnalytics.getTotalTransactionValue();

            if (transactionValue.compareTo(new BigDecimal("10000000")) >= 0) { // >= 1 Crore
                baseScore = baseScore.add(new BigDecimal("15"));
            } else if (transactionValue.compareTo(new BigDecimal("1000000")) >= 0) { // >= 10 Lakh
                baseScore = baseScore.add(new BigDecimal("10"));
            } else if (transactionValue.compareTo(new BigDecimal("500000")) >= 0) { // >= 5 Lakh
                baseScore = baseScore.add(new BigDecimal("5"));
            }
        }

        // Payment consistency adjustment
        if (paymentAnalytics.getOverallPaymentScore() != null) {
            BigDecimal paymentScore = BigDecimal.valueOf(paymentAnalytics.getOverallPaymentScore());

            if (paymentScore.compareTo(new BigDecimal("80")) >= 0) {
                baseScore = baseScore.add(new BigDecimal("10"));
            } else if (paymentScore.compareTo(new BigDecimal("60")) >= 0) {
                baseScore = baseScore.add(new BigDecimal("5"));
            } else if (paymentScore.compareTo(new BigDecimal("40")) < 0) {
                baseScore = baseScore.subtract(new BigDecimal("10"));
            }
        }

        return baseScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEnhancedComplianceScore(Business business) {
        // Same as basic compliance score for now
        return calculateBasicComplianceScore(business);
    }

    private BigDecimal calculateEnhancedBusinessStabilityScore(Business business) {
        // Same as basic stability score for now
        return calculateBasicBusinessStabilityScore(business);
    }

    private BigDecimal calculateEnhancedOverallCreditScore(BigDecimal compliance, BigDecimal stability,
                                                           BigDecimal financial, BigDecimal payment) {
        // Enhanced weighted average with more emphasis on payment behavior
        BigDecimal weightedScore = compliance.multiply(new BigDecimal("0.20"))      // 20% weight (reduced)
                .add(stability.multiply(new BigDecimal("0.20")))                   // 20% weight (same)
                .add(financial.multiply(new BigDecimal("0.25")))                   // 25% weight (reduced)
                .add(payment.multiply(new BigDecimal("0.35")));                    // 35% weight (increased)

        return weightedScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEnhancedCreditLimitRecommendation(BigDecimal score, Business business,
                                                                  PaymentAnalyticsResponse paymentAnalytics) {
        // Base credit limit calculation
        BigDecimal baseLimit = new BigDecimal("100000"); // ₹1 Lakh base

        // Adjust based on credit score
        BigDecimal multiplier = score.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal adjustedLimit = baseLimit.multiply(multiplier);

        // Payment history adjustments
        if (paymentAnalytics != null) {
            // Transaction volume adjustment
            if (paymentAnalytics.getTotalTransactionValue() != null) {
                BigDecimal avgTransactionValue = paymentAnalytics.getTotalTransactionValue()
                        .divide(new BigDecimal(paymentAnalytics.getTotalTransactions()), 2, RoundingMode.HALF_UP);

                // Set limit based on historical transaction patterns
                BigDecimal historyBasedLimit = avgTransactionValue.multiply(new BigDecimal("3")); // 3x average
                adjustedLimit = adjustedLimit.max(historyBasedLimit);
            }

            // Payment reliability adjustment
            if (paymentAnalytics.getOnTimePaymentPercentage() != null) {
                if (paymentAnalytics.getOnTimePaymentPercentage() >= 90) {
                    adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.5"));
                } else if (paymentAnalytics.getOnTimePaymentPercentage() >= 75) {
                    adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.2"));
                } else if (paymentAnalytics.getOnTimePaymentPercentage() < 50) {
                    adjustedLimit = adjustedLimit.multiply(new BigDecimal("0.7"));
                }
            }

            // Overdue amount penalty
            if (paymentAnalytics.getTotalOverdueAmount() != null &&
                    paymentAnalytics.getTotalOverdueAmount().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal overdueRatio = paymentAnalytics.getTotalOverdueAmount()
                        .divide(paymentAnalytics.getTotalTransactionValue(), 4, RoundingMode.HALF_UP);

                if (overdueRatio.compareTo(new BigDecimal("0.1")) > 0) { // > 10% overdue
                    adjustedLimit = adjustedLimit.multiply(new BigDecimal("0.6"));
                } else if (overdueRatio.compareTo(new BigDecimal("0.05")) > 0) { // > 5% overdue
                    adjustedLimit = adjustedLimit.multiply(new BigDecimal("0.8"));
                }
            }
        }

        // Industry-based adjustments (same as before)
        switch (business.getIndustryCategory()) {
            case TECHNOLOGY:
            case HEALTHCARE:
                adjustedLimit = adjustedLimit.multiply(new BigDecimal("2.0"));
                break;
            case MANUFACTURING:
            case RETAIL:
                adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.5"));
                break;
            case CONSTRUCTION:
                adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.2"));
                break;
        }

        // Years in business bonus (same as before)
        int yearsInBusiness = calculateYearsInBusiness(business);
        if (yearsInBusiness >= 10) {
            adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.5"));
        } else if (yearsInBusiness >= 5) {
            adjustedLimit = adjustedLimit.multiply(new BigDecimal("1.2"));
        }

        // Round to nearest 10,000
        return adjustedLimit.divide(new BigDecimal("10000"), 0, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("10000"));
    }

    private void generateEnhancedReportContent(CreditReport creditReport, Business business,
                                               PaymentAnalyticsResponse paymentAnalytics) {
        // Generate enhanced summary
        creditReport.setSummary(generateEnhancedSummary(creditReport, business, paymentAnalytics));

        // Generate enhanced recommendations
        creditReport.setRecommendations(generateEnhancedRecommendations(creditReport, business, paymentAnalytics));

        // Generate enhanced risk factors
        creditReport.setRiskFactors(generateEnhancedRiskFactors(creditReport, business, paymentAnalytics));

        // Generate enhanced positive indicators
        creditReport.setPositiveIndicators(generateEnhancedPositiveIndicators(creditReport, business, paymentAnalytics));
    }

    private String generateEnhancedSummary(CreditReport creditReport, Business business,
                                           PaymentAnalyticsResponse paymentAnalytics) {
        StringBuilder summary = new StringBuilder();

        summary.append(String.format(
                "%s is a %s company in the %s sector with a credit score of %.2f (%s grade). ",
                business.getBusinessName(),
                business.getBusinessType().toString().toLowerCase().replace("_", " "),
                business.getIndustryCategory().toString().toLowerCase().replace("_", " "),
                creditReport.getCreditScore(),
                creditReport.getCreditScoreGrade()
        ));

        summary.append(String.format(
                "The business has been operating for %d years and shows %s risk characteristics. ",
                creditReport.getYearsInBusiness(),
                creditReport.getRiskCategory().toString().toLowerCase().replace("_", " ")
        ));

        if (paymentAnalytics != null && paymentAnalytics.getTotalTransactions() != null && paymentAnalytics.getTotalTransactions() > 0) {
            summary.append(String.format(
                    "Based on %d payment transactions worth %s, the business demonstrates %.1f%% on-time payment performance. ",
                    paymentAnalytics.getTotalTransactions(),
                    formatCurrency(paymentAnalytics.getTotalTransactionValue()),
                    paymentAnalytics.getOnTimePaymentPercentage() != null ? paymentAnalytics.getOnTimePaymentPercentage() : 0.0
            ));
        }

        summary.append(String.format(
                "We recommend a credit limit of %s based on this comprehensive analysis.",
                formatCurrency(creditReport.getCreditLimitRecommendation())
        ));

        return summary.toString();
    }

    private String generateEnhancedRecommendations(CreditReport creditReport, Business business,
                                                   PaymentAnalyticsResponse paymentAnalytics) {
        StringBuilder recommendations = new StringBuilder();

        if (creditReport.getCreditScore().compareTo(new BigDecimal("75")) >= 0) {
            recommendations.append("• Excellent creditworthiness - proceed with confidence\n");

            if (paymentAnalytics != null && paymentAnalytics.getOnTimePaymentPercentage() != null &&
                    paymentAnalytics.getOnTimePaymentPercentage() >= 90) {
                recommendations.append("• Outstanding payment history supports extended payment terms\n");
            }
        } else if (creditReport.getCreditScore().compareTo(new BigDecimal("60")) >= 0) {
            recommendations.append("• Good creditworthiness - proceed with standard terms\n");
            recommendations.append("• Monitor payment patterns regularly\n");
        } else {
            recommendations.append("• Exercise caution in credit decisions\n");
            recommendations.append("• Consider requiring guarantees or collateral\n");
            recommendations.append("• Implement shorter payment terms\n");
        }

        // Payment history based recommendations
        if (paymentAnalytics != null) {
            if (paymentAnalytics.getAveragePaymentDelay() != null && paymentAnalytics.getAveragePaymentDelay() > 15) {
                recommendations.append("• Consider shorter payment terms due to historical delays\n");
            }

            if (paymentAnalytics.getTotalDisputes() != null && paymentAnalytics.getTotalDisputes() > 0) {
                recommendations.append("• Monitor for potential disputes based on historical patterns\n");
            }

            if (paymentAnalytics.getTotalOverdueAmount() != null &&
                    paymentAnalytics.getTotalOverdueAmount().compareTo(BigDecimal.ZERO) > 0) {
                recommendations.append(String.format("• Current overdue amount of %s requires attention\n",
                        formatCurrency(paymentAnalytics.getTotalOverdueAmount())));
            }
        }

        // Standard recommendations
        if (!Boolean.TRUE.equals(business.getGstinVerified())) {
            recommendations.append("• Verify GST registration before proceeding\n");
        }

        if (!Boolean.TRUE.equals(business.getPanVerified())) {
            recommendations.append("• Verify PAN details before proceeding\n");
        }

        return recommendations.toString();
    }

    private String generateEnhancedRiskFactors(CreditReport creditReport, Business business,
                                               PaymentAnalyticsResponse paymentAnalytics) {
        StringBuilder riskFactors = new StringBuilder();

        // Standard risk factors
        if (creditReport.getYearsInBusiness() < 2) {
            riskFactors.append("• Relatively new business with limited operating history\n");
        }

        if (!Boolean.TRUE.equals(business.getGstinVerified())) {
            riskFactors.append("• GST registration not verified\n");
        }

        if (!Boolean.TRUE.equals(business.getPanVerified())) {
            riskFactors.append("• PAN not verified\n");
        }

        // Payment history risk factors
        if (paymentAnalytics != null) {
            if (paymentAnalytics.getOnTimePaymentPercentage() != null &&
                    paymentAnalytics.getOnTimePaymentPercentage() < 70) {
                riskFactors.append(String.format("• Below-average on-time payment rate of %.1f%%\n",
                        paymentAnalytics.getOnTimePaymentPercentage()));
            }

            if (paymentAnalytics.getAveragePaymentDelay() != null &&
                    paymentAnalytics.getAveragePaymentDelay() > 30) {
                riskFactors.append(String.format("• High average payment delay of %.1f days\n",
                        paymentAnalytics.getAveragePaymentDelay()));
            }

            if (paymentAnalytics.getLongestPaymentDelay() != null &&
                    paymentAnalytics.getLongestPaymentDelay() > 90) {
                riskFactors.append(String.format("• Maximum payment delay of %d days indicates potential cash flow issues\n",
                        paymentAnalytics.getLongestPaymentDelay()));
            }

            if (paymentAnalytics.getTotalDisputes() != null && paymentAnalytics.getTotalDisputes() > 0) {
                riskFactors.append(String.format("• %d payment disputes on record\n",
                        paymentAnalytics.getTotalDisputes()));
            }
        }

        if (creditReport.getCreditScore().compareTo(new BigDecimal("50")) < 0) {
            riskFactors.append("• Below-average credit score\n");
        }

        if (riskFactors.length() == 0) {
            riskFactors.append("• No significant risk factors identified");
        }

        return riskFactors.toString();
    }

    private String generateEnhancedPositiveIndicators(CreditReport creditReport, Business business,
                                                      PaymentAnalyticsResponse paymentAnalytics) {
        StringBuilder positiveIndicators = new StringBuilder();

        // Standard positive indicators
        if (Boolean.TRUE.equals(business.getGstinVerified())) {
            positiveIndicators.append("• GST registration verified\n");
        }

        if (Boolean.TRUE.equals(business.getPanVerified())) {
            positiveIndicators.append("• PAN verified\n");
        }

        if (creditReport.getYearsInBusiness() >= 5) {
            positiveIndicators.append("• Established business with proven track record\n");
        }

        if (creditReport.getCreditScore().compareTo(new BigDecimal("75")) >= 0) {
            positiveIndicators.append("• High credit score indicating strong financial health\n");
        }

        // Payment history positive indicators
        if (paymentAnalytics != null) {
            if (paymentAnalytics.getOnTimePaymentPercentage() != null &&
                    paymentAnalytics.getOnTimePaymentPercentage() >= 85) {
                positiveIndicators.append(String.format("• Excellent on-time payment rate of %.1f%%\n",
                        paymentAnalytics.getOnTimePaymentPercentage()));
            }

            if (paymentAnalytics.getTotalTransactions() != null && paymentAnalytics.getTotalTransactions() >= 10) {
                positiveIndicators.append(String.format("• Substantial payment history with %d transactions\n",
                        paymentAnalytics.getTotalTransactions()));
            }

            if (paymentAnalytics.getTotalTransactionValue() != null &&
                    paymentAnalytics.getTotalTransactionValue().compareTo(new BigDecimal("1000000")) >= 0) {
                positiveIndicators.append(String.format("• Significant transaction volume of %s\n",
                        formatCurrency(paymentAnalytics.getTotalTransactionValue())));
            }

            if (paymentAnalytics.getAveragePaymentDelay() != null &&
                    paymentAnalytics.getAveragePaymentDelay() <= 5) {
                positiveIndicators.append("• Consistently prompt payment behavior\n");
            }
        }

        if (business.getWebsite() != null && !business.getWebsite().isEmpty()) {
            positiveIndicators.append("• Professional web presence\n");
        }

        return positiveIndicators.toString();
    }

    // Helper methods (keeping existing ones and adding new ones)

    private BigDecimal calculateVolumeScore(Integer totalTransactions) {
        if (totalTransactions == null || totalTransactions == 0) {
            return BigDecimal.ZERO;
        }

        if (totalTransactions >= 50) return new BigDecimal("100");
        if (totalTransactions >= 25) return new BigDecimal("80");
        if (totalTransactions >= 10) return new BigDecimal("60");
        if (totalTransactions >= 5) return new BigDecimal("40");
        return new BigDecimal("20");
    }

    private int calculatePositiveReferences(PaymentAnalyticsResponse paymentAnalytics) {
        if (paymentAnalytics.getOnTimePaymentPercentage() == null) {
            return 0;
        }

        double positivePercentage = paymentAnalytics.getOnTimePaymentPercentage() / 100.0;
        return (int) (paymentAnalytics.getTotalTransactions() * positivePercentage);
    }

    // Keep all existing helper methods from original CreditReportService
    private BigDecimal calculateBasicComplianceScore(Business business) {
        BigDecimal score = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(business.getGstinVerified())) {
            score = score.add(new BigDecimal("40"));
        }

        if (Boolean.TRUE.equals(business.getPanVerified())) {
            score = score.add(new BigDecimal("30"));
        }

        if (Boolean.TRUE.equals(business.getIsActive())) {
            score = score.add(new BigDecimal("30"));
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBasicBusinessStabilityScore(Business business) {
        BigDecimal score = new BigDecimal("50");

        int yearsInBusiness = calculateYearsInBusiness(business);
        if (yearsInBusiness >= 10) {
            score = score.add(new BigDecimal("30"));
        } else if (yearsInBusiness >= 5) {
            score = score.add(new BigDecimal("20"));
        } else if (yearsInBusiness >= 2) {
            score = score.add(new BigDecimal("10"));
        }

        if (business.getBusinessDescription() != null && !business.getBusinessDescription().isEmpty()) {
            score = score.add(new BigDecimal("10"));
        }

        if (business.getWebsite() != null && !business.getWebsite().isEmpty()) {
            score = score.add(new BigDecimal("10"));
        }

        return score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBasicFinancialStrengthScore(Business business) {
        BigDecimal baseScore = new BigDecimal("60");

        switch (business.getIndustryCategory()) {
            case TECHNOLOGY:
            case HEALTHCARE:
                baseScore = baseScore.add(new BigDecimal("20"));
                break;
            case MANUFACTURING:
            case RETAIL:
                baseScore = baseScore.add(new BigDecimal("15"));
                break;
            case CONSTRUCTION:
            case AGRICULTURE:
                baseScore = baseScore.add(new BigDecimal("10"));
                break;
            default:
                baseScore = baseScore.add(new BigDecimal("5"));
        }

        if (business.getBusinessType() == BusinessType.PRIVATE_LIMITED) {
            baseScore = baseScore.add(new BigDecimal("15"));
        } else if (business.getBusinessType() == BusinessType.PUBLIC_LIMITED) {
            baseScore = baseScore.add(new BigDecimal("20"));
        } else {
            baseScore = baseScore.add(new BigDecimal("5"));
        }

        return baseScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBasicPaymentBehaviorScore(Business business) {
        BigDecimal baseScore = new BigDecimal("70");

        int yearsInBusiness = calculateYearsInBusiness(business);
        if (yearsInBusiness >= 5) {
            baseScore = baseScore.add(new BigDecimal("20"));
        } else if (yearsInBusiness >= 2) {
            baseScore = baseScore.add(new BigDecimal("10"));
        }

        if (Boolean.TRUE.equals(business.getGstinVerified()) && Boolean.TRUE.equals(business.getPanVerified())) {
            baseScore = baseScore.add(new BigDecimal("10"));
        }

        return baseScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateYearsInBusiness(Business business) {
        if (business.getRegistrationDate() == null) {
            return 0;
        }

        LocalDate registrationDate = business.getRegistrationDate();
        LocalDate currentDate = LocalDate.now();

        return Period.between(registrationDate, currentDate).getYears();
    }

    private CreditGrade determineCreditGrade(BigDecimal score) {
        int scoreInt = score.intValue();

        if (scoreInt >= 90) return CreditGrade.AAA;
        if (scoreInt >= 80) return CreditGrade.AA;
        if (scoreInt >= 70) return CreditGrade.A;
        if (scoreInt >= 60) return CreditGrade.BBB;
        if (scoreInt >= 50) return CreditGrade.BB;
        if (scoreInt >= 40) return CreditGrade.B;
        if (scoreInt >= 30) return CreditGrade.CCC;
        if (scoreInt >= 20) return CreditGrade.CC;
        if (scoreInt >= 10) return CreditGrade.C;
        return CreditGrade.D;
    }

    private RiskCategory determineRiskCategory(BigDecimal score) {
        int scoreInt = score.intValue();

        if (scoreInt >= 75) return RiskCategory.LOW;
        if (scoreInt >= 60) return RiskCategory.MODERATE;
        if (scoreInt >= 40) return RiskCategory.HIGH;
        return RiskCategory.VERY_HIGH;
    }

    private Integer generateMockTradeReferencesCount() {
        return (int) (Math.random() * 20) + 5;
    }

    private Integer generateMockPositiveReferencesCount(Integer totalReferences) {
        double positivePercentage = 0.70 + (Math.random() * 0.25);
        return (int) (totalReferences * positivePercentage);
    }

    private String generateReportNumber() {
        return "TCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0";

        if (amount.compareTo(new BigDecimal("10000000")) >= 0) {
            return String.format("₹%.2f Cr", amount.divide(new BigDecimal("10000000"), 2, RoundingMode.HALF_UP));
        } else if (amount.compareTo(new BigDecimal("100000")) >= 0) {
            return String.format("₹%.2f L", amount.divide(new BigDecimal("100000"), 2, RoundingMode.HALF_UP));
        } else {
            return String.format("₹%.0f", amount);
        }
    }

    private CreditReportResponse convertToResponse(CreditReport creditReport) {
        CreditReportResponse response = new CreditReportResponse();

        response.setId(creditReport.getId());
        response.setReportNumber(creditReport.getReportNumber());
        response.setBusinessId(creditReport.getBusiness().getId());
        response.setBusinessName(creditReport.getBusiness().getBusinessName());
        response.setBusinessGstin(creditReport.getBusiness().getGstin());
        response.setBusinessPan(creditReport.getBusiness().getPan());
        response.setCreditScoreGrade(creditReport.getCreditScoreGrade());
        response.setCreditScore(creditReport.getCreditScore());
        response.setCreditLimitRecommendation(creditReport.getCreditLimitRecommendation());
        response.setRiskCategory(creditReport.getRiskCategory());

        response.setFinancialStrengthScore(creditReport.getFinancialStrengthScore());
        response.setPaymentBehaviorScore(creditReport.getPaymentBehaviorScore());
        response.setBusinessStabilityScore(creditReport.getBusinessStabilityScore());
        response.setComplianceScore(creditReport.getComplianceScore());

        response.setYearsInBusiness(creditReport.getYearsInBusiness());
        response.setGstComplianceStatus(creditReport.getGstComplianceStatus());
        response.setPanVerificationStatus(creditReport.getPanVerificationStatus());
        response.setTradeReferencesCount(creditReport.getTradeReferencesCount());
        response.setPositiveReferencesCount(creditReport.getPositiveReferencesCount());
        response.setNegativeReferencesCount(creditReport.getNegativeReferencesCount());

        response.setSummary(creditReport.getSummary());
        response.setRecommendations(creditReport.getRecommendations());
        response.setRiskFactors(creditReport.getRiskFactors());
        response.setPositiveIndicators(creditReport.getPositiveIndicators());

        response.setReportValidUntil(creditReport.getReportValidUntil());
        response.setReportStatus(creditReport.getReportStatus());
        response.setRequestedByName(creditReport.getRequestedBy().getFirstName() + " " +
                creditReport.getRequestedBy().getLastName());
        response.setCreatedAt(creditReport.getCreatedAt());
        response.setUpdatedAt(creditReport.getUpdatedAt());

        return response;
    }

    // Expose methods for backward compatibility
    public CreditReportResponse generateCreditReport(CreditReportRequest request) {
        return generateEnhancedCreditReport(request);
    }

    public CreditReportResponse getCreditReport(String reportNumber) {
        logger.info("Retrieving credit report with number: {}", reportNumber);

        CreditReport creditReport = creditReportRepository.findByReportNumber(reportNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Credit report not found with number: " + reportNumber));

        return convertToResponse(creditReport);
    }

    public CreditReportResponse getCreditReportById(Long reportId) {
        logger.info("Retrieving credit report with ID: {}", reportId);

        CreditReport creditReport = creditReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit report not found with ID: " + reportId));

        return convertToResponse(creditReport);
    }

    public List<CreditReportResponse> getBusinessCreditHistory(Long businessId) {
        logger.info("Retrieving credit history for business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        List<CreditReport> reports = creditReportRepository.findByBusinessOrderByCreatedAtDesc(business);

        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<CreditReportResponse> getUserCreditReports(int page, int size) {
        logger.info("Retrieving credit reports for current user - page: {}, size: {}", page, size);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CreditReport> reportsPage = creditReportRepository.findByRequestedByOrderByCreatedAtDesc(user, pageable);

        return reportsPage.map(this::convertToResponse);
    }
}