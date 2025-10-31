package com.mockperiod.main.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionListDTO {
	
	private Long id;
    private String questionText;
    private String questionImageUrl;
    private Integer questionNumber;
    private Integer marks;
    private String testTitle;
    private Boolean hasImage;
    private LocalDateTime createdAt;

}
