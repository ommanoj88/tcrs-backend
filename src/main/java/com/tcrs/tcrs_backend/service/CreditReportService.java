package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.credit.CreditReportRequest;
import com.tcrs.tcrs_backend.dto.credit.CreditReportResponse;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
import com.tcrs.tcrs_backend.repository.CreditReportRepository;
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
public class CreditReportService {

    private static final Logger logger = LoggerFactory.getLogger(CreditReportService.class);
    private static final int REPORT_VALIDITY_DAYS = 30; // Reports valid for 30 days

    @Autowired
    private CreditReportRepository creditReportRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    public CreditReportResponse generateCreditReport(CreditReportRequest request) {
        logger.info("Generating credit report for business ID: {}", request.getBusinessId());

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

        // Generate new credit report
        CreditReport creditReport = new CreditReport();
        creditReport.setBusiness(business);
        creditReport.setRequestedBy(requestedBy);
        creditReport.setReportNumber(generateReportNumber());
        creditReport.setReportStatus(ReportStatus.GENERATED);
        creditReport.setReportValidUntil(LocalDateTime.now().plusDays(REPORT_VALIDITY_DAYS));

        // Calculate credit scores
        calculateCreditScores(creditReport, business);

        // Generate report content
        generateReportContent(creditReport, business);

        // Save credit report
        CreditReport savedReport = creditReportRepository.save(creditReport);

        logger.info("Credit report generated successfully with number: {}", savedReport.getReportNumber());

        return convertToResponse(savedReport);
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

    private void calculateCreditScores(CreditReport creditReport, Business business) {
        logger.debug("Calculating credit scores for business: {}", business.getBusinessName());

        // Calculate component scores
        BigDecimal complianceScore = calculateComplianceScore(business);
        BigDecimal businessStabilityScore = calculateBusinessStabilityScore(business);
        BigDecimal financialStrengthScore = calculateFinancialStrengthScore(business);
        BigDecimal paymentBehaviorScore = calculatePaymentBehaviorScore(business);

        // Set component scores
        creditReport.setComplianceScore(complianceScore);
        creditReport.setBusinessStabilityScore(businessStabilityScore);
        creditReport.setFinancialStrengthScore(financialStrengthScore);
        creditReport.setPaymentBehaviorScore(paymentBehaviorScore);

        // Calculate overall credit score (weighted average)
        BigDecimal overallScore = calculateOverallCreditScore(
                complianceScore, businessStabilityScore, financialStrengthScore, paymentBehaviorScore);

        creditReport.setCreditScore(overallScore);
        creditReport.setCreditScoreGrade(determineCreditGrade(overallScore));
        creditReport.setRiskCategory(determineRiskCategory(overallScore));
        creditReport.setCreditLimitRecommendation(calculateCreditLimitRecommendation(overallScore, business));

        // Set business metrics
        creditReport.setYearsInBusiness(calculateYearsInBusiness(business));
        creditReport.setGstComplianceStatus(business.getGstinVerified());
        creditReport.setPanVerificationStatus(business.getPanVerified());

        // Set trade reference counts (mock data for now)
        creditReport.setTradeReferencesCount(generateMockTradeReferencesCount());
        creditReport.setPositiveReferencesCount(generateMockPositiveReferencesCount(creditReport.getTradeReferencesCount()));
        creditReport.setNegativeReferencesCount(creditReport.getTradeReferencesCount() - creditReport.getPositiveReferencesCount());

        logger.debug("Credit scores calculated - Overall: {}, Grade: {}, Risk: {}",
                overallScore, creditReport.getCreditScoreGrade(), creditReport.getRiskCategory());
    }

    private BigDecimal calculateComplianceScore(Business business) {
        BigDecimal score = BigDecimal.ZERO;

        // GST verification (40% weight)
        if (Boolean.TRUE.equals(business.getGstinVerified())) {
            score = score.add(new BigDecimal("40"));
        }

        // PAN verification (30% weight)
        if (Boolean.TRUE.equals(business.getPanVerified())) {
            score = score.add(new BigDecimal("30"));
        }

        // Business active status (30% weight)
        if (Boolean.TRUE.equals(business.getIsActive())) {
            score = score.add(new BigDecimal("30"));
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBusinessStabilityScore(Business business) {
        BigDecimal score = new BigDecimal("50"); // Base score

        // Years in business
        int yearsInBusiness = calculateYearsInBusiness(business);
        if (yearsInBusiness >= 10) {
            score = score.add(new BigDecimal("30"));
        } else if (yearsInBusiness >= 5) {
            score = score.add(new BigDecimal("20"));
        } else if (yearsInBusiness >= 2) {
            score = score.add(new BigDecimal("10"));
        }

        // Complete business profile
        if (business.getBusinessDescription() != null && !business.getBusinessDescription().isEmpty()) {
            score = score.add(new BigDecimal("10"));
        }

        if (business.getWebsite() != null && !business.getWebsite().isEmpty()) {
            score = score.add(new BigDecimal("10"));
        }

        return score.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinancialStrengthScore(Business business) {
        // Mock financial strength calculation
        // In real implementation, this would analyze financial statements, bank data, etc.

        BigDecimal baseScore = new BigDecimal("60"); // Base score

        // Industry-based scoring
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

        // Business type scoring
        if (business.getBusinessType() == BusinessType.PRIVATE_LIMITED) {
            baseScore = baseScore.add(new BigDecimal("15"));
        } else if (business.getBusinessType() == BusinessType.PUBLIC_LIMITED) {
            baseScore = baseScore.add(new BigDecimal("20"));
        } else {
            baseScore = baseScore.add(new BigDecimal("5"));
        }

        return baseScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePaymentBehaviorScore(Business business) {
        // Mock payment behavior calculation
        // In real implementation, this would analyze payment history, defaults, etc.

        BigDecimal baseScore = new BigDecimal("70"); // Assume good payment behavior

        // Adjust based on business age
        int yearsInBusiness = calculateYearsInBusiness(business);
        if (yearsInBusiness >= 5) {
            baseScore = baseScore.add(new BigDecimal("20"));
        } else if (yearsInBusiness >= 2) {
            baseScore = baseScore.add(new BigDecimal("10"));
        }

        // Verification bonus
        if (Boolean.TRUE.equals(business.getGstinVerified()) && Boolean.TRUE.equals(business.getPanVerified())) {
            baseScore = baseScore.add(new BigDecimal("10"));
        }

        return baseScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOverallCreditScore(BigDecimal compliance, BigDecimal stability,
                                                   BigDecimal financial, BigDecimal payment) {
        // Weighted average calculation
        BigDecimal weightedScore = compliance.multiply(new BigDecimal("0.25"))      // 25% weight
                .add(stability.multiply(new BigDecimal("0.20")))                   // 20% weight
                .add(financial.multiply(new BigDecimal("0.30")))                   // 30% weight
                .add(payment.multiply(new BigDecimal("0.25")));                    // 25% weight

        return weightedScore.setScale(2, RoundingMode.HALF_UP);
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

    private BigDecimal calculateCreditLimitRecommendation(BigDecimal score, Business business) {
        // Base credit limit calculation
        BigDecimal baseLimit = new BigDecimal("100000"); // ₹1 Lakh base

        // Adjust based on credit score
        BigDecimal multiplier = score.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal adjustedLimit = baseLimit.multiply(multiplier);

        // Industry-based adjustments
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

        // Years in business bonus
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

    private int calculateYearsInBusiness(Business business) {
        if (business.getRegistrationDate() == null) {
            return 0;
        }

        LocalDate registrationDate = business.getRegistrationDate();
        LocalDate currentDate = LocalDate.now();

        return Period.between(registrationDate, currentDate).getYears();
    }

    private Integer generateMockTradeReferencesCount() {
        // Mock trade references count (in real implementation, this would come from database)
        return (int) (Math.random() * 20) + 5; // 5-25 references
    }

    private Integer generateMockPositiveReferencesCount(Integer totalReferences) {
        // Mock positive references (70-95% positive)
        double positivePercentage = 0.70 + (Math.random() * 0.25);
        return (int) (totalReferences * positivePercentage);
    }

    private void generateReportContent(CreditReport creditReport, Business business) {
        // Generate summary
        creditReport.setSummary(generateSummary(creditReport, business));

        // Generate recommendations
        creditReport.setRecommendations(generateRecommendations(creditReport, business));

        // Generate risk factors
        creditReport.setRiskFactors(generateRiskFactors(creditReport, business));

        // Generate positive indicators
        creditReport.setPositiveIndicators(generatePositiveIndicators(creditReport, business));
    }

    private String generateSummary(CreditReport creditReport, Business business) {
        return String.format(
                "%s is a %s company in the %s sector with a credit score of %.2f (%s grade). " +
                        "The business has been operating for %d years and shows %s risk characteristics. " +
                        "Based on our analysis, we recommend a credit limit of ₹%s.",
                business.getBusinessName(),
                business.getBusinessType().toString().toLowerCase().replace("_", " "),
                business.getIndustryCategory().toString().toLowerCase().replace("_", " "),
                creditReport.getCreditScore(),
                creditReport.getCreditScoreGrade(),
                creditReport.getYearsInBusiness(),
                creditReport.getRiskCategory().toString().toLowerCase().replace("_", " "),
                formatCurrency(creditReport.getCreditLimitRecommendation())
        );
    }

    private String generateRecommendations(CreditReport creditReport, Business business) {
        StringBuilder recommendations = new StringBuilder();

        if (creditReport.getCreditScore().compareTo(new BigDecimal("75")) >= 0) {
            recommendations.append("• Excellent creditworthiness - proceed with confidence\n");
            recommendations.append("• Consider extended payment terms\n");
        } else if (creditReport.getCreditScore().compareTo(new BigDecimal("60")) >= 0) {
            recommendations.append("• Good creditworthiness - proceed with standard terms\n");
            recommendations.append("• Monitor payment patterns regularly\n");
        } else {
            recommendations.append("• Exercise caution in credit decisions\n");
            recommendations.append("• Consider requiring guarantees or collateral\n");
            recommendations.append("• Implement shorter payment terms\n");
        }

        if (!Boolean.TRUE.equals(business.getGstinVerified())) {
            recommendations.append("• Verify GST registration before proceeding\n");
        }

        if (!Boolean.TRUE.equals(business.getPanVerified())) {
            recommendations.append("• Verify PAN details before proceeding\n");
        }

        return recommendations.toString();
    }

    private String generateRiskFactors(CreditReport creditReport, Business business) {
        StringBuilder riskFactors = new StringBuilder();

        if (creditReport.getYearsInBusiness() < 2) {
            riskFactors.append("• Relatively new business with limited operating history\n");
        }

        if (!Boolean.TRUE.equals(business.getGstinVerified())) {
            riskFactors.append("• GST registration not verified\n");
        }

        if (!Boolean.TRUE.equals(business.getPanVerified())) {
            riskFactors.append("• PAN not verified\n");
        }

        if (creditReport.getCreditScore().compareTo(new BigDecimal("50")) < 0) {
            riskFactors.append("• Below-average credit score\n");
        }

        if (creditReport.getNegativeReferencesCount() > 0) {
            riskFactors.append(String.format("• %d negative trade references on record\n",
                    creditReport.getNegativeReferencesCount()));
        }

        if (riskFactors.length() == 0) {
            riskFactors.append("• No significant risk factors identified");
        }

        return riskFactors.toString();
    }

    private String generatePositiveIndicators(CreditReport creditReport, Business business) {
        StringBuilder positiveIndicators = new StringBuilder();

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

        if (creditReport.getPositiveReferencesCount() > 0) {
            positiveIndicators.append(String.format("• %d positive trade references\n",
                    creditReport.getPositiveReferencesCount()));
        }

        if (business.getWebsite() != null && !business.getWebsite().isEmpty()) {
            positiveIndicators.append("• Professional web presence\n");
        }

        return positiveIndicators.toString();
    }

    private String generateReportNumber() {
        return "TCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";

        // Format Indian currency
        if (amount.compareTo(new BigDecimal("10000000")) >= 0) { // >= 1 Crore
            return String.format("%.2f Cr", amount.divide(new BigDecimal("10000000"), 2, RoundingMode.HALF_UP));
        } else if (amount.compareTo(new BigDecimal("100000")) >= 0) { // >= 1 Lakh
            return String.format("%.2f L", amount.divide(new BigDecimal("100000"), 2, RoundingMode.HALF_UP));
        } else {
            return String.format("%.0f", amount);
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
}
