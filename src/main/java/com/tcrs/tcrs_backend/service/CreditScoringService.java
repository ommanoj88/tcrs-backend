package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class CreditScoringService {

    private static final Logger logger = LoggerFactory.getLogger(CreditScoringService.class);

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private TradeReferenceRepository tradeReferenceRepository;

    @Autowired
    private CreditReportRepository creditReportRepository;

    // Credit scoring weights
    private static final BigDecimal PAYMENT_HISTORY_WEIGHT = new BigDecimal("0.35");
    private static final BigDecimal TRADE_REFERENCE_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal BUSINESS_PROFILE_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal CREDIT_UTILIZATION_WEIGHT = new BigDecimal("0.10");
    private static final BigDecimal BUSINESS_AGE_WEIGHT = new BigDecimal("0.10");

    // Base scores
    private static final BigDecimal BASE_SCORE = new BigDecimal("500");
    private static final BigDecimal MIN_SCORE = new BigDecimal("300");
    private static final BigDecimal MAX_SCORE = new BigDecimal("850");

    public BigDecimal calculateCurrentCreditScore(Long businessId) {
        logger.info("Calculating current credit score for business ID: {}", businessId);

        try {
            Business business = businessRepository.findById(businessId)
                    .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));

            BigDecimal paymentScore = calculatePaymentHistoryScore(businessId);
            BigDecimal tradeReferenceScore = calculateTradeReferenceScore(businessId);
            BigDecimal businessProfileScore = calculateBusinessProfileScore(business);
            BigDecimal creditUtilizationScore = calculateCreditUtilizationScore(businessId);
            BigDecimal businessAgeScore = calculateBusinessAgeScore(business);

            // Calculate weighted score
            BigDecimal totalScore = BASE_SCORE
                    .add(paymentScore.multiply(PAYMENT_HISTORY_WEIGHT))
                    .add(tradeReferenceScore.multiply(TRADE_REFERENCE_WEIGHT))
                    .add(businessProfileScore.multiply(BUSINESS_PROFILE_WEIGHT))
                    .add(creditUtilizationScore.multiply(CREDIT_UTILIZATION_WEIGHT))
                    .add(businessAgeScore.multiply(BUSINESS_AGE_WEIGHT));

            // Ensure score is within bounds
            if (totalScore.compareTo(MIN_SCORE) < 0) {
                totalScore = MIN_SCORE;
            } else if (totalScore.compareTo(MAX_SCORE) > 0) {
                totalScore = MAX_SCORE;
            }

            logger.info("Calculated credit score for business {}: {}", business.getBusinessName(), totalScore);

            return totalScore.setScale(1, RoundingMode.HALF_UP);

        } catch (Exception e) {
            logger.error("Error calculating credit score for business ID: {}", businessId, e);
            return BASE_SCORE; // Return base score on error
        }
    }

    private BigDecimal calculatePaymentHistoryScore(Long businessId) {
        try {
            List<PaymentHistory> paymentHistories = paymentHistoryRepository.findByBusinessIdAndIsActiveTrue(businessId);

            if (paymentHistories.isEmpty()) {
                return BigDecimal.ZERO; // No payment history
            }

            BigDecimal totalScore = BigDecimal.ZERO;
            int totalPayments = paymentHistories.size();
            int onTimePayments = 0;
            int latePayments = 0;
            int veryLatePayments = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (PaymentHistory payment : paymentHistories) {
                totalAmount = totalAmount.add(payment.getTransactionAmount());

                if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                    if (payment.getDaysDelayed() == null || payment.getDaysDelayed() <= 0) {
                        onTimePayments++;
                        totalScore = totalScore.add(new BigDecimal("50")); // Positive score for on-time
                    } else if (payment.getDaysDelayed() <= 30) {
                        latePayments++;
                        totalScore = totalScore.subtract(new BigDecimal("10")); // Minor penalty
                    } else {
                        veryLatePayments++;
                        totalScore = totalScore.subtract(new BigDecimal("30")); // Major penalty
                    }
                } else if (payment.getPaymentStatus() == PaymentStatus.OVERDUE) {
                    totalScore = totalScore.subtract(new BigDecimal("40")); // Overdue penalty
                } else if (payment.getPaymentStatus() == PaymentStatus.DEFAULTED) {
                    totalScore = totalScore.subtract(new BigDecimal("100")); // Major default penalty
                }
            }

            // Calculate percentage-based adjustments
            if (totalPayments > 0) {
                BigDecimal onTimePercentage = new BigDecimal(onTimePayments)
                        .divide(new BigDecimal(totalPayments), 4, RoundingMode.HALF_UP);

                if (onTimePercentage.compareTo(new BigDecimal("0.95")) >= 0) {
                    totalScore = totalScore.add(new BigDecimal("50")); // Excellent payment history
                } else if (onTimePercentage.compareTo(new BigDecimal("0.85")) >= 0) {
                    totalScore = totalScore.add(new BigDecimal("25")); // Good payment history
                }
            }

            // Average per payment
            BigDecimal averageScore = totalScore.divide(new BigDecimal(totalPayments), 2, RoundingMode.HALF_UP);

            // Cap the score
            if (averageScore.compareTo(new BigDecimal("100")) > 0) {
                averageScore = new BigDecimal("100");
            } else if (averageScore.compareTo(new BigDecimal("-100")) < 0) {
                averageScore = new BigDecimal("-100");
            }

            logger.debug("Payment history score for business {}: {}", businessId, averageScore);
            return averageScore;

        } catch (Exception e) {
            logger.error("Error calculating payment history score for business: {}", businessId, e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTradeReferenceScore(Long businessId) {
        try {
            List<TradeReference> tradeReferences = tradeReferenceRepository.findByBusinessIdAndIsActiveTrue(businessId);

            if (tradeReferences.isEmpty()) {
                return new BigDecimal("-20"); // Penalty for no trade references
            }

            BigDecimal totalScore = BigDecimal.ZERO;
            int verifiedReferences = 0;
            int positiveReferences = 0;
            int negativeReferences = 0;

            for (TradeReference reference : tradeReferences) {
                // Verification bonus
                if (reference.getVerificationStatus() == ReferenceVerificationStatus.VERIFIED) {
                    verifiedReferences++;
                    totalScore = totalScore.add(new BigDecimal("20"));
                } else if (reference.getVerificationStatus() == ReferenceVerificationStatus.PARTIALLY_VERIFIED) {
                    totalScore = totalScore.add(new BigDecimal("10"));
                }

                // Payment behavior scoring
                switch (reference.getPaymentBehavior()) {
                    case EXCELLENT:
                        totalScore = totalScore.add(new BigDecimal("30"));
                        positiveReferences++;
                        break;
                    case GOOD:
                        totalScore = totalScore.add(new BigDecimal("20"));
                        positiveReferences++;
                        break;
                    case SATISFACTORY:
                        totalScore = totalScore.add(new BigDecimal("10"));
                        break;
                    case POOR:
                        totalScore = totalScore.subtract(new BigDecimal("20"));
                        negativeReferences++;
                        break;
                    case VERY_POOR:
                        totalScore = totalScore.subtract(new BigDecimal("40"));
                        negativeReferences++;
                        break;
                    case DEFAULTED:
                        totalScore = totalScore.subtract(new BigDecimal("60"));
                        negativeReferences++;
                        break;
                }

                // Recommendation level scoring
                if (reference.getRecommendationLevel() != null) {
                    switch (reference.getRecommendationLevel()) {
                        case HIGHLY_RECOMMENDED:
                            totalScore = totalScore.add(new BigDecimal("25"));
                            break;
                        case RECOMMENDED:
                            totalScore = totalScore.add(new BigDecimal("15"));
                            break;
                        case CONDITIONALLY_RECOMMENDED:
                            totalScore = totalScore.add(new BigDecimal("5"));
                            break;
                        case NOT_RECOMMENDED:
                            totalScore = totalScore.subtract(new BigDecimal("30"));
                            break;
                    }
                }

                // Dispute penalty
                if (reference.getHasDisputes() != null && reference.getHasDisputes()) {
                    totalScore = totalScore.subtract(new BigDecimal("15"));
                }

                // Business value bonus (for significant relationships)
                if (reference.getTotalBusinessValue() != null &&
                        reference.getTotalBusinessValue().compareTo(new BigDecimal("1000000")) > 0) {
                    totalScore = totalScore.add(new BigDecimal("10"));
                }
            }

            // Quality adjustments
            int totalReferences = tradeReferences.size();
            if (totalReferences >= 5) {
                totalScore = totalScore.add(new BigDecimal("20")); // Multiple references bonus
            }

            if (verifiedReferences > 0) {
                BigDecimal verificationRate = new BigDecimal(verifiedReferences)
                        .divide(new BigDecimal(totalReferences), 4, RoundingMode.HALF_UP);
                if (verificationRate.compareTo(new BigDecimal("0.8")) >= 0) {
                    totalScore = totalScore.add(new BigDecimal("25")); // High verification rate
                }
            }

            // Average per reference
            BigDecimal averageScore = totalScore.divide(new BigDecimal(totalReferences), 2, RoundingMode.HALF_UP);

            // Cap the score
            if (averageScore.compareTo(new BigDecimal("80")) > 0) {
                averageScore = new BigDecimal("80");
            } else if (averageScore.compareTo(new BigDecimal("-50")) < 0) {
                averageScore = new BigDecimal("-50");
            }

            logger.debug("Trade reference score for business {}: {}", businessId, averageScore);
            return averageScore;

        } catch (Exception e) {
            logger.error("Error calculating trade reference score for business: {}", businessId, e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateBusinessProfileScore(Business business) {
        try {
            BigDecimal score = BigDecimal.ZERO;

            // Verification bonuses
            if (business.getGstinVerified() != null && business.getGstinVerified()) {
                score = score.add(new BigDecimal("20"));
            }

            if (business.getPanVerified() != null && business.getPanVerified()) {
                score = score.add(new BigDecimal("15"));
            }

            // Business type scoring
            if (business.getBusinessType() != null) {
                switch (business.getBusinessType()) {
                    case PUBLIC_LIMITED:
                        score = score.add(new BigDecimal("25"));
                        break;
                    case PRIVATE_LIMITED:
                        score = score.add(new BigDecimal("20"));
                        break;
                    case PARTNERSHIP:
                        score = score.add(new BigDecimal("15"));
                        break;
                    case LLP:
                        score = score.add(new BigDecimal("15"));
                        break;
                    case SOLE_PROPRIETORSHIP:
                        score = score.add(new BigDecimal("10"));
                        break;
                    case OTHER:  // This is correct - don't prefix with BusinessType
                        score = score.add(new BigDecimal("5"));
                        break;
                }
            }

            // Complete profile bonus
            boolean hasCompleteProfile =
                    business.getBusinessName() != null &&
                            business.getGstin() != null &&
                            business.getPan() != null &&
                            business.getAddress() != null &&
                            business.getPhoneNumber() != null &&
                            business.getEmail() != null;

            if (hasCompleteProfile) {
                score = score.add(new BigDecimal("15"));
            }

            // Industry category considerations (some industries are lower risk)
            if (business.getIndustryCategory() != null) {
                switch (business.getIndustryCategory()) {
                    case TECHNOLOGY:
                    case HEALTHCARE:
                    case EDUCATION:  // This is correct - don't prefix with IndustryCategory
                        score = score.add(new BigDecimal("10"));
                        break;
                    case MANUFACTURING:
                    case RETAIL:
                        score = score.add(new BigDecimal("5"));
                        break;
                    default:
                        // No additional score for other categories
                        break;
                }
            }

            logger.debug("Business profile score for {}: {}", business.getBusinessName(), score);
            return score;

        } catch (Exception e) {
            logger.error("Error calculating business profile score for business: {}", business.getId(), e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateCreditUtilizationScore(Long businessId) {
        try {
            // This would need credit limit and utilization data
            // For now, return a neutral score since we don't have this data yet
            // TODO: Implement when credit utilization tracking is added

            List<TradeReference> references = tradeReferenceRepository.findByBusinessIdAndIsActiveTrue(businessId);

            BigDecimal totalCreditLimit = BigDecimal.ZERO;
            int referencesWithCredit = 0;

            for (TradeReference reference : references) {
                if (reference.getCreditLimitProvided() != null &&
                        reference.getCreditLimitProvided().compareTo(BigDecimal.ZERO) > 0) {
                    totalCreditLimit = totalCreditLimit.add(reference.getCreditLimitProvided());
                    referencesWithCredit++;
                }
            }

            if (referencesWithCredit > 0) {
                // Having established credit limits is positive
                BigDecimal avgCreditLimit = totalCreditLimit.divide(new BigDecimal(referencesWithCredit), 2, RoundingMode.HALF_UP);

                if (avgCreditLimit.compareTo(new BigDecimal("1000000")) > 0) {
                    return new BigDecimal("20"); // High credit limits available
                } else if (avgCreditLimit.compareTo(new BigDecimal("500000")) > 0) {
                    return new BigDecimal("15");
                } else if (avgCreditLimit.compareTo(new BigDecimal("100000")) > 0) {
                    return new BigDecimal("10");
                } else {
                    return new BigDecimal("5");
                }
            }

            return BigDecimal.ZERO;

        } catch (Exception e) {
            logger.error("Error calculating credit utilization score for business: {}", businessId, e);
            return BigDecimal.ZERO;
        }
    }

   private BigDecimal calculateBusinessAgeScore(Business business) {
        try {
            if (business.getRegistrationDate() == null) {
                return new BigDecimal("-10"); // Penalty for unknown registration date
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime registrationDateTime;

            // Safely handle the object regardless of its type
            Object regDateObj = business.getRegistrationDate();

            if (regDateObj instanceof LocalDate) {
                // If it's a LocalDate, convert to LocalDateTime
                registrationDateTime = ((LocalDate) regDateObj).atStartOfDay();
            } else if (regDateObj instanceof LocalDateTime) {
                // If it's already a LocalDateTime, use directly
                registrationDateTime = (LocalDateTime) regDateObj;
            } else {
                // Log unexpected type and return default value
                logger.warn("Registration date has unexpected type: {}",
                    regDateObj != null ? regDateObj.getClass().getName() : "null");
                return BigDecimal.ZERO;
            }

            long daysBetween = java.time.Duration.between(registrationDateTime, now).toDays();
            long monthsBetween = daysBetween / 30;

            if (monthsBetween >= 60) { // 5+ years
                return new BigDecimal("30");
            } else if (monthsBetween >= 36) { // 3+ years
                return new BigDecimal("20");
            } else if (monthsBetween >= 24) { // 2+ years
                return new BigDecimal("15");
            } else if (monthsBetween >= 12) { // 1+ year
                return new BigDecimal("10");
            } else if (monthsBetween >= 6) { // 6+ months
                return new BigDecimal("5");
            } else {
                return new BigDecimal("-5"); // New business penalty
            }
        } catch (Exception e) {
            logger.error("Error calculating business age score for business: {}", business.getId(), e);
            return BigDecimal.ZERO;
        }
    }

    public String getCreditScoreGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("750")) >= 0) {
            return "A+";
        } else if (score.compareTo(new BigDecimal("700")) >= 0) {
            return "A";
        } else if (score.compareTo(new BigDecimal("650")) >= 0) {
            return "B+";
        } else if (score.compareTo(new BigDecimal("600")) >= 0) {
            return "B";
        } else if (score.compareTo(new BigDecimal("550")) >= 0) {
            return "C+";
        } else if (score.compareTo(new BigDecimal("500")) >= 0) {
            return "C";
        } else if (score.compareTo(new BigDecimal("450")) >= 0) {
            return "D";
        } else {
            return "F";
        }
    }

    public String getRiskCategory(BigDecimal score) {
        if (score.compareTo(new BigDecimal("700")) >= 0) {
            return "LOW";
        } else if (score.compareTo(new BigDecimal("600")) >= 0) {
            return "MODERATE";
        } else if (score.compareTo(new BigDecimal("500")) >= 0) {
            return "MEDIUM";
        } else if (score.compareTo(new BigDecimal("400")) >= 0) {
            return "HIGH";
        } else {
            return "VERY_HIGH";
        }
    }

    public BigDecimal getRecommendedCreditLimit(BigDecimal score, Long businessId) {
        try {
            // Base credit limit based on score
            BigDecimal baseCreditLimit;

            if (score.compareTo(new BigDecimal("750")) >= 0) {
                baseCreditLimit = new BigDecimal("5000000"); // 50 Lakh
            } else if (score.compareTo(new BigDecimal("700")) >= 0) {
                baseCreditLimit = new BigDecimal("2500000"); // 25 Lakh
            } else if (score.compareTo(new BigDecimal("650")) >= 0) {
                baseCreditLimit = new BigDecimal("1500000"); // 15 Lakh
            } else if (score.compareTo(new BigDecimal("600")) >= 0) {
                baseCreditLimit = new BigDecimal("1000000"); // 10 Lakh
            } else if (score.compareTo(new BigDecimal("550")) >= 0) {
                baseCreditLimit = new BigDecimal("500000"); // 5 Lakh
            } else if (score.compareTo(new BigDecimal("500")) >= 0) {
                baseCreditLimit = new BigDecimal("250000"); // 2.5 Lakh
            } else if (score.compareTo(new BigDecimal("450")) >= 0) {
                baseCreditLimit = new BigDecimal("100000"); // 1 Lakh
            } else {
                baseCreditLimit = new BigDecimal("50000"); // 50 Thousand
            }

            // Adjust based on business factors
            List<TradeReference> references = tradeReferenceRepository.findByBusinessIdAndIsActiveTrue(businessId);

            if (!references.isEmpty()) {
                // Calculate average business value from references
                BigDecimal totalBusinessValue = BigDecimal.ZERO;
                int validReferences = 0;

                for (TradeReference reference : references) {
                    if (reference.getTotalBusinessValue() != null &&
                            reference.getTotalBusinessValue().compareTo(BigDecimal.ZERO) > 0) {
                        totalBusinessValue = totalBusinessValue.add(reference.getTotalBusinessValue());
                        validReferences++;
                    }
                }

                if (validReferences > 0) {
                    BigDecimal avgBusinessValue = totalBusinessValue.divide(new BigDecimal(validReferences), 2, RoundingMode.HALF_UP);

                    // Adjust credit limit based on business value (but cap the adjustment)
                    BigDecimal valueMultiplier = avgBusinessValue.divide(new BigDecimal("1000000"), 4, RoundingMode.HALF_UP);
                    if (valueMultiplier.compareTo(new BigDecimal("2.0")) > 0) {
                        valueMultiplier = new BigDecimal("2.0"); // Cap at 2x
                    } else if (valueMultiplier.compareTo(new BigDecimal("0.5")) < 0) {
                        valueMultiplier = new BigDecimal("0.5"); // Floor at 0.5x
                    }

                    baseCreditLimit = baseCreditLimit.multiply(valueMultiplier);
                }
            }

            return baseCreditLimit.setScale(0, RoundingMode.HALF_UP);

        } catch (Exception e) {
            logger.error("Error calculating recommended credit limit for business: {}", businessId, e);
            return new BigDecimal("100000"); // Default 1 Lakh
        }
    }
}