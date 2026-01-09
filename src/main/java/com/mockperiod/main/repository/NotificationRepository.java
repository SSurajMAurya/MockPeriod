package com.mockperiod.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.NotificationType;
import com.mockperiod.main.entities.Notifications;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {
	
	 // Find all notifications by type
    List<Notifications> findByType(NotificationType type);
    
    // Find all notifications for a specific user (direct assignment)
    List<Notifications> findByUserId(Long userId);
    
    // Find notifications where user is in userIds list
    @Query("SELECT n FROM Notifications n WHERE :userId MEMBER OF n.userIds")
    List<Notifications> findByUserInUserIds(@Param("userId") Long userId);
    
    // Find all notifications for user (either direct userId or in userIds list)
    @Query("SELECT n FROM Notifications n WHERE n.userId = :userId OR :userId MEMBER OF n.userIds")
    List<Notifications> findAllByUser(@Param("userId") Long userId);
    
    // Find by type and userId
    List<Notifications> findByTypeAndUserId(NotificationType type, Long userId);
    
    // Find by multiple user IDs
    @Query("SELECT n FROM Notifications n WHERE n.userId IN :userIds")
    List<Notifications> findByUserIdIn(@Param("userIds") List<Long> userIds);
    
    // Find unread notifications for user
    @Query("SELECT n FROM Notifications n WHERE (n.userId = :userId OR :userId MEMBER OF n.userIds) AND n.isRead = false")
    List<Notifications> findUnreadByUser(@Param("userId") Long userId);
    
    // Find by type and user is in userIds list
    @Query("SELECT n FROM Notifications n WHERE :userId MEMBER OF n.userIds AND n.type = :type")
    List<Notifications> findByTypeAndUserInUserIds(@Param("type") NotificationType type, @Param("userId") Long userId);

}
