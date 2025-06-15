package com.tcrs.tcrs_backend.service;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Service;

    import jakarta.mail.MessagingException;
    import jakarta.mail.internet.MimeMessage;
    import java.io.UnsupportedEncodingException;

    @Service
    public class EmailService {

        private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

        @Autowired
        private JavaMailSender mailSender;

        @Value("${spring.mail.username}")
        private String fromEmail;

        @Value("${app.name:TCRS}")
        private String appName;

        public void sendHtmlEmail(String to, String subject, String htmlContent) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, appName);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(buildEmailTemplate(subject, htmlContent), true);

                mailSender.send(message);

                logger.info("Email sent successfully to: {}", to);
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error("Failed to send email to: {}", to, e);
                throw new RuntimeException("Failed to send email", e);
            }
        }

        public void sendPlainTextEmail(String to, String subject, String content) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

                helper.setFrom(fromEmail, appName);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, false);

                mailSender.send(message);

                logger.info("Plain text email sent successfully to: {}", to);
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error("Failed to send plain text email to: {}", to, e);
                throw new RuntimeException("Failed to send email", e);
            }
        }

        private String buildEmailTemplate(String subject, String content) {
            StringBuilder template = new StringBuilder();
            template.append("<!DOCTYPE html>");
            template.append("<html><head>");
            template.append("<meta charset='UTF-8'>");
            template.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            template.append("<title>").append(subject).append("</title>");
            template.append("<style>");
            template.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }");
            template.append(".header { background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }");
            template.append(".content { background-color: #f8f9fa; padding: 30px; border-radius: 0 0 5px 5px; }");
            template.append(".footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }");
            template.append("h2 { color: #1f2937; margin-top: 0; }");
            template.append("ul { padding-left: 20px; }");
            template.append("li { margin-bottom: 5px; }");
            template.append("</style>");
            template.append("</head><body>");
            template.append("<div class='header'>");
            template.append("<h1>").append(appName).append("</h1>");
            template.append("</div>");
            template.append("<div class='content'>");
            template.append(content);
            template.append("</div>");
            template.append("<div class='footer'>");
            template.append("<p>This email was sent by ").append(appName).append(" - Trade Credit Reference System</p>");
            template.append("<p>Please do not reply to this email. For support, contact our team.</p>");
            template.append("</div>");
            template.append("</body></html>");

            return template.toString();
        }
    }