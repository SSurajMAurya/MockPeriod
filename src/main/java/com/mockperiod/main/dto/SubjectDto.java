package com.mockperiod.main.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectDto {

	private Long id;

	private String name;
	
//	 private Long examId;

	private List<ChapterDto> chapters;

}
