package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChapterDto {

	private Long id;

	private String name;

	private Long subjectId;

	private String description;

//	private List<Questions> questions;

}
