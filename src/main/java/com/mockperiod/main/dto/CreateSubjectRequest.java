package com.mockperiod.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSubjectRequest {
	
	 @NotBlank(message = "Subject name is required")
	    @Size(max = 255, message = "Subject name must not exceed 255 characters")
	    private String name;

//	    @NotNull(message = "Subject category is required")
//	    private SubjectCategory category;

	    @Size(max = 1000, message = "Description must not exceed 1000 characters")
	    private String description;

}
