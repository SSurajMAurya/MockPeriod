package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestInstituteTimeDto {
	
private Long id;
	
	private Long intituteId;
	
	private Long testId;
	
	private String starDateTime;
	
	private String enDateTime;

}
