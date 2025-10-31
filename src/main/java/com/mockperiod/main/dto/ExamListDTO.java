package com.mockperiod.main.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamListDTO {
	
	 private Long id;
	    private String name;
	    private String description;
	    private Long testCount;
	    private LocalDateTime createdAt;

}
