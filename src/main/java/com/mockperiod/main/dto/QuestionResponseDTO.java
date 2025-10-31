package com.mockperiod.main.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class QuestionResponseDTO {

	private Long id;
	private String questionText;
	private String questionImageUrl;
	private Integer questionNumber;
	private Integer marks;
	private List<OptionResponseDTO> options;
	private LocalDateTime createdAt;
}
