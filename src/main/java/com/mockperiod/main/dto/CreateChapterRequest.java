package com.mockperiod.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateChapterRequest {
	
	 @NotBlank(message = "Chapter name is required")
	    @Size(max = 255, message = "Chapter name must not exceed 255 characters")
	    private String name;

	    @NotNull(message = "Subject ID is required")
	    private Long subjectId;

	    @Size(max = 1000, message = "Description must not exceed 1000 characters")
	    private String description;

}
