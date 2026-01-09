package com.mockperiod.main.controllers;


import com.mockperiod.main.dto.NotificationDto;
import com.mockperiod.main.entities.NotificationType;
import com.mockperiod.main.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "APIs for managing notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<NotificationDto> createNotification(
            @Valid @RequestBody NotificationDto notificationDTO) {
    	NotificationDto createdNotification = notificationService.createNotification(notificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }
    
    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        List<NotificationDto> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
    	NotificationDto notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get all notifications by type")
    public ResponseEntity<List<NotificationDto>> getAllByType(
            @PathVariable NotificationType type) {
        List<NotificationDto> notifications = notificationService.getAllByType(type);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a specific user")
    public ResponseEntity<List<NotificationDto>> getAllByUserId(
            @PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getAllByUserId(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/in-list")
    @Operation(summary = "Get notifications where user is in userIds list")
    public ResponseEntity<List<NotificationDto>> getAllByUserInUserIds(
            @PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getAllByUserInUserIds(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/type/{type}/user/{userId}")
    @Operation(summary = "Get notifications by type for a specific user (direct assignment)")
    public ResponseEntity<List<NotificationDto>> getByTypeAndUserId(
            @PathVariable NotificationType type,
            @PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getByTypeAndUserId(type, userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/type/{type}/user/{userId}/in-list")
    @Operation(summary = "Get notifications by type where user is in userIds list")
    public ResponseEntity<List<NotificationDto>> getByTypeAndUserInUserIds(
            @PathVariable NotificationType type,
            @PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getByTypeAndUserInUserIds(type, userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get notifications for multiple users")
    public ResponseEntity<List<NotificationDto>> getByUserIds(
            @RequestParam List<Long> userIds) {
        List<NotificationDto> notifications = notificationService.getByUserIds(userIds);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications for user")
    public ResponseEntity<List<NotificationDto>> getUnreadByUserId(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUnreadByUserId(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notifications count for user")
    public ResponseEntity<Map<String, Long>> getUnreadCountByUserId(@PathVariable Long userId) {
        Long count = notificationService.getUnreadCountByUserId(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
    	NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read for user")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@PathVariable Long userId) {
        Integer markedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedCount", markedCount));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update notification")
    public ResponseEntity<NotificationDto> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationDto notificationDTO) {
    	NotificationDto updatedNotification = notificationService.updateNotification(id, notificationDTO);
        return ResponseEntity.ok(updatedNotification);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}