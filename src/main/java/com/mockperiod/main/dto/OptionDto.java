package com.mockperiod.main.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionDto {
	
	private String optionText;
    private MultipartFile optionImage;
    private Integer optionNumber;
    private Boolean isCorrect;

}
