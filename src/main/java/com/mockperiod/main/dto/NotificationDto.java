package com.mockperiod.main.dto;

import com.mockperiod.main.entities.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDto {
    
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    
   
    private Long userId;
    private List<Long> userIds;
    private String type;
    private String message;
    
  
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRead;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updatedAt;
    
    
}
