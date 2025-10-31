package com.mockperiod.main.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateOptionRequest {
	
	 @NotBlank(message = "Option text is required")
	    @Size(max = 1000, message = "Option text must not exceed 1000 characters")
	    private String optionText;

	    @NotNull(message = "Option number is required")
	    @Min(value = 1, message = "Option number must be at least 1")
	    @Max(value = 6, message = "Option number must not exceed 6")
	    private Integer optionNumber;

	    @NotNull(message = "IsCorrect flag is required")
	    private Boolean isCorrect;

}
