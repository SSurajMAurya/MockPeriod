package com.mockperiod.main.dto;

import java.time.LocalDateTime;

import com.mockperiod.main.entities.Language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestResponseDto {
	
	  private Long id;
	    private String title;
	    private String examName;
	    private Integer durationMinutes;
//	    private SubjectCategory subjectCategory;
	    private String chapterName;
	    private Double correctMark;
	    private Double negativeMark;
	    private Language language;
	    private Integer totalQuestions;
	    private LocalDateTime createdAt;

}
