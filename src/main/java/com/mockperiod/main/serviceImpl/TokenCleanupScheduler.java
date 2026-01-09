//package com.mockperiod.main.serviceImpl;
//
//import java.time.LocalDateTime;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.mockperiod.main.repository.ResetPasswordRepository;
//
//import jakarta.transaction.Transactional;
//
//@Component
//public class TokenCleanupScheduler {
//    
//    private final ResetPasswordRepository passwordResetTokenRepository;
//    
//    @Scheduled(fixedRate = 3600000) // Run every hour
//    @Transactional
//    public void cleanupExpiredTokens() {
//        LocalDateTime now = LocalDateTime.now();
//        passwordResetTokenRepository.deleteExpiredTokens(now);
//    }
//}
