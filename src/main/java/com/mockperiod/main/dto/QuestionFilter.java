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
public class QuestionFilter {
	
	  private Long testId;
	    private Long subjectId;
	    private Long chapterId;
	    private Boolean hasImage;
	    private String searchKeyword;
	    private LocalDateTime createdAfter;
	    private LocalDateTime createdBefore;

}
