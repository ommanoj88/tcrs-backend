package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.reference.TradeReferenceAnalyticsResponse;
import com.tcrs.tcrs_backend.dto.reference.TradeReferenceRequest;
import com.tcrs.tcrs_backend.dto.reference.TradeReferenceResponse;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
import com.tcrs.tcrs_backend.repository.TradeReferenceRepository;
import com.tcrs.tcrs_backend.repository.UserRepository;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TradeReferenceService {

    private static final Logger logger = LoggerFactory.getLogger(TradeReferenceService.class);

    @Autowired
    private TradeReferenceRepository tradeReferenceRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public TradeReferenceResponse addTradeReference(TradeReferenceRequest request) {
        logger.info("Adding trade reference for business ID: {}", request.getBusinessId());

        // Get current user
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User addedBy = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Get business
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        // Check for duplicate references
        validateDuplicateReference(business, request.getEmail(), request.getPhone(),
                request.getCompanyGstin(), null);

        // Get reference provider if specified
        Business referenceProvider = null;
        if (request.getReferenceProviderId() != null) {
            referenceProvider = businessRepository.findById(request.getReferenceProviderId())
                    .orElse(null);
        }

        // Create trade reference
        TradeReference tradeReference = new TradeReference();
        tradeReference.setBusiness(business);
        tradeReference.setReferenceProvider(referenceProvider);
        tradeReference.setAddedBy(addedBy);
        tradeReference.setReferenceNumber(generateReferenceNumber());
        tradeReference.setCompanyName(request.getCompanyName());
        tradeReference.setContactPerson(request.getContactPerson());
        tradeReference.setDesignation(request.getDesignation());
        tradeReference.setEmail(request.getEmail());
        tradeReference.setPhone(request.getPhone());
        tradeReference.setCompanyAddress(request.getCompanyAddress());
        tradeReference.setCompanyGstin(request.getCompanyGstin());
        tradeReference.setReferenceType(request.getReferenceType());
        tradeReference.setRelationshipType(request.getRelationshipType());
        tradeReference.setRelationshipDurationMonths(request.getRelationshipDurationMonths());
        tradeReference.setRelationshipStartDate(request.getRelationshipStartDate());
        tradeReference.setRelationshipEndDate(request.getRelationshipEndDate());
        tradeReference.setAverageMonthlyBusiness(request.getAverageMonthlyBusiness());
        tradeReference.setTotalBusinessValue(request.getTotalBusinessValue());
        tradeReference.setCreditLimitProvided(request.getCreditLimitProvided());
        tradeReference.setPaymentTerms(request.getPaymentTerms());
        tradeReference.setPaymentBehavior(request.getPaymentBehavior());
        tradeReference.setPaymentRating(request.getPaymentRating());
        tradeReference.setOverallRating(request.getOverallRating());
        tradeReference.setHasDisputes(request.getHasDisputes() != null ? request.getHasDisputes() : false);
        tradeReference.setDisputeDetails(request.getDisputeDetails());
        tradeReference.setReferenceComments(request.getReferenceComments());
        tradeReference.setRecommendationLevel(request.getRecommendationLevel());
        tradeReference.setIsConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : false);
        tradeReference.setVerificationStatus(ReferenceVerificationStatus.PENDING);

        // Calculate relationship duration if dates are provided
        if (request.getRelationshipStartDate() != null && request.getRelationshipEndDate() != null) {
            Period period = Period.between(request.getRelationshipStartDate(), request.getRelationshipEndDate());
            tradeReference.setRelationshipDurationMonths(period.toTotalMonths() > 0 ? (int) period.toTotalMonths() : 0);
        } else if (request.getRelationshipStartDate() != null) {
            Period period = Period.between(request.getRelationshipStartDate(), LocalDate.now());
            tradeReference.setRelationshipDurationMonths(period.toTotalMonths() > 0 ? (int) period.toTotalMonths() : 0);
        }

        TradeReference savedReference = tradeReferenceRepository.save(tradeReference);

        // Send verification email asynchronously
        sendVerificationEmailAsync(savedReference);

        logger.info("Trade reference added successfully with number: {}", savedReference.getReferenceNumber());

        return convertToResponse(savedReference);
    }

    public TradeReferenceResponse updateTradeReference(Long referenceId, TradeReferenceRequest request) {
        logger.info("Updating trade reference with ID: {}", referenceId);

        TradeReference tradeReference = tradeReferenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade reference not found with ID: " + referenceId));

        // Check if current user can update this record
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!tradeReference.getAddedBy().getId().equals(currentUserPrincipal.getId()) &&
                !currentUserPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only update trade references that you added");
        }

        // Check for duplicate references (excluding current reference)
        validateDuplicateReference(tradeReference.getBusiness(), request.getEmail(),
                request.getPhone(), request.getCompanyGstin(), referenceId);

        // Update fields
        tradeReference.setCompanyName(request.getCompanyName());
        tradeReference.setContactPerson(request.getContactPerson());
        tradeReference.setDesignation(request.getDesignation());
        tradeReference.setEmail(request.getEmail());
        tradeReference.setPhone(request.getPhone());
        tradeReference.setCompanyAddress(request.getCompanyAddress());
        tradeReference.setCompanyGstin(request.getCompanyGstin());
        tradeReference.setReferenceType(request.getReferenceType());
        tradeReference.setRelationshipType(request.getRelationshipType());
        tradeReference.setRelationshipDurationMonths(request.getRelationshipDurationMonths());
        tradeReference.setRelationshipStartDate(request.getRelationshipStartDate());
        tradeReference.setRelationshipEndDate(request.getRelationshipEndDate());
        tradeReference.setAverageMonthlyBusiness(request.getAverageMonthlyBusiness());
        tradeReference.setTotalBusinessValue(request.getTotalBusinessValue());
        tradeReference.setCreditLimitProvided(request.getCreditLimitProvided());
        tradeReference.setPaymentTerms(request.getPaymentTerms());
        tradeReference.setPaymentBehavior(request.getPaymentBehavior());
        tradeReference.setPaymentRating(request.getPaymentRating());
        tradeReference.setOverallRating(request.getOverallRating());
        tradeReference.setHasDisputes(request.getHasDisputes());
        tradeReference.setDisputeDetails(request.getDisputeDetails());
        tradeReference.setReferenceComments(request.getReferenceComments());
        tradeReference.setRecommendationLevel(request.getRecommendationLevel());
        tradeReference.setIsConfidential(request.getIsConfidential());

        // Recalculate relationship duration
        if (request.getRelationshipStartDate() != null && request.getRelationshipEndDate() != null) {
            Period period = Period.between(request.getRelationshipStartDate(), request.getRelationshipEndDate());
            tradeReference.setRelationshipDurationMonths(period.toTotalMonths() > 0 ? (int) period.toTotalMonths() : 0);
        } else if (request.getRelationshipStartDate() != null) {
            Period period = Period.between(request.getRelationshipStartDate(), LocalDate.now());
            tradeReference.setRelationshipDurationMonths(period.toTotalMonths() > 0 ? (int) period.toTotalMonths() : 0);
        }

        // Reset verification if significant changes made
        if (hasSignificantChanges(tradeReference, request)) {
            tradeReference.setVerificationStatus(ReferenceVerificationStatus.PENDING);
            tradeReference.setVerifiedBy(null);
            tradeReference.setVerifiedDate(null);
            tradeReference.setVerificationNotes(null);
            tradeReference.setReferenceResponse(null);

            // Send verification email for updated reference
            sendVerificationEmailAsync(tradeReference);
        }

        TradeReference updatedReference = tradeReferenceRepository.save(tradeReference);

        logger.info("Trade reference updated successfully");

        return convertToResponse(updatedReference);
    }

    public TradeReferenceResponse getTradeReference(Long referenceId) {
        logger.info("Retrieving trade reference with ID: {}", referenceId);

        TradeReference tradeReference = tradeReferenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade reference not found with ID: " + referenceId));

        return convertToResponse(tradeReference);
    }

    public List<TradeReferenceResponse> getBusinessTradeReferences(Long businessId) {
        logger.info("Retrieving trade references for business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        List<TradeReference> references = tradeReferenceRepository
                .findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(business);

        return references.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<TradeReferenceResponse> getBusinessTradeReferencesPaginated(Long businessId, int page, int size) {
        logger.info("Retrieving paginated trade references for business ID: {} - page: {}, size: {}", businessId, page, size);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TradeReference> referencesPage = tradeReferenceRepository
                .findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(business, pageable);

        return referencesPage.map(this::convertToResponse);
    }

    public Page<TradeReferenceResponse> getUserTradeReferences(int page, int size) {
        logger.info("Retrieving user trade references - page: {}, size: {}", page, size);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TradeReference> referencesPage = tradeReferenceRepository
                .findByAddedByAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);

        return referencesPage.map(this::convertToResponse);
    }

    public Page<TradeReferenceResponse> getPendingVerifications(int page, int size) {
        logger.info("Retrieving pending verifications - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<TradeReference> referencesPage = tradeReferenceRepository
                .findByVerificationStatusAndIsActiveTrueOrderByCreatedAtDesc(
                        ReferenceVerificationStatus.PENDING, pageable);

        return referencesPage.map(this::convertToResponse);
    }

    public TradeReferenceAnalyticsResponse getTradeReferenceAnalytics(Long businessId) {
        logger.info("Generating trade reference analytics for business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        TradeReferenceAnalyticsResponse analytics = new TradeReferenceAnalyticsResponse();
        analytics.setBusinessId(business.getId());
        analytics.setBusinessName(business.getBusinessName());

        // Basic Statistics
        Long totalReferences = tradeReferenceRepository.countTotalReferencesByBusiness(business);
        Long verifiedReferences = tradeReferenceRepository.countVerifiedReferencesByBusiness(business);
        Long positiveReferences = tradeReferenceRepository.countPositiveReferencesByBusiness(business);
        Long negativeReferences = tradeReferenceRepository.countNegativeReferencesByBusiness(business);
        Long confidentialReferences = tradeReferenceRepository.countConfidentialReferencesByBusiness(business);
        Long referencesWithDisputes = tradeReferenceRepository.countReferencesWithDisputesByBusiness(business);

        analytics.setTotalReferences(totalReferences != null ? totalReferences.intValue() : 0);
        analytics.setVerifiedReferences(verifiedReferences != null ? verifiedReferences.intValue() : 0);
        analytics.setPendingReferences(analytics.getTotalReferences() - analytics.getVerifiedReferences());
        analytics.setPositiveReferences(positiveReferences != null ? positiveReferences.intValue() : 0);
        analytics.setNegativeReferences(negativeReferences != null ? negativeReferences.intValue() : 0);
        analytics.setConfidentialReferences(confidentialReferences != null ? confidentialReferences.intValue() : 0);
        analytics.setReferencesWithDisputes(referencesWithDisputes != null ? referencesWithDisputes.intValue() : 0);

        // Rating Statistics
        Double avgPaymentRating = tradeReferenceRepository.averagePaymentRatingByBusiness(business);
        Double avgOverallRating = tradeReferenceRepository.averageOverallRatingByBusiness(business);

        analytics.setAveragePaymentRating(avgPaymentRating != null ? avgPaymentRating : 0.0);
        analytics.setAverageOverallRating(avgOverallRating != null ? avgOverallRating : 0.0);

        // Calculate rates
        if (analytics.getTotalReferences() > 0) {
            double verificationRate = (analytics.getVerifiedReferences().doubleValue() / analytics.getTotalReferences()) * 100;
            double positiveRate = (analytics.getPositiveReferences().doubleValue() / analytics.getTotalReferences()) * 100;
            double disputeRate = (analytics.getReferencesWithDisputes().doubleValue() / analytics.getTotalReferences()) * 100;

            analytics.setVerificationRate(BigDecimal.valueOf(verificationRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            analytics.setPositiveReferenceRate(BigDecimal.valueOf(positiveRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            analytics.setDisputeRate(BigDecimal.valueOf(disputeRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            analytics.setVerificationRate(0.0);
            analytics.setPositiveReferenceRate(0.0);
            analytics.setDisputeRate(0.0);
        }

        // Business Value Metrics
        BigDecimal totalBusinessValue = tradeReferenceRepository.sumTotalBusinessValueByBusiness(business);
        BigDecimal totalMonthlyBusiness = tradeReferenceRepository.sumAverageMonthlyBusinessByBusiness(business);

        analytics.setTotalBusinessValue(totalBusinessValue != null ? totalBusinessValue : BigDecimal.ZERO);
        analytics.setAverageMonthlyBusinessTotal(totalMonthlyBusiness != null ? totalMonthlyBusiness : BigDecimal.ZERO);

        if (analytics.getVerifiedReferences() > 0) {
            BigDecimal avgBusinessValue = analytics.getTotalBusinessValue()
                    .divide(new BigDecimal(analytics.getVerifiedReferences()), 2, RoundingMode.HALF_UP);
            analytics.setAverageBusinessValuePerReference(avgBusinessValue);
        } else {
            analytics.setAverageBusinessValuePerReference(BigDecimal.ZERO);
        }

        // Distribution Analysis
        analytics.setReferenceTypeDistribution(getReferenceTypeDistribution(business));
        analytics.setRelationshipTypeDistribution(getRelationshipTypeDistribution(business));
        analytics.setPaymentBehaviorDistribution(getPaymentBehaviorDistribution(business));
        analytics.setRecommendationLevelDistribution(getRecommendationLevelDistribution(business));

        // Advanced Metrics
        List<TradeReference> longTermRefs = tradeReferenceRepository.findLongTermReferences(business, 24);
        List<TradeReference> highValueRefs = tradeReferenceRepository.findHighValueReferences(business, new BigDecimal("10000000")); // 1 Crore

        analytics.setLongTermRelationships(longTermRefs.size());
        analytics.setHighValueRelationships(highValueRefs.size());

        // Calculate average relationship duration
        List<TradeReference> allRefs = tradeReferenceRepository.findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(business);
        double avgDuration = allRefs.stream()
                .filter(ref -> ref.getRelationshipDurationMonths() != null)
                .mapToInt(TradeReference::getRelationshipDurationMonths)
                .average()
                .orElse(0.0);
        analytics.setAverageRelationshipDuration(avgDuration);

        // Performance Indicators
        long excellentPaymentCount = allRefs.stream()
                .filter(ref -> ref.getPaymentBehavior() == PaymentBehavior.EXCELLENT &&
                        ref.getVerificationStatus() == ReferenceVerificationStatus.VERIFIED)
                .count();
        analytics.setExcellentPaymentBehaviorCount((int) excellentPaymentCount);

        long highlyRecommendedCount = allRefs.stream()
                .filter(ref -> ref.getRecommendationLevel() == RecommendationLevel.HIGHLY_RECOMMENDED &&
                        ref.getVerificationStatus() == ReferenceVerificationStatus.VERIFIED)
                .count();
        analytics.setHighlyRecommendedCount((int) highlyRecommendedCount);

        // Recent verifications (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<TradeReference> recentVerifications = tradeReferenceRepository
                .findRecentlyVerifiedReferences(thirtyDaysAgo);
        long recentVerificationCount = recentVerifications.stream()
                .filter(ref -> ref.getBusiness().getId().equals(businessId))
                .count();
        analytics.setRecentVerifications((int) recentVerificationCount);

        logger.info("Trade reference analytics generated successfully for business: {}", business.getBusinessName());

        return analytics;
    }

    public void verifyTradeReference(Long referenceId, ReferenceVerificationStatus status,
                                     VerificationMethod method, String notes, String response) {
        logger.info("Verifying trade reference ID: {} - Status: {}", referenceId, status);

        TradeReference tradeReference = tradeReferenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade reference not found with ID: " + referenceId));

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        tradeReference.setVerificationStatus(status);
        tradeReference.setVerificationMethod(method);
        tradeReference.setVerifiedBy(currentUserPrincipal.getUsername());
        tradeReference.setVerifiedDate(LocalDateTime.now());
        tradeReference.setVerificationNotes(notes);
        tradeReference.setReferenceResponse(response);

        if (status == ReferenceVerificationStatus.VERIFIED ||
                status == ReferenceVerificationStatus.PARTIALLY_VERIFIED) {
            tradeReference.setContactAttemptedDate(LocalDateTime.now());
        }

        tradeReferenceRepository.save(tradeReference);

        // Send notification to the business owner
        sendVerificationNotificationAsync(tradeReference);

        logger.info("Trade reference verification completed");
    }

    public void markContactAttempted(Long referenceId, String notes) {
        logger.info("Marking contact attempted for trade reference ID: {}", referenceId);

        TradeReference tradeReference = tradeReferenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade reference not found with ID: " + referenceId));

        tradeReference.setContactAttemptedDate(LocalDateTime.now());
        if (notes != null && !notes.isEmpty()) {
            String existingNotes = tradeReference.getVerificationNotes();
            String updatedNotes = existingNotes != null ? existingNotes + "\n" + notes : notes;
            tradeReference.setVerificationNotes(updatedNotes);
        }

        tradeReferenceRepository.save(tradeReference);

        logger.info("Contact attempt marked for trade reference");
    }

    public void deleteTradeReference(Long referenceId) {
        logger.info("Deleting trade reference with ID: {}", referenceId);

        TradeReference tradeReference = tradeReferenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade reference not found with ID: " + referenceId));

        // Check if current user can delete this record
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!tradeReference.getAddedBy().getId().equals(currentUserPrincipal.getId()) &&
                !currentUserPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only delete trade references that you added");
        }

        // Soft delete
        tradeReference.setIsActive(false);
        tradeReferenceRepository.save(tradeReference);

        logger.info("Trade reference deleted successfully");
    }

    public List<TradeReferenceResponse> searchTradeReferences(Long businessId, String searchTerm) {
        logger.info("Searching trade references for business ID: {} with term: {}", businessId, searchTerm);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        List<TradeReference> references = tradeReferenceRepository.searchReferences(business, searchTerm);

        return references.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper Methods

    private void validateDuplicateReference(Business business, String email, String phone,
                                            String gstin, Long excludeId) {
        Long excludeIdValue = excludeId != null ? excludeId : -1L;
        List<TradeReference> duplicates = tradeReferenceRepository.findPotentialDuplicates(
                business, email, phone, gstin, excludeIdValue);

        if (!duplicates.isEmpty()) {
            throw new BadRequestException("A trade reference with similar contact information already exists");
        }
    }

    private boolean hasSignificantChanges(TradeReference existing, TradeReferenceRequest request) {
        return !existing.getEmail().equals(request.getEmail()) ||
                !existing.getPhone().equals(request.getPhone()) ||
                !existing.getContactPerson().equals(request.getContactPerson()) ||
                !existing.getPaymentBehavior().equals(request.getPaymentBehavior()) ||
                !existing.getRecommendationLevel().equals(request.getRecommendationLevel());
    }

    @Async
    protected void sendVerificationEmailAsync(TradeReference tradeReference) {
        try {
            String subject = "Trade Reference Verification Request - " + tradeReference.getReferenceNumber();
            String content = buildVerificationEmailContent(tradeReference);

            emailService.sendHtmlEmail(tradeReference.getEmail(), subject, content);

            logger.info("Verification email sent for reference: {}", tradeReference.getReferenceNumber());
        } catch (Exception e) {
            logger.error("Failed to send verification email for reference: {}",
                    tradeReference.getReferenceNumber(), e);
        }
    }

    @Async
    protected void sendVerificationNotificationAsync(TradeReference tradeReference) {
        try {
            String subject = "Trade Reference Verification Update - " + tradeReference.getReferenceNumber();
            String content = buildVerificationNotificationContent(tradeReference);

            // Send to business owner
            String businessOwnerEmail = tradeReference.getAddedBy().getEmail();
            emailService.sendHtmlEmail(businessOwnerEmail, subject, content);

            logger.info("Verification notification sent for reference: {}", tradeReference.getReferenceNumber());
        } catch (Exception e) {
            logger.error("Failed to send verification notification for reference: {}",
                    tradeReference.getReferenceNumber(), e);
        }
    }

    private String buildVerificationEmailContent(TradeReference tradeReference) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>Trade Reference Verification Request</h2>");
        content.append("<p>Dear ").append(tradeReference.getContactPerson()).append(",</p>");
        content.append("<p>We have received a trade reference request for your business relationship with <strong>")
                .append(tradeReference.getBusiness().getBusinessName()).append("</strong>.</p>");
        content.append("<h3>Reference Details:</h3>");
        content.append("<ul>");
        content.append("<li><strong>Reference Number:</strong> ").append(tradeReference.getReferenceNumber()).append("</li>");
        content.append("<li><strong>Business:</strong> ").append(tradeReference.getBusiness().getBusinessName()).append("</li>");
        content.append("<li><strong>Relationship Type:</strong> ").append(tradeReference.getRelationshipType()).append("</li>");
        content.append("<li><strong>Business Duration:</strong> ").append(tradeReference.getRelationshipDurationMonths()).append(" months</li>");
        content.append("</ul>");
        content.append("<p>Please reply to this email or contact us to verify this trade reference.</p>");
        content.append("<p>Thank you for your cooperation.</p>");
        content.append("<p>Best regards,<br>TCRS Team</p>");

        return content.toString();
    }

    private String buildVerificationNotificationContent(TradeReference tradeReference) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>Trade Reference Verification Update</h2>");
        content.append("<p>Dear ").append(tradeReference.getAddedBy().getFirstName()).append(",</p>");
        content.append("<p>Your trade reference <strong>").append(tradeReference.getReferenceNumber())
                .append("</strong> has been updated.</p>");
        content.append("<h3>Verification Details:</h3>");
        content.append("<ul>");
        content.append("<li><strong>Reference Company:</strong> ").append(tradeReference.getCompanyName()).append("</li>");
        content.append("<li><strong>Status:</strong> ").append(tradeReference.getVerificationStatus()).append("</li>");
        content.append("<li><strong>Verified By:</strong> ").append(tradeReference.getVerifiedBy()).append("</li>");
        content.append("<li><strong>Verification Date:</strong> ").append(tradeReference.getVerifiedDate()).append("</li>");
        content.append("</ul>");
        if (tradeReference.getVerificationNotes() != null) {
            content.append("<p><strong>Notes:</strong> ").append(tradeReference.getVerificationNotes()).append("</p>");
        }
        content.append("<p>You can view the full details in your TCRS dashboard.</p>");
        content.append("<p>Best regards,<br>TCRS Team</p>");

        return content.toString();
    }

    private Map<String, Integer> getReferenceTypeDistribution(Business business) {
        List<Object[]> results = tradeReferenceRepository.getReferenceTypeDistribution(business);
        Map<String, Integer> distribution = new HashMap<>();

        for (Object[] result : results) {
            ReferenceType type = (ReferenceType) result[0];
            Long count = (Long) result[1];
            distribution.put(type.toString(), count.intValue());
        }

        return distribution;
    }

    private Map<String, Integer> getRelationshipTypeDistribution(Business business) {
        List<Object[]> results = tradeReferenceRepository.getRelationshipTypeDistribution(business);
        Map<String, Integer> distribution = new HashMap<>();

        for (Object[] result : results) {
            RelationshipType type = (RelationshipType) result[0];
            Long count = (Long) result[1];
            distribution.put(type.toString(), count.intValue());
        }

        return distribution;
    }

    private Map<String, Integer> getPaymentBehaviorDistribution(Business business) {
        List<Object[]> results = tradeReferenceRepository.getPaymentBehaviorDistribution(business);
        Map<String, Integer> distribution = new HashMap<>();

        for (Object[] result : results) {
            PaymentBehavior behavior = (PaymentBehavior) result[0];
            Long count = (Long) result[1];
            distribution.put(behavior.toString(), count.intValue());
        }

        return distribution;
    }

    private Map<String, Integer> getRecommendationLevelDistribution(Business business) {
        List<Object[]> results = tradeReferenceRepository.getRecommendationLevelDistribution(business);
        Map<String, Integer> distribution = new HashMap<>();

        for (Object[] result : results) {
            RecommendationLevel level = (RecommendationLevel) result[0];
            Long count = (Long) result[1];
            distribution.put(level.toString(), count.intValue());
        }

        return distribution;
    }

    private String generateReferenceNumber() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TradeReferenceResponse convertToResponse(TradeReference tradeReference) {
        TradeReferenceResponse response = new TradeReferenceResponse();

        response.setId(tradeReference.getId());
        response.setBusinessId(tradeReference.getBusiness().getId());
        response.setBusinessName(tradeReference.getBusiness().getBusinessName());

        if (tradeReference.getReferenceProvider() != null) {
            response.setReferenceProviderId(tradeReference.getReferenceProvider().getId());
            response.setReferenceProviderName(tradeReference.getReferenceProvider().getBusinessName());
        }

        response.setReferenceNumber(tradeReference.getReferenceNumber());
        response.setCompanyName(tradeReference.getCompanyName());
        response.setContactPerson(tradeReference.getContactPerson());
        response.setDesignation(tradeReference.getDesignation());
        response.setEmail(tradeReference.getEmail());
        response.setPhone(tradeReference.getPhone());
        response.setCompanyAddress(tradeReference.getCompanyAddress());
        response.setCompanyGstin(tradeReference.getCompanyGstin());
        response.setReferenceType(tradeReference.getReferenceType());
        response.setRelationshipType(tradeReference.getRelationshipType());
        response.setRelationshipDurationMonths(tradeReference.getRelationshipDurationMonths());
        response.setRelationshipStartDate(tradeReference.getRelationshipStartDate());
        response.setRelationshipEndDate(tradeReference.getRelationshipEndDate());
        response.setAverageMonthlyBusiness(tradeReference.getAverageMonthlyBusiness());
        response.setTotalBusinessValue(tradeReference.getTotalBusinessValue());
        response.setCreditLimitProvided(tradeReference.getCreditLimitProvided());
        response.setPaymentTerms(tradeReference.getPaymentTerms());
        response.setPaymentBehavior(tradeReference.getPaymentBehavior());
        response.setPaymentRating(tradeReference.getPaymentRating());
        response.setOverallRating(tradeReference.getOverallRating());
        response.setHasDisputes(tradeReference.getHasDisputes());
        response.setDisputeDetails(tradeReference.getDisputeDetails());
        response.setReferenceComments(tradeReference.getReferenceComments());
        response.setRecommendationLevel(tradeReference.getRecommendationLevel());
        response.setVerificationStatus(tradeReference.getVerificationStatus());
        response.setVerificationMethod(tradeReference.getVerificationMethod());
        response.setVerifiedBy(tradeReference.getVerifiedBy());
        response.setVerifiedDate(tradeReference.getVerifiedDate());
        response.setVerificationNotes(tradeReference.getVerificationNotes());
        response.setReferenceResponse(tradeReference.getReferenceResponse());
        response.setContactAttemptedDate(tradeReference.getContactAttemptedDate());
        response.setIsConfidential(tradeReference.getIsConfidential());
        response.setAddedByName(tradeReference.getAddedBy().getFirstName() + " " +
                tradeReference.getAddedBy().getLastName());
        response.setIsActive(tradeReference.getIsActive());
        response.setCreatedAt(tradeReference.getCreatedAt());
        response.setUpdatedAt(tradeReference.getUpdatedAt());

        return response;
    }
}
