// OptionDto - Remove MultipartFile from DTO
package com.mockperiod.main.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionDto {
    private Long id;
    private String optionText;
    private String optionImageUrl;
    private Long questionId;
    private Integer optionNumber;
    private Boolean isCorrect = false;
    
    @JsonIgnore
    private transient List<MultipartFile> optionImage;
    
}