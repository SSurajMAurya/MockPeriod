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
public class TestSummaryDTO {
	
	 private Long testId;
	    private String title;
	    private String examName;
	    private Long totalQuestions;
	    private Long questionsWithImages;
	    private Double totalMarks;
	    private Double averageMarksPerQuestion;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;

}
