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
public class TestFilter {
	
	 private Long examId;
//	    private SubjectCategory subjectCategory;
	    private LocalDateTime createdAfter;
	    private LocalDateTime createdBefore;
	    private String searchKeyword;

}
