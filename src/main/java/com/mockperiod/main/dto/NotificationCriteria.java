package com.mockperiod.main.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationCriteria {
	
	 private String department;
	    private String role;
	    private String location;
	    private Set<String> permissions;
	    private LocalDateTime lastActiveAfter;
	    

}




