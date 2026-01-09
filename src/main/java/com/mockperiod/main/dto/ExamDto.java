package com.mockperiod.main.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor
@Builder
public class ExamDto {
	
	private Long id;

	private String examName;

	private String description;
	
	private List<Long> subjectIds;
	
	private List<String> subjectNames;
	
	private String examImageUrl;
	
	private MultipartFile examImage;

}
