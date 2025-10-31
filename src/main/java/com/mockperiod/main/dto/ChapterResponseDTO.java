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
public class ChapterResponseDTO {
	
	 private Long id;
	    private String name;
	    private String description;
	    private Long subjectId;
	    private String subjectName;
//	    private SubjectCategory subjectCategory;
	    private Long questionCount;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;

}
