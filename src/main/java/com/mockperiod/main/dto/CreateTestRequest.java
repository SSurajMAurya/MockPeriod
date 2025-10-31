package com.mockperiod.main.dto;

import com.mockperiod.main.entities.Language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTestRequest  {

	private String title;
	private Long examId;
	private Integer durationMinutes;
//	private SubjectCategory subjectCategory;
	private Long chapterId;
	private Double correctMark;
	private Double negativeMark;
	private Language language;

}
