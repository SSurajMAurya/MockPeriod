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
public class CreateQuestionRequest {
	
	private String questionText;
    private MultipartFile questionImage;
    private Long testId;
    private Long subjectId;
    private Long chapterId;
    private Integer questionNumber;
    private List<OptionDto> options;

}
