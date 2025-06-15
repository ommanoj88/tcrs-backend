package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.monitoring.CreditMonitoringRequest;
import com.tcrs.tcrs_backend.dto.monitoring.CreditMonitoringResponse;
import com.tcrs.tcrs_backend.dto.monitoring.CreditAlertResponse;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.*;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CreditMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(CreditMonitoringService.class);

    @Autowired
    private CreditMonitoringRepository creditMonitoringRepository;

    @Autowired
    private CreditAlertRepository creditAlertRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditReportRepository creditReportRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private TradeReferenceRepository tradeReferenceRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CreditScoringService creditScoringService;

    public CreditMonitoringResponse setupCreditMonitoring(CreditMonitoringRequest request) {
        logger.info("Setting up credit monitoring for business ID: {}", request.getBusinessId());

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        // Check if monitoring already exists
        if (creditMonitoringRepository.existsByUserAndBusinessAndIsActiveTrue(user, business)) {
            throw new BadRequestException("Credit monitoring already exists for this business");
        }

        CreditMonitoring monitoring = new CreditMonitoring();
        monitoring.setUser(user);
        monitoring.setBusiness(business);
        monitoring.setMonitoringName(request.getMonitoringName());
        monitoring.setMonitoringType(request.getMonitoringType());
        monitoring.setCreditScoreThresholdMin(request.getCreditScoreThresholdMin());
        monitoring.setCreditScoreThresholdMax(request.getCreditScoreThresholdMax());
        monitoring.setCreditScoreChangeThreshold(request.getCreditScoreChangeThreshold());
        monitoring.setPaymentDelayThresholdDays(request.getPaymentDelayThresholdDays());
        monitoring.setOverdueAmountThreshold(request.getOverdueAmountThreshold());
        monitoring.setNewTradeReferenceAlert(request.getNewTradeReferenceAlert());
        monitoring.setNewPaymentHistoryAlert(request.getNewPaymentHistoryAlert());
        monitoring.setCreditReportGenerationAlert(request.getCreditReportGenerationAlert());
        monitoring.setBusinessProfileChangeAlert(request.getBusinessProfileChangeAlert());
        monitoring.setEmailNotifications(request.getEmailNotifications());
        monitoring.setSmsNotifications(request.getSmsNotifications());
        monitoring.setInAppNotifications(request.getInAppNotifications());
        monitoring.setNotificationFrequency(request.getNotificationFrequency());
        monitoring.setNotes(request.getNotes());

        // Set initial credit score if available
        try {
            BigDecimal currentScore = creditScoringService.calculateCurrentCreditScore(business.getId());
            monitoring.setLastCreditScore(currentScore);
        } catch (Exception e) {
            logger.warn("Could not get initial credit score for business: {}", business.getId());
        }

        CreditMonitoring savedMonitoring = creditMonitoringRepository.save(monitoring);

        // Create initial setup alert
        createSystemAlert(savedMonitoring, AlertType.SYSTEM_ALERT, AlertSeverity.LOW,
                "Credit Monitoring Activated",
                "Credit monitoring has been successfully set up for " + business.getBusinessName(),
                null);

        logger.info("Credit monitoring setup completed for business: {}", business.getBusinessName());

        return convertToMonitoringResponse(savedMonitoring);
    }

    public CreditMonitoringResponse updateCreditMonitoring(Long monitoringId, CreditMonitoringRequest request) {
        logger.info("Updating credit monitoring ID: {}", monitoringId);

        CreditMonitoring monitoring = creditMonitoringRepository.findById(monitoringId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit monitoring not found with ID: " + monitoringId));

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!monitoring.getUser().getId().equals(currentUserPrincipal.getId())) {
            throw new BadRequestException("You can only update your own monitoring setups");
        }

        monitoring.setMonitoringName(request.getMonitoringName());
        monitoring.setMonitoringType(request.getMonitoringType());
        monitoring.setCreditScoreThresholdMin(request.getCreditScoreThresholdMin());
        monitoring.setCreditScoreThresholdMax(request.getCreditScoreThresholdMax());
        monitoring.setCreditScoreChangeThreshold(request.getCreditScoreChangeThreshold());
        monitoring.setPaymentDelayThresholdDays(request.getPaymentDelayThresholdDays());
        monitoring.setOverdueAmountThreshold(request.getOverdueAmountThreshold());
        monitoring.setNewTradeReferenceAlert(request.getNewTradeReferenceAlert());
        monitoring.setNewPaymentHistoryAlert(request.getNewPaymentHistoryAlert());
        monitoring.setCreditReportGenerationAlert(request.getCreditReportGenerationAlert());
        monitoring.setBusinessProfileChangeAlert(request.getBusinessProfileChangeAlert());
        monitoring.setEmailNotifications(request.getEmailNotifications());
        monitoring.setSmsNotifications(request.getSmsNotifications());
        monitoring.setInAppNotifications(request.getInAppNotifications());
        monitoring.setNotificationFrequency(request.getNotificationFrequency());
        monitoring.setNotes(request.getNotes());

        CreditMonitoring updatedMonitoring = creditMonitoringRepository.save(monitoring);

        logger.info("Credit monitoring updated successfully");

        return convertToMonitoringResponse(updatedMonitoring);
    }

    public Page<CreditMonitoringResponse> getUserCreditMonitoring(int page, int size) {
        logger.info("Retrieving user credit monitoring setups - page: {}, size: {}", page, size);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CreditMonitoring> monitoringPage = creditMonitoringRepository
                .findByUserAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);

        return monitoringPage.map(this::convertToMonitoringResponse);
    }

    public Page<CreditAlertResponse> getUserAlerts(int page, int size, boolean unreadOnly) {
        logger.info("Retrieving user alerts - page: {}, size: {}, unreadOnly: {}", page, size, unreadOnly);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CreditAlert> alertsPage;

        if (unreadOnly) {
            alertsPage = creditAlertRepository.findByUserAndIsReadFalseAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
        } else {
            alertsPage = creditAlertRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
        }

        return alertsPage.map(this::convertToAlertResponse);
    }

    public CreditAlertResponse markAlertAsRead(Long alertId) {
        logger.info("Marking alert as read: {}", alertId);

        CreditAlert alert = creditAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!alert.getUser().getId().equals(currentUserPrincipal.getId())) {
            throw new BadRequestException("You can only mark your own alerts as read");
        }

        alert.setIsRead(true);
        CreditAlert savedAlert = creditAlertRepository.save(alert);

        return convertToAlertResponse(savedAlert);
    }

    public CreditAlertResponse acknowledgeAlert(Long alertId, String notes) {
        logger.info("Acknowledging alert: {}", alertId);

        CreditAlert alert = creditAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!alert.getUser().getId().equals(currentUserPrincipal.getId())) {
            throw new BadRequestException("You can only acknowledge your own alerts");
        }

        alert.setIsAcknowledged(true);
        alert.setAcknowledgedBy(currentUserPrincipal.getUsername());
        alert.setAcknowledgedDate(LocalDateTime.now());
        alert.setAcknowledgmentNotes(notes);

        CreditAlert savedAlert = creditAlertRepository.save(alert);

        return convertToAlertResponse(savedAlert);
    }

    public Map<String, Object> getUserAlertStatistics() {
        logger.info("Retrieving user alert statistics");

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        stats.put("totalAlerts", creditAlertRepository.countTotalAlertsByUser(user));
        stats.put("unreadAlerts", creditAlertRepository.countUnreadAlertsByUser(user));
        stats.put("unacknowledgedAlerts", creditAlertRepository.countUnacknowledgedAlertsByUser(user));
        stats.put("activeMonitoring", creditMonitoringRepository.countActiveMonitoringByUser(user));

        // Severity distribution
        stats.put("criticalAlerts", creditAlertRepository.countAlertsByUserAndSeverity(user, AlertSeverity.CRITICAL));
        stats.put("highAlerts", creditAlertRepository.countAlertsByUserAndSeverity(user, AlertSeverity.HIGH));
        stats.put("mediumAlerts", creditAlertRepository.countAlertsByUserAndSeverity(user, AlertSeverity.MEDIUM));
        stats.put("lowAlerts", creditAlertRepository.countAlertsByUserAndSeverity(user, AlertSeverity.LOW));

        // Recent activity
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<CreditAlert> recentAlerts = creditAlertRepository.findRecentAlertsByUser(user, lastWeek);
        stats.put("recentAlerts", recentAlerts.size());

        // Type distribution (last 30 days)
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        List<Object[]> typeDistribution = creditAlertRepository.getAlertTypeDistributionByUser(user, lastMonth);
        Map<String, Long> typeStats = new HashMap<>();
        for (Object[] result : typeDistribution) {
            typeStats.put(result[0].toString(), (Long) result[1]);
        }
        stats.put("alertTypeDistribution", typeStats);

        return stats;
    }

    // Scheduled monitoring tasks
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void performScheduledMonitoring() {
        logger.info("Starting scheduled credit monitoring check");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        List<CreditMonitoring> monitoringList = creditMonitoringRepository.findMonitoringDueForCheck(cutoffTime);

        logger.info("Found {} monitoring setups due for check", monitoringList.size());

        for (CreditMonitoring monitoring : monitoringList) {
            try {
                performMonitoringCheck(monitoring);
            } catch (Exception e) {
                logger.error("Error performing monitoring check for ID: {}", monitoring.getId(), e);
            }
        }

        logger.info("Completed scheduled credit monitoring check");
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void processScheduledNotifications() {
        logger.info("Processing scheduled notifications");

        // Process hourly notifications
        List<CreditMonitoring> hourlyMonitoring = creditMonitoringRepository
                .findActiveMonitoringByEmailNotificationFrequency(NotificationFrequency.HOURLY);

        for (CreditMonitoring monitoring : hourlyMonitoring) {
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            List<CreditAlert> recentAlerts = creditAlertRepository
                    .findRecentAlertsByMonitoring(monitoring, lastHour);

            if (!recentAlerts.isEmpty()) {
                sendBatchNotification(monitoring, recentAlerts, "Hourly");
            }
        }

        logger.info("Completed processing scheduled notifications");
    }

    @Scheduled(cron = "0 0 9 * * *") // Run daily at 9 AM
    public void processDailyNotifications() {
        logger.info("Processing daily notifications");

        List<CreditMonitoring> dailyMonitoring = creditMonitoringRepository
                .findActiveMonitoringByEmailNotificationFrequency(NotificationFrequency.DAILY);

        for (CreditMonitoring monitoring : dailyMonitoring) {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<CreditAlert> recentAlerts = creditAlertRepository
                    .findRecentAlertsByMonitoring(monitoring, yesterday);

            if (!recentAlerts.isEmpty()) {
                sendBatchNotification(monitoring, recentAlerts, "Daily");
            }
        }

        logger.info("Completed processing daily notifications");
    }

    // Event-triggered monitoring methods
    @Async
    public void onCreditReportGenerated(Long businessId, String reportNumber) {
        logger.info("Credit report generated for business: {}, triggering monitoring checks", businessId);

        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) return;

        List<CreditMonitoring> monitoringList = creditMonitoringRepository
                .findMonitoringWithCreditReportAlerts();

        for (CreditMonitoring monitoring : monitoringList) {
            if (monitoring.getBusiness().getId().equals(businessId)) {
                createAlert(monitoring, AlertType.CREDIT_REPORT_GENERATED, AlertSeverity.LOW,
                        "New Credit Report Generated",
                        "A new credit report #" + reportNumber + " has been generated for " + business.getBusinessName(),
                        "CreditReport", null);
            }
        }
    }

    @Async
    public void onPaymentHistoryAdded(Long businessId, Long paymentHistoryId) {
        logger.info("Payment history added for business: {}, triggering monitoring checks", businessId);

        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) return;

        List<CreditMonitoring> monitoringList = creditMonitoringRepository
                .findMonitoringWithPaymentHistoryAlerts();

        for (CreditMonitoring monitoring : monitoringList) {
            if (monitoring.getBusiness().getId().equals(businessId)) {
                createAlert(monitoring, AlertType.NEW_PAYMENT_HISTORY, AlertSeverity.MEDIUM,
                        "New Payment History Added",
                        "New payment transaction has been recorded for " + business.getBusinessName(),
                        "PaymentHistory", paymentHistoryId);

                // Check for payment delay alerts
                checkPaymentDelayAlerts(monitoring, paymentHistoryId);
            }
        }
    }

    @Async
    public void onTradeReferenceAdded(Long businessId, Long tradeReferenceId) {
        logger.info("Trade reference added for business: {}, triggering monitoring checks", businessId);

        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) return;

        List<CreditMonitoring> monitoringList = creditMonitoringRepository
                .findMonitoringWithTradeReferenceAlerts();

        for (CreditMonitoring monitoring : monitoringList) {
            if (monitoring.getBusiness().getId().equals(businessId)) {
                createAlert(monitoring, AlertType.NEW_TRADE_REFERENCE, AlertSeverity.MEDIUM,
                        "New Trade Reference Added",
                        "A new trade reference has been added for " + business.getBusinessName(),
                        "TradeReference", tradeReferenceId);
            }
        }
    }

    @Async
    public void onBusinessProfileUpdated(Long businessId) {
        logger.info("Business profile updated for business: {}, triggering monitoring checks", businessId);

        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) return;

        List<CreditMonitoring> monitoringList = creditMonitoringRepository
                .findMonitoringWithBusinessProfileAlerts();

        for (CreditMonitoring monitoring : monitoringList) {
            if (monitoring.getBusiness().getId().equals(businessId)) {
                createAlert(monitoring, AlertType.BUSINESS_PROFILE_UPDATED, AlertSeverity.LOW,
                        "Business Profile Updated",
                        "Business profile information has been updated for " + business.getBusinessName(),
                        "Business", businessId);
            }
        }
    }

    // Helper methods
    private void performMonitoringCheck(CreditMonitoring monitoring) {
        logger.debug("Performing monitoring check for ID: {}", monitoring.getId());

        try {
            // Update last check date
            monitoring.setLastCheckDate(LocalDateTime.now());

            // Check credit score changes
            checkCreditScoreChanges(monitoring);

            // Check credit score thresholds
            checkCreditScoreThresholds(monitoring);

            // Save updated monitoring
            creditMonitoringRepository.save(monitoring);

        } catch (Exception e) {
            logger.error("Error in monitoring check for ID: {}", monitoring.getId(), e);
        }
    }

    private void checkCreditScoreChanges(CreditMonitoring monitoring) {
        try {
            BigDecimal currentScore = creditScoringService.calculateCurrentCreditScore(monitoring.getBusiness().getId());
            BigDecimal lastScore = monitoring.getLastCreditScore();

            if (lastScore != null && monitoring.getCreditScoreChangeThreshold() != null) {
                BigDecimal change = currentScore.subtract(lastScore);
                BigDecimal changeAbs = change.abs();

                if (changeAbs.compareTo(monitoring.getCreditScoreChangeThreshold()) >= 0) {
                    AlertSeverity severity = changeAbs.compareTo(new BigDecimal("50")) >= 0 ?
                            AlertSeverity.HIGH : AlertSeverity.MEDIUM;

                    String direction = change.compareTo(BigDecimal.ZERO) > 0 ? "increased" : "decreased";

                    String title = "Credit Score " + direction.substring(0, 1).toUpperCase() + direction.substring(1);
                    String description = String.format("Credit score has %s by %.1f points (from %.1f to %.1f) for %s",
                            direction, changeAbs, lastScore, currentScore, monitoring.getBusiness().getBusinessName());

                    Map<String, Object> details = new HashMap<>();
                    details.put("previousScore", lastScore);
                    details.put("currentScore", currentScore);
                    details.put("change", change);
                    details.put("changePercentage", change.divide(lastScore, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));

                    createAlertWithDetails(monitoring, AlertType.CREDIT_SCORE_CHANGE, severity, title, description, details);
                }
            }

            // Update last known score
            monitoring.setLastCreditScore(currentScore);

        } catch (Exception e) {
            logger.error("Error checking credit score changes for monitoring ID: {}", monitoring.getId(), e);
        }
    }

    private void checkCreditScoreThresholds(CreditMonitoring monitoring) {
        try {
            BigDecimal currentScore = creditScoringService.calculateCurrentCreditScore(monitoring.getBusiness().getId());

            // Check minimum threshold
            if (monitoring.getCreditScoreThresholdMin() != null &&
                    currentScore.compareTo(monitoring.getCreditScoreThresholdMin()) < 0) {

                createAlert(monitoring, AlertType.CREDIT_SCORE_THRESHOLD, AlertSeverity.HIGH,
                        "Credit Score Below Minimum Threshold",
                        String.format("Credit score (%.1f) has fallen below your minimum threshold (%.1f) for %s",
                                currentScore, monitoring.getCreditScoreThresholdMin(), monitoring.getBusiness().getBusinessName()),
                        null, null);
            }

            // Check maximum threshold
            if (monitoring.getCreditScoreThresholdMax() != null &&
                    currentScore.compareTo(monitoring.getCreditScoreThresholdMax()) > 0) {

                createAlert(monitoring, AlertType.CREDIT_SCORE_THRESHOLD, AlertSeverity.MEDIUM,
                        "Credit Score Above Maximum Threshold",
                        String.format("Credit score (%.1f) has exceeded your maximum threshold (%.1f) for %s",
                                currentScore, monitoring.getCreditScoreThresholdMax(), monitoring.getBusiness().getBusinessName()),
                        null, null);
            }

        } catch (Exception e) {
            logger.error("Error checking credit score thresholds for monitoring ID: {}", monitoring.getId(), e);
        }
    }

    private void checkPaymentDelayAlerts(CreditMonitoring monitoring, Long paymentHistoryId) {
        // Implementation for payment delay checking
        // This would analyze the payment history to detect delays exceeding thresholds
    }

    private void createAlert(CreditMonitoring monitoring, AlertType alertType, AlertSeverity severity,
                             String title, String description, String relatedEntityType, Long relatedEntityId) {
        createAlertWithDetails(monitoring, alertType, severity, title, description, null);
    }

    private void createAlertWithDetails(CreditMonitoring monitoring, AlertType alertType, AlertSeverity severity,
                                        String title, String description, Map<String, Object> details) {
        CreditAlert alert = new CreditAlert();
        alert.setCreditMonitoring(monitoring);
        alert.setUser(monitoring.getUser());
        alert.setBusiness(monitoring.getBusiness());
        alert.setAlertNumber(generateAlertNumber());
        alert.setAlertType(alertType);
        alert.setSeverityLevel(severity);
        alert.setTitle(title);
        alert.setDescription(description);

        if (details != null) {
            // Convert details to JSON or structured format
            alert.setDetails(convertDetailsToJson(details));
            alert.setPreviousValue(details.get("previousScore") != null ? details.get("previousScore").toString() : null);
            alert.setCurrentValue(details.get("currentScore") != null ? details.get("currentScore").toString() : null);
            alert.setChangeAmount((BigDecimal) details.get("change"));
            alert.setChangePercentage((BigDecimal) details.get("changePercentage"));
        }

        // Set expiration (30 days)
        alert.setExpiresAt(LocalDateTime.now().plusDays(30));

        CreditAlert savedAlert = creditAlertRepository.save(alert);

        // Update monitoring stats
        monitoring.setLastAlertDate(LocalDateTime.now());
        monitoring.setTotalAlertsSent(monitoring.getTotalAlertsSent() + 1);

        // Send notifications based on preferences
        sendAlertNotifications(savedAlert);

        logger.info("Alert created: {} for business: {}", alertType, monitoring.getBusiness().getBusinessName());
    }

    private void createSystemAlert(CreditMonitoring monitoring, AlertType alertType, AlertSeverity severity,
                                   String title, String description, String relatedEntityType) {
        createAlert(monitoring, alertType, severity, title, description, relatedEntityType, null);
    }

    @Async
    protected void sendAlertNotifications(CreditAlert alert) {
        CreditMonitoring monitoring = alert.getCreditMonitoring();

        // Send immediate notifications
        if (monitoring.getNotificationFrequency() == NotificationFrequency.IMMEDIATE) {
            if (monitoring.getEmailNotifications()) {
                sendEmailNotification(alert);
            }

            if (monitoring.getSmsNotifications()) {
                sendSmsNotification(alert);
            }
        }

        // Always mark for in-app notification
        if (monitoring.getInAppNotifications()) {
            alert.setInAppNotified(true);
            alert.setInAppNotifiedDate(LocalDateTime.now());
            creditAlertRepository.save(alert);
        }
    }

    private void sendEmailNotification(CreditAlert alert) {
        try {
            String subject = "TCRS Alert: " + alert.getTitle();
            String content = buildEmailNotificationContent(alert);

            emailService.sendHtmlEmail(alert.getUser().getEmail(), subject, content);

            alert.setEmailSent(true);
            alert.setEmailSentDate(LocalDateTime.now());
            creditAlertRepository.save(alert);

            logger.info("Email notification sent for alert: {}", alert.getAlertNumber());
        } catch (Exception e) {
            logger.error("Failed to send email notification for alert: {}", alert.getAlertNumber(), e);
        }
    }

    private void sendSmsNotification(CreditAlert alert) {
        // SMS notification implementation would go here
        // For now, just mark as sent
        alert.setSmsSent(true);
        alert.setSmsSentDate(LocalDateTime.now());
        creditAlertRepository.save(alert);
    }

    private void sendBatchNotification(CreditMonitoring monitoring, List<CreditAlert> alerts, String frequency) {
        try {
            String subject = String.format("TCRS %s Alert Summary - %s", frequency, monitoring.getMonitoringName());
            String content = buildBatchEmailContent(monitoring, alerts, frequency);

            emailService.sendHtmlEmail(monitoring.getUser().getEmail(), subject, content);

            logger.info("{} batch notification sent for monitoring: {}", frequency, monitoring.getId());
        } catch (Exception e) {
            logger.error("Failed to send {} batch notification for monitoring: {}", frequency, monitoring.getId(), e);
        }
    }

    private String buildEmailNotificationContent(CreditAlert alert) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>").append(alert.getTitle()).append("</h2>");
        content.append("<p><strong>Business:</strong> ").append(alert.getBusiness().getBusinessName()).append("</p>");
        content.append("<p><strong>Alert Type:</strong> ").append(alert.getAlertType()).append("</p>");
        content.append("<p><strong>Severity:</strong> ").append(alert.getSeverityLevel()).append("</p>");
        content.append("<p><strong>Description:</strong></p>");
        content.append("<p>").append(alert.getDescription()).append("</p>");

        if (alert.getPreviousValue() != null && alert.getCurrentValue() != null) {
            content.append("<h3>Details:</h3>");
            content.append("<ul>");
            content.append("<li><strong>Previous Value:</strong> ").append(alert.getPreviousValue()).append("</li>");
            content.append("<li><strong>Current Value:</strong> ").append(alert.getCurrentValue()).append("</li>");
            if (alert.getChangeAmount() != null) {
                content.append("<li><strong>Change:</strong> ").append(alert.getChangeAmount()).append("</li>");
            }
            content.append("</ul>");
        }

        content.append("<p><strong>Alert Time:</strong> ").append(alert.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        content.append("<p>Please log in to your TCRS dashboard to acknowledge this alert and take any necessary actions.</p>");

        return content.toString();
    }

    private String buildBatchEmailContent(CreditMonitoring monitoring, List<CreditAlert> alerts, String frequency) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>").append(frequency).append(" Alert Summary</h2>");
        content.append("<p><strong>Monitoring:</strong> ").append(monitoring.getMonitoringName()).append("</p>");
        content.append("<p><strong>Business:</strong> ").append(monitoring.getBusiness().getBusinessName()).append("</p>");
        content.append("<p><strong>Total Alerts:</strong> ").append(alerts.size()).append("</p>");

        content.append("<h3>Alert Summary:</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        content.append("<tr><th>Time</th><th>Type</th><th>Severity</th><th>Title</th></tr>");

        for (CreditAlert alert : alerts) {
            content.append("<tr>");
            content.append("<td>").append(alert.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))).append("</td>");
            content.append("<td>").append(alert.getAlertType()).append("</td>");
            content.append("<td>").append(alert.getSeverityLevel()).append("</td>");
            content.append("<td>").append(alert.getTitle()).append("</td>");
            content.append("</tr>");
        }

        content.append("</table>");
        content.append("<p>Please log in to your TCRS dashboard to view detailed information and acknowledge these alerts.</p>");

        return content.toString();
    }

    private String convertDetailsToJson(Map<String, Object> details) {
        // Simple JSON conversion - in production, use proper JSON library
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String generateAlertNumber() {
        return "ALT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CreditMonitoringResponse convertToMonitoringResponse(CreditMonitoring monitoring) {
        CreditMonitoringResponse response = new CreditMonitoringResponse();
        response.setId(monitoring.getId());
        response.setBusinessId(monitoring.getBusiness().getId());
        response.setBusinessName(monitoring.getBusiness().getBusinessName());
        response.setMonitoringName(monitoring.getMonitoringName());
        response.setMonitoringType(monitoring.getMonitoringType());
        response.setIsActive(monitoring.getIsActive());
        response.setCreditScoreThresholdMin(monitoring.getCreditScoreThresholdMin());
        response.setCreditScoreThresholdMax(monitoring.getCreditScoreThresholdMax());
        response.setCreditScoreChangeThreshold(monitoring.getCreditScoreChangeThreshold());
        response.setPaymentDelayThresholdDays(monitoring.getPaymentDelayThresholdDays());
        response.setOverdueAmountThreshold(monitoring.getOverdueAmountThreshold());
        response.setNewTradeReferenceAlert(monitoring.getNewTradeReferenceAlert());
        response.setNewPaymentHistoryAlert(monitoring.getNewPaymentHistoryAlert());
        response.setCreditReportGenerationAlert(monitoring.getCreditReportGenerationAlert());
        response.setBusinessProfileChangeAlert(monitoring.getBusinessProfileChangeAlert());
        response.setEmailNotifications(monitoring.getEmailNotifications());
        response.setSmsNotifications(monitoring.getSmsNotifications());
        response.setInAppNotifications(monitoring.getInAppNotifications());
        response.setNotificationFrequency(monitoring.getNotificationFrequency());
        response.setLastCheckDate(monitoring.getLastCheckDate());
        response.setLastAlertDate(monitoring.getLastAlertDate());
        response.setTotalAlertsSent(monitoring.getTotalAlertsSent());
        response.setLastCreditScore(monitoring.getLastCreditScore());
        response.setNotes(monitoring.getNotes());
        response.setCreatedAt(monitoring.getCreatedAt());
        response.setUpdatedAt(monitoring.getUpdatedAt());

        return response;
    }

    private CreditAlertResponse convertToAlertResponse(CreditAlert alert) {
        CreditAlertResponse response = new CreditAlertResponse();
        response.setId(alert.getId());
        response.setBusinessId(alert.getBusiness().getId());
        response.setBusinessName(alert.getBusiness().getBusinessName());
        response.setAlertNumber(alert.getAlertNumber());
        response.setAlertType(alert.getAlertType());
        response.setSeverityLevel(alert.getSeverityLevel());
        response.setTitle(alert.getTitle());
        response.setDescription(alert.getDescription());
        response.setDetails(alert.getDetails());
        response.setPreviousValue(alert.getPreviousValue());
        response.setCurrentValue(alert.getCurrentValue());
        response.setThresholdValue(alert.getThresholdValue());
        response.setChangeAmount(alert.getChangeAmount());
        response.setChangePercentage(alert.getChangePercentage());
        response.setIsRead(alert.getIsRead());
        response.setIsAcknowledged(alert.getIsAcknowledged());
        response.setAcknowledgedBy(alert.getAcknowledgedBy());
        response.setAcknowledgedDate(alert.getAcknowledgedDate());
        response.setAcknowledgmentNotes(alert.getAcknowledgmentNotes());
        response.setRelatedEntityType(alert.getRelatedEntityType());
        response.setRelatedEntityId(alert.getRelatedEntityId());
        response.setExpiresAt(alert.getExpiresAt());
        response.setCreatedAt(alert.getCreatedAt());

        return response;
    }
}