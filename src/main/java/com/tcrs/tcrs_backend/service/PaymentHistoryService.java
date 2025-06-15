package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.payment.PaymentAnalyticsResponse;
import com.tcrs.tcrs_backend.dto.payment.PaymentHistoryRequest;
import com.tcrs.tcrs_backend.dto.payment.PaymentHistoryResponse;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentHistoryService.class);

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    public PaymentHistoryResponse addPaymentHistory(PaymentHistoryRequest request) {
        logger.info("Adding payment history for business ID: {}", request.getBusinessId());

        // Get current user
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User reportedBy = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Get business
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        // Check if transaction reference already exists
        if (paymentHistoryRepository.findByTransactionReferenceAndIsActiveTrue(request.getTransactionReference()).isPresent()) {
            throw new BadRequestException("Payment history with this transaction reference already exists");
        }

        // Create payment history
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setBusiness(business);
        paymentHistory.setReportedBy(reportedBy);
        paymentHistory.setTransactionReference(request.getTransactionReference());
        paymentHistory.setInvoiceNumber(request.getInvoiceNumber());
        paymentHistory.setTransactionAmount(request.getTransactionAmount());
        paymentHistory.setDueDate(request.getDueDate());
        paymentHistory.setPaymentDate(request.getPaymentDate());
        paymentHistory.setPaymentStatus(request.getPaymentStatus());
        paymentHistory.setTransactionType(request.getTransactionType());
        paymentHistory.setPenaltyAmount(request.getPenaltyAmount());
        paymentHistory.setSettledAmount(request.getSettledAmount());
        paymentHistory.setPaymentMethod(request.getPaymentMethod());
        paymentHistory.setPaymentTerms(request.getPaymentTerms());
        paymentHistory.setTradeRelationship(request.getTradeRelationship());
        paymentHistory.setPaymentRating(request.getPaymentRating());
        paymentHistory.setComments(request.getComments());
        paymentHistory.setDisputeStatus(request.getDisputeStatus() != null ? request.getDisputeStatus() : DisputeStatus.NO_DISPUTE);
        paymentHistory.setDisputeReason(request.getDisputeReason());
        paymentHistory.setVerificationStatus(VerificationStatus.PENDING);

        // Calculate days overdue if payment is made and was late
        if (request.getPaymentDate() != null && request.getDueDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(request.getDueDate(), request.getPaymentDate());
            if (daysBetween > 0) {
                paymentHistory.setDaysOverdue((int) daysBetween);
            }
        } else if (request.getDaysOverdue() != null) {
            paymentHistory.setDaysOverdue(request.getDaysOverdue());
        }

        // Auto-verify if certain conditions are met
        if (shouldAutoVerify(paymentHistory, reportedBy)) {
            paymentHistory.setVerificationStatus(VerificationStatus.AUTO_VERIFIED);
            paymentHistory.setVerifiedBy("System Auto-Verification");
            paymentHistory.setVerifiedDate(LocalDateTime.now());
        }

        PaymentHistory savedPaymentHistory = paymentHistoryRepository.save(paymentHistory);

        logger.info("Payment history added successfully with ID: {}", savedPaymentHistory.getId());

        return convertToResponse(savedPaymentHistory);
    }

    public PaymentHistoryResponse updatePaymentHistory(Long paymentHistoryId, PaymentHistoryRequest request) {
        logger.info("Updating payment history with ID: {}", paymentHistoryId);

        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment history not found with ID: " + paymentHistoryId));

        // Check if current user can update this record
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!paymentHistory.getReportedBy().getId().equals(currentUserPrincipal.getId()) &&
                !currentUserPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only update payment history records that you reported");
        }

        // Update fields
        paymentHistory.setInvoiceNumber(request.getInvoiceNumber());
        paymentHistory.setTransactionAmount(request.getTransactionAmount());
        paymentHistory.setDueDate(request.getDueDate());
        paymentHistory.setPaymentDate(request.getPaymentDate());
        paymentHistory.setPaymentStatus(request.getPaymentStatus());
        paymentHistory.setTransactionType(request.getTransactionType());
        paymentHistory.setPenaltyAmount(request.getPenaltyAmount());
        paymentHistory.setSettledAmount(request.getSettledAmount());
        paymentHistory.setPaymentMethod(request.getPaymentMethod());
        paymentHistory.setPaymentTerms(request.getPaymentTerms());
        paymentHistory.setTradeRelationship(request.getTradeRelationship());
        paymentHistory.setPaymentRating(request.getPaymentRating());
        paymentHistory.setComments(request.getComments());
        paymentHistory.setDisputeStatus(request.getDisputeStatus());
        paymentHistory.setDisputeReason(request.getDisputeReason());

        // Recalculate days overdue
        if (request.getPaymentDate() != null && request.getDueDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(request.getDueDate(), request.getPaymentDate());
            if (daysBetween > 0) {
                paymentHistory.setDaysOverdue((int) daysBetween);
            } else {
                paymentHistory.setDaysOverdue(0);
            }
        } else if (request.getDaysOverdue() != null) {
            paymentHistory.setDaysOverdue(request.getDaysOverdue());
        }

        // Reset verification if significant changes made
        if (hasSignificantChanges(paymentHistory, request)) {
            paymentHistory.setVerificationStatus(VerificationStatus.PENDING);
            paymentHistory.setVerifiedBy(null);
            paymentHistory.setVerifiedDate(null);
        }

        PaymentHistory updatedPaymentHistory = paymentHistoryRepository.save(paymentHistory);

        logger.info("Payment history updated successfully");

        return convertToResponse(updatedPaymentHistory);
    }

    public PaymentHistoryResponse getPaymentHistory(Long paymentHistoryId) {
        logger.info("Retrieving payment history with ID: {}", paymentHistoryId);

        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment history not found with ID: " + paymentHistoryId));

        return convertToResponse(paymentHistory);
    }

    public List<PaymentHistoryResponse> getBusinessPaymentHistory(Long businessId) {
        logger.info("Retrieving payment history for business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        List<PaymentHistory> paymentHistories = paymentHistoryRepository
                .findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(business);

        return paymentHistories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<PaymentHistoryResponse> getBusinessPaymentHistoryPaginated(Long businessId, int page, int size) {
        logger.info("Retrieving paginated payment history for business ID: {} - page: {}, size: {}", businessId, page, size);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentHistory> paymentHistoriesPage = paymentHistoryRepository
                .findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(business, pageable);

        return paymentHistoriesPage.map(this::convertToResponse);
    }

    public Page<PaymentHistoryResponse> getUserPaymentHistories(int page, int size) {
        logger.info("Retrieving user payment histories - page: {}, size: {}", page, size);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentHistory> paymentHistoriesPage = paymentHistoryRepository
                .findByReportedByAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);

        return paymentHistoriesPage.map(this::convertToResponse);
    }

    public PaymentAnalyticsResponse getPaymentAnalytics(Long businessId) {
        logger.info("Generating payment analytics for business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        PaymentAnalyticsResponse analytics = new PaymentAnalyticsResponse();
        analytics.setBusinessId(business.getId());
        analytics.setBusinessName(business.getBusinessName());

        // Basic Statistics
        Long totalTransactions = paymentHistoryRepository.countTotalTransactionsByBusiness(business);
        BigDecimal totalValue = paymentHistoryRepository.sumTotalTransactionValueByBusiness(business);
        Long onTimePayments = paymentHistoryRepository.countOnTimePaymentsByBusiness(business);
        Long overdueTransactions = paymentHistoryRepository.countOverdueTransactionsByBusiness(business);
        BigDecimal overdueAmount = paymentHistoryRepository.sumOverdueAmountByBusiness(business);
        Double averageDelay = paymentHistoryRepository.averagePaymentDelayByBusiness(business);
        Double averageRating = paymentHistoryRepository.averagePaymentRatingByBusiness(business);

        analytics.setTotalTransactions(totalTransactions != null ? totalTransactions.intValue() : 0);
        analytics.setTotalTransactionValue(totalValue != null ? totalValue : BigDecimal.ZERO);
        analytics.setTotalOverdueTransactions(overdueTransactions != null ? overdueTransactions.intValue() : 0);
        analytics.setTotalOverdueAmount(overdueAmount != null ? overdueAmount : BigDecimal.ZERO);
        analytics.setAveragePaymentDelay(averageDelay != null ? averageDelay : 0.0);

        // Calculate percentages
        if (totalTransactions != null && totalTransactions > 0) {
            double onTimePercentage = (onTimePayments != null ? onTimePayments.doubleValue() : 0.0) / totalTransactions * 100;
            double overduePercentage = (overdueTransactions != null ? overdueTransactions.doubleValue() : 0.0) / totalTransactions * 100;

            analytics.setOnTimePaymentPercentage(BigDecimal.valueOf(onTimePercentage).setScale(2, RoundingMode.HALF_UP).doubleValue());
            analytics.setOverduePaymentPercentage(BigDecimal.valueOf(overduePercentage).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            analytics.setOnTimePaymentPercentage(0.0);
            analytics.setOverduePaymentPercentage(0.0);
        }

        // Payment Behavior Scores
        calculatePaymentBehaviorScores(analytics, business);

        // Distribution Analysis
        analytics.setPaymentStatusDistribution(getPaymentStatusDistribution(business));
        analytics.setPaymentAmountByStatus(getPaymentAmountByStatus(business));

        // Relationship Analysis
        analytics.setRelationshipTypeBreakdown(getRelationshipTypeBreakdown(business));
        analytics.setAverageRatingByRelationship(getAverageRatingByRelationship(business));

        // Risk Indicators
        BigDecimal largestOverdue = paymentHistoryRepository.findLargestOverdueAmount(business);
        Integer longestDelay = paymentHistoryRepository.findLongestPaymentDelay(business);
        Long activeDisputes = paymentHistoryRepository.countActiveDisputes(business);

        analytics.setLargestOverdueAmount(largestOverdue != null ? largestOverdue : BigDecimal.ZERO);
        analytics.setLongestPaymentDelay(longestDelay != null ? longestDelay : 0);
        analytics.setTotalDisputes(activeDisputes != null ? activeDisputes.intValue() : 0);

        // Monthly Trends
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Object[]> monthlyTrends = paymentHistoryRepository.getMonthlyPaymentTrends(business, sixMonthsAgo);
        analytics.setMonthlyPaymentTrends(processMonthlyTrends(monthlyTrends));
        analytics.setMonthlyTransactionCounts(processMonthlyTransactionCounts(monthlyTrends));

        logger.info("Payment analytics generated successfully for business: {}", business.getBusinessName());

        return analytics;
    }

    public void verifyPaymentHistory(Long paymentHistoryId, boolean approve, String reason) {
        logger.info("Verifying payment history ID: {} - Approved: {}", paymentHistoryId, approve);

        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment history not found with ID: " + paymentHistoryId));

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (approve) {
            paymentHistory.setVerificationStatus(VerificationStatus.VERIFIED);
        } else {
            paymentHistory.setVerificationStatus(VerificationStatus.REJECTED);
            paymentHistory.setComments(paymentHistory.getComments() + "\nRejection Reason: " + reason);
        }

        paymentHistory.setVerifiedBy(currentUserPrincipal.getUsername());
        paymentHistory.setVerifiedDate(LocalDateTime.now());

        paymentHistoryRepository.save(paymentHistory);

        logger.info("Payment history verification completed");
    }

    public void deletePaymentHistory(Long paymentHistoryId) {
        logger.info("Deleting payment history with ID: {}", paymentHistoryId);

        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment history not found with ID: " + paymentHistoryId));

        // Check if current user can delete this record
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!paymentHistory.getReportedBy().getId().equals(currentUserPrincipal.getId()) &&
                !currentUserPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only delete payment history records that you reported");
        }

        // Soft delete
        paymentHistory.setIsActive(false);
        paymentHistoryRepository.save(paymentHistory);

        logger.info("Payment history deleted successfully");
    }

    // Helper Methods

    private boolean shouldAutoVerify(PaymentHistory paymentHistory, User reportedBy) {
        // Auto-verify if:
        // 1. Reporter has good track record
        // 2. Transaction amount is below threshold
        // 3. Payment status is straightforward (PAID, PENDING)

        BigDecimal autoVerifyThreshold = new BigDecimal("100000"); // ₹1 Lakh

        return paymentHistory.getTransactionAmount().compareTo(autoVerifyThreshold) <= 0 &&
                (paymentHistory.getPaymentStatus() == PaymentStatus.PAID ||
                        paymentHistory.getPaymentStatus() == PaymentStatus.PENDING) &&
                paymentHistory.getDisputeStatus() == DisputeStatus.NO_DISPUTE;
    }

    private boolean hasSignificantChanges(PaymentHistory existing, PaymentHistoryRequest request) {
        // Check if changes are significant enough to require re-verification
        return !existing.getTransactionAmount().equals(request.getTransactionAmount()) ||
                !existing.getPaymentStatus().equals(request.getPaymentStatus()) ||
                !existing.getDueDate().equals(request.getDueDate()) ||
                (existing.getPaymentDate() != null && !existing.getPaymentDate().equals(request.getPaymentDate()));
    }

    private void calculatePaymentBehaviorScores(PaymentAnalyticsResponse analytics, Business business) {
        // Payment Reliability Score (0-100)
        double reliabilityScore = 50.0; // Base score

        if (analytics.getOnTimePaymentPercentage() != null) {
            reliabilityScore = analytics.getOnTimePaymentPercentage();
        }

        // Payment Speed Score (0-100)
        double speedScore = 100.0;
        if (analytics.getAveragePaymentDelay() != null && analytics.getAveragePaymentDelay() > 0) {
            speedScore = Math.max(0, 100 - (analytics.getAveragePaymentDelay() * 2)); // Penalize 2 points per day delay
        }

        // Dispute Frequency Score (0-100)
        double disputeScore = 100.0;
        if (analytics.getTotalTransactions() != null && analytics.getTotalTransactions() > 0) {
            double disputeRate = (analytics.getTotalDisputes() != null ? analytics.getTotalDisputes().doubleValue() : 0.0)
                    / analytics.getTotalTransactions() * 100;
            disputeScore = Math.max(0, 100 - (disputeRate * 10)); // Penalize heavily for disputes
        }

        // Overall Payment Score (weighted average)
        double overallScore = (reliabilityScore * 0.4) + (speedScore * 0.3) + (disputeScore * 0.3);

        analytics.setPaymentReliabilityScore(BigDecimal.valueOf(reliabilityScore).setScale(2, RoundingMode.HALF_UP).doubleValue());
        analytics.setPaymentSpeedScore(BigDecimal.valueOf(speedScore).setScale(2, RoundingMode.HALF_UP).doubleValue());
        analytics.setDisputeFrequencyScore(BigDecimal.valueOf(disputeScore).setScale(2, RoundingMode.HALF_UP).doubleValue());
        analytics.setOverallPaymentScore(BigDecimal.valueOf(overallScore).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private Map<String, Integer> getPaymentStatusDistribution(Business business) {
        List<Object[]> results = paymentHistoryRepository.getPaymentStatusDistribution(business);
        Map<String, Integer> distribution = new HashMap<>();

        for (Object[] result : results) {
            PaymentStatus status = (PaymentStatus) result[0];
            Long count = (Long) result[1];
            distribution.put(status.toString(), count.intValue());
        }

        return distribution;
    }

    private Map<String, BigDecimal> getPaymentAmountByStatus(Business business) {
        List<Object[]> results = paymentHistoryRepository.getPaymentAmountByStatus(business);
        Map<String, BigDecimal> amounts = new HashMap<>();

        for (Object[] result : results) {
            PaymentStatus status = (PaymentStatus) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            amounts.put(status.toString(), amount != null ? amount : BigDecimal.ZERO);
        }

        return amounts;
    }

    private Map<String, Integer> getRelationshipTypeBreakdown(Business business) {
        List<Object[]> results = paymentHistoryRepository.getRelationshipTypeBreakdown(business);
        Map<String, Integer> breakdown = new HashMap<>();

        for (Object[] result : results) {
            String relationship = (String) result[0];
            Long count = (Long) result[1];
            breakdown.put(relationship, count.intValue());
        }

        return breakdown;
    }

    private Map<String, Double> getAverageRatingByRelationship(Business business) {
        List<Object[]> results = paymentHistoryRepository.getAverageRatingByRelationship(business);
        Map<String, Double> ratings = new HashMap<>();

        for (Object[] result : results) {
            String relationship = (String) result[0];
            Double avgRating = (Double) result[1];
            ratings.put(relationship, avgRating != null ? avgRating : 0.0);
        }

        return ratings;
    }

    private Map<String, Double> processMonthlyTrends(List<Object[]> trends) {
        Map<String, Double> monthlyTrends = new HashMap<>();

        for (Object[] trend : trends) {
            Integer year = (Integer) trend[0];
            Integer month = (Integer) trend[1];
            Long count = (Long) trend[2];
            Double avgDelay = (Double) trend[3];

            String monthKey = year + "-" + String.format("%02d", month);
            monthlyTrends.put(monthKey, avgDelay != null ? avgDelay : 0.0);
        }

        return monthlyTrends;
    }

    private Map<String, Integer> processMonthlyTransactionCounts(List<Object[]> trends) {
        Map<String, Integer> monthlyCounts = new HashMap<>();

        for (Object[] trend : trends) {
            Integer year = (Integer) trend[0];
            Integer month = (Integer) trend[1];
            Long count = (Long) trend[2];

            String monthKey = year + "-" + String.format("%02d", month);
            monthlyCounts.put(monthKey, count != null ? count.intValue() : 0);
        }

        return monthlyCounts;
    }

    private PaymentHistoryResponse convertToResponse(PaymentHistory paymentHistory) {
        PaymentHistoryResponse response = new PaymentHistoryResponse();

        response.setId(paymentHistory.getId());
        response.setBusinessId(paymentHistory.getBusiness().getId());
        response.setBusinessName(paymentHistory.getBusiness().getBusinessName());
        response.setBusinessGstin(paymentHistory.getBusiness().getGstin());
        response.setTransactionReference(paymentHistory.getTransactionReference());
        response.setInvoiceNumber(paymentHistory.getInvoiceNumber());
        response.setTransactionAmount(paymentHistory.getTransactionAmount());
        response.setDueDate(paymentHistory.getDueDate());
        response.setPaymentDate(paymentHistory.getPaymentDate());
        response.setPaymentStatus(paymentHistory.getPaymentStatus());
        response.setTransactionType(paymentHistory.getTransactionType());
        response.setDaysOverdue(paymentHistory.getDaysOverdue());
        response.setPenaltyAmount(paymentHistory.getPenaltyAmount());
        response.setSettledAmount(paymentHistory.getSettledAmount());
        response.setPaymentMethod(paymentHistory.getPaymentMethod());
        response.setPaymentTerms(paymentHistory.getPaymentTerms());
        response.setTradeRelationship(paymentHistory.getTradeRelationship());
        response.setPaymentRating(paymentHistory.getPaymentRating());
        response.setComments(paymentHistory.getComments());
        response.setVerificationStatus(paymentHistory.getVerificationStatus());
        response.setVerifiedBy(paymentHistory.getVerifiedBy());
        response.setVerifiedDate(paymentHistory.getVerifiedDate());
        response.setDisputeStatus(paymentHistory.getDisputeStatus());
        response.setDisputeReason(paymentHistory.getDisputeReason());
        response.setReportedByName(paymentHistory.getReportedBy().getFirstName() + " " +
                paymentHistory.getReportedBy().getLastName());
        response.setIsActive(paymentHistory.getIsActive());
        response.setCreatedAt(paymentHistory.getCreatedAt());
        response.setUpdatedAt(paymentHistory.getUpdatedAt());

        return response;
    }
}