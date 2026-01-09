package com.mockperiod.main.serviceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mockperiod.main.dto.NotificationCriteria;
import com.mockperiod.main.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final UserService userService;

    // Send to specific user by username - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<Void> sendToUser(String username, Object message) {
        return CompletableFuture.runAsync(() -> {
            try {
                messagingTemplate.convertAndSendToUser(
                    username, 
                    "/queue/notifications", 
                    message
                );
                log.debug("Notification sent to user: {}", username);
            } catch (Exception e) {
                log.error("Failed to send notification to user: {}", username, e);
                throw new RuntimeException("Failed to send notification", e);
            }
        });
    }

    // Send to multiple users - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<Void> sendToUsers(List<String> usernames, Object message) {
        return CompletableFuture.runAsync(() -> {
            usernames.forEach(username -> {
                try {
                    sendToUser(username, message);
                    log.debug("Notification sent to user in batch: {}", username);
                } catch (Exception e) {
                    log.error("Failed to send batch notification to user: {}", username, e);
                }
            });
        });
    }

    // Send to multiple users with individual error handling - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<List<String>> sendToUsersWithResult(List<String> usernames, Object message) {
        return CompletableFuture.supplyAsync(() -> {
            return usernames.stream()
                .filter(username -> {
                    try {
                        messagingTemplate.convertAndSendToUser(
                            username, 
                            "/queue/notifications", 
                            message
                        );
                        log.debug("Notification sent to user: {}", username);
                        return true;
                    } catch (Exception e) {
                        log.error("Failed to send notification to user: {}", username, e);
                        return false;
                    }
                })
                .toList();
        });
    }

    // Broadcast to all connected users - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<Void> broadcastToAll(Object message) {
        return CompletableFuture.runAsync(() -> {
            try {
                messagingTemplate.convertAndSend("/topic/notifications", message);
                log.debug("Broadcast notification sent to all users");
            } catch (Exception e) {
                log.error("Failed to broadcast notification", e);
                throw new RuntimeException("Failed to broadcast notification", e);
            }
        });
    }

    // Check if user is connected - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<Boolean> isUserConnectedAsync(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean connected = userRegistry.getUsers().stream()
                        .anyMatch(user -> user.getName().equals(username));
                log.debug("User connection check for {}: {}", username, connected);
                return connected;
            } catch (Exception e) {
                log.error("Failed to check user connection: {}", username, e);
                return false;
            }
        });
    }

    // Send with retry logic - ASYNC
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendToUserWithRetry(String username, Object message, int maxRetries) {
        return CompletableFuture.supplyAsync(() -> {
            int attempt = 0;
            while (attempt < maxRetries) {
                try {
                    messagingTemplate.convertAndSendToUser(
                        username, 
                        "/queue/notifications", 
                        message
                    );
                    log.debug("Notification sent to user: {} on attempt {}", username, attempt + 1);
                    return true;
                } catch (Exception e) {
                    attempt++;
                    log.warn("Attempt {} failed for user: {}", attempt, username);
                    if (attempt >= maxRetries) {
                        log.error("All retry attempts failed for user: {}", username, e);
                        return false;
                    }
                    // Wait before retry
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
            return false;
        });
    }

    // Batch send with completion callback
    @Async("taskExecutor")
    public void sendToUsersWithCallback(List<String> usernames, Object message, 
                                      SendCompletionCallback callback) {
        CompletableFuture.runAsync(() -> {
            int successCount = 0;
            int failureCount = 0;
            
            for (String username : usernames) {
                try {
                    messagingTemplate.convertAndSendToUser(
                        username, 
                        "/queue/notifications", 
                        message
                    );
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to send to user: {}", username, e);
                }
            }
            
            // Execute callback
            if (callback != null) {
                callback.onComplete(successCount, failureCount);
            }
        });
    }

    // Functional interface for callback
    @FunctionalInterface
    public interface SendCompletionCallback {
        void onComplete(int successCount, int failureCount);
    }
}