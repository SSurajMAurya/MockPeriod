package com.mockperiod.main.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mockperiod.main.dto.ResendEmailRequest;
import com.mockperiod.main.dto.ResendEmailResponse;
import com.mockperiod.main.exceptions.CustomException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ResendEmailService {
    
    private final WebClient webClient;
    
    @Value("${resend.from-email}")
    private String defaultFromEmail;
    
    @Value("${resend.api-key}")
    private String apiKey;
    
    public ResendEmailService(WebClient resendWebClient) {
        this.webClient = resendWebClient;
    }
    
    public Mono<ResendEmailResponse> sendEmail(ResendEmailRequest request) {
        
        log.debug("Attempting to send email to: {}", request.getTo());
        
        // Set default from email if not provided
        if (request.getFrom() == null || request.getFrom().isEmpty()) {
            request.setFrom(defaultFromEmail);
        }
        
        // Log request details (without sensitive data)
        log.debug("Email request - From: {}, To: {}, Subject: {}", 
                  request.getFrom(), request.getTo(), request.getSubject());
        
        return webClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Resend API client error: {} - {}", response.statusCode(), response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("Error response body: {}", errorBody);
                                return Mono.error(new CustomException(
                                        "Failed to send email: " + errorBody,
                                        HttpStatus.BAD_REQUEST
                                ));
                            });
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Resend API server error: {} - {}", response.statusCode(), response.statusCode());
                    return Mono.error(new CustomException(
                            "Email service temporarily unavailable. Please try again later.",
                            HttpStatus.SERVICE_UNAVAILABLE
                    ));
                })
                .bodyToMono(ResendEmailResponse.class)
                .doOnSuccess(response -> {
                    log.info("Email sent successfully to: {}, ID: {}", request.getTo(), response.getId());
                })
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Resend API error - Status: {}, Response: {}", 
                              ex.getStatusCode(), ex.getResponseBodyAsString());
                    throw new CustomException(
                            "Failed to send email: " + ex.getResponseBodyAsString(),
                            HttpStatus.valueOf(ex.getStatusCode().value())
                    );
                })
                .doOnError(Exception.class, ex -> {
                    log.error("Unexpected error sending email: {}", ex.getMessage(), ex);
                    throw new CustomException(
                            "Failed to send email due to unexpected error",
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                });
    }
    
    // Convenience methods
    public Mono<ResendEmailResponse> sendSimpleEmail(String to, String subject, String body) {
        ResendEmailRequest request = new ResendEmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setText(body);
        
        return sendEmail(request);
    }
    
    public Mono<ResendEmailResponse> sendResetEmail(String to, Integer otp) {
        // Create HTML email with OTP
        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 20px; }
                    .otp-box { background-color: #f8f9fa; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0; font-size: 24px; font-weight: bold; color: #007bff; border: 2px dashed #007bff; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                    .warning { color: #dc3545; font-size: 14px; font-weight: bold; }
                    .info { color: #666; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>You have requested to reset your password. Please use the OTP below to complete the process:</p>
                        
                        <div class="otp-box">
                            %s
                        </div>
                        
                        <p class="warning">⚠️ This OTP is valid for 5 minutes only.</p>
                        <p class="warning">⚠️ Do not share this OTP with anyone.</p>
                        
                        <p class="info">If you did not request a password reset, please ignore this email.</p>
                        
                        <p>Best regards,<br>Your Application Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, otp);
        
        ResendEmailRequest request = new ResendEmailRequest();
        request.setTo(to);
        request.setSubject("Password Reset OTP - Your Verification Code");
        request.setHtml(htmlBody);
        
        // Also include plain text version for email clients that don't support HTML
        String textBody = String.format("""
            Password Reset Request
            
            Hello,
            
            You have requested to reset your password. Please use the OTP below to complete the process:
            
            OTP: %s
            
            ⚠️ This OTP is valid for 5 minutes only.
            ⚠️ Do not share this OTP with anyone.
            
            If you did not request a password reset, please ignore this email.
            
            Best regards,
            Your Application Team
            """, otp);
        request.setText(textBody);
        
        return sendEmail(request);
    }
    
    public Mono<ResendEmailResponse> sendHtmlEmail(String to, String subject, String htmlBody) {
        ResendEmailRequest request = new ResendEmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setHtml(htmlBody);
        
        return sendEmail(request);
    }
    
    // Synchronous wrapper for blocking operations
    public ResendEmailResponse sendResetEmailSync(String to, Integer otp) {
        return sendResetEmail(to, otp)
                .onErrorResume(e -> {
                    log.error("Failed to send reset email to {}: {}", to, e.getMessage());
                    throw new CustomException("Failed to send reset email", HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .block();
    }
}