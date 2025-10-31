package com.mockperiod.main.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateQuestionRequest {
	
	 @Size(max = 2000, message = "Question text must not exceed 2000 characters")
	    private String questionText;

	    private Long subjectId;
	    private Long chapterId;

	    @Min(value = 1, message = "Question number must be at least 1")
	    private Integer questionNumber;

	    @Min(value = 0, message = "Marks must be non-negative")
	    private Integer marks;

	    @Valid
	    @Size(min = 2, max = 6, message = "There must be between 2 and 6 options")
	    private List<UpdateOptionRequest> options;

}
