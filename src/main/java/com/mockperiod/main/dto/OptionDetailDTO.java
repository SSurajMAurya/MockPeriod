package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionDetailDTO {

	  private Long id;
	    private String optionText;
	    private String optionImageUrl;
	    private Integer optionNumber;
	    private Boolean isCorrect;
	    private Long questionId;
	    private String questionText;
}
