package com.mockperiod.main.serviceImpl;



import com.mockperiod.main.entities.Notifications;
import com.mockperiod.main.dto.NotificationDto;
import com.mockperiod.main.entities.NotificationType;
import com.mockperiod.main.repository.NotificationRepository;
import com.mockperiod.main.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public NotificationDto createNotification(NotificationDto notificationDTO) {
        log.info("Creating notification for user: {}", notificationDTO.getUserId());
        
        Notifications notification = new Notifications();
        notification.setUserId(notificationDTO.getUserId());
        notification.setUserIds(notificationDTO.getUserIds());
        notification.setType(NotificationType.valueOf(notificationDTO.getType()));
        notification.setMessage(notificationDTO.getMessage());
        
        Notifications savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(Long id) {
        log.info("Fetching notification by ID: {}", id);
        Notifications notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return convertToDTO(notification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllByType(NotificationType type) {
        log.info("Fetching all notifications by type: {}", type);
        return notificationRepository.findByType(type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllByUserId(Long userId) {
        log.info("Fetching all notifications for user ID: {}", userId);
        return notificationRepository.findAllByUser(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllByUserInUserIds(Long userId) {
        log.info("Fetching notifications where user {} is in userIds list", userId);
        return notificationRepository.findByUserInUserIds(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getByTypeAndUserId(NotificationType type, Long userId) {
        log.info("Fetching notifications of type {} for user {}", type, userId);
        return notificationRepository.findByTypeAndUserId(type, userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getByTypeAndUserInUserIds(NotificationType type, Long userId) {
        log.info("Fetching notifications of type {} where user {} is in userIds list", type, userId);
        return notificationRepository.findByTypeAndUserInUserIds(type, userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getByUserIds(List<Long> userIds) {
        log.info("Fetching notifications for multiple users: {}", userIds);
        return notificationRepository.findByUserIdIn(userIds)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public NotificationDto markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        
        notification.setRead(true);
        Notifications updatedNotification = notificationRepository.save(notification);
        return convertToDTO(updatedNotification);
    }
    
    @Override
    public Integer markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        List<Notifications> unreadNotifications = notificationRepository.findUnreadByUser(userId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        
        return unreadNotifications.size();
    }
    
    @Override
    public void deleteNotification(Long id) {
        log.info("Deleting notification with ID: {}", id);
        notificationRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadByUserId(Long userId) {
        log.info("Fetching unread notifications for user: {}", userId);
        return notificationRepository.findUnreadByUser(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCountByUserId(Long userId) {
        log.info("Getting unread count for user: {}", userId);
        return (long) notificationRepository.findUnreadByUser(userId).size();
    }
    
    @Override
    public NotificationDto updateNotification(Long id, NotificationDto notificationDTO) {
        log.info("Updating notification with ID: {}", id);
        Notifications notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        if (notificationDTO.getMessage() != null) {
            notification.setMessage(notificationDTO.getMessage());
        }
        if (notificationDTO.getType() != null) {
            notification.setType(NotificationType.valueOf(notificationDTO.getType()));
        }
        if (notificationDTO.getUserIds() != null) {
            notification.setUserIds(notificationDTO.getUserIds());
        }
        
        Notifications updatedNotification = notificationRepository.save(notification);
        return convertToDTO(updatedNotification);
    }
    
    // Helper method to convert entity to DTO
    private NotificationDto convertToDTO(Notifications notification) {
    	NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setUserIds(notification.getUserIds());
        dto.setType(notification.getType().toString());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
}