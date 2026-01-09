package com.mockperiod.main.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification") 
public class Notifications {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    
	    private Long userId;
	    
	    @ElementCollection
	    @CollectionTable(name = "notification_user_ids", 
	                     joinColumns = @JoinColumn(name = "notification_id"))
	    @Column(name = "user_id")
	    private List<Long> userIds;
	    
	    @Enumerated(EnumType.STRING)
	    private NotificationType type;
	    
	    @Column(length = 1000)
	    private String message;
	    
	    private boolean isRead = false;
	    
	    @CreationTimestamp
	    @Column(updatable = false)
	    private LocalDateTime createdAt;
	    
	    @UpdateTimestamp
	    private LocalDateTime updatedAt;

}
