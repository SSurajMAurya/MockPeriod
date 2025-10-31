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
public class SubjectResponseDTO {
	
	 private Long id;
	    private String name;
//	    private SubjectCategory category;
	    private String description;
	    private Long chapterCount;
	    private Long questionCount;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;

}
