package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateExamRequest {
	
	private Long id;

	private String domainName;

	private String description;

}
