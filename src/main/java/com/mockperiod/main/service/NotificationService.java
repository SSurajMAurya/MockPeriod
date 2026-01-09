package com.mockperiod.main.service;

import com.mockperiod.main.dto.NotificationDto;
import com.mockperiod.main.entities.NotificationType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {

	NotificationDto createNotification(NotificationDto notificationDTO);

	// Get all notifications
	List<NotificationDto> getAllNotifications();

	// Get notification by ID
	NotificationDto getNotificationById(Long id);

	// Get all notifications by type
	List<NotificationDto> getAllByType(NotificationType type);

	// Get all notifications for a specific user (direct or in list)
	List<NotificationDto> getAllByUserId(Long userId);

	// Get all notifications where user is specifically in userIds list
	List<NotificationDto> getAllByUserInUserIds(Long userId);

	// Get notifications by type for a specific user
	List<NotificationDto> getByTypeAndUserId(NotificationType type, Long userId);

	// Get notifications by type where user is in userIds list
	List<NotificationDto> getByTypeAndUserInUserIds(NotificationType type, Long userId);

	// Get notifications for multiple users
	List<NotificationDto> getByUserIds(List<Long> userIds);

	// Mark notification as read
	NotificationDto markAsRead(Long notificationId);

	// Mark all notifications as read for user
	Integer markAllAsRead(Long userId);

	// Delete notification
	void deleteNotification(Long id);

	// Get unread notifications for user
	List<NotificationDto> getUnreadByUserId(Long userId);

	// Get unread notifications count for user
	Long getUnreadCountByUserId(Long userId);

	// Update notification
	NotificationDto updateNotification(Long id, NotificationDto notificationDTO);

}