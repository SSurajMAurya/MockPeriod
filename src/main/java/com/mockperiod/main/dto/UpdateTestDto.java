package com.mockperiod.main.dto;

import com.mockperiod.main.entities.Language;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTestDto {

	  @Size(max = 255, message = "Title must not exceed 255 characters")
	    private String title;

	    @Min(value = 1, message = "Duration must be at least 1 minute")
	    private Integer durationMinutes;

//	    private SubjectCategory subjectCategory;
	    private Long chapterId;

	    @PositiveOrZero(message = "Correct mark must be positive or zero")
	    private Double correctMark;

	    @NegativeOrZero(message = "Negative mark must be negative or zero")
	    private Double negativeMark;

	    private Language language;
}
