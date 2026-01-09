// QuestionDto - Remove MultipartFile from DTO  
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
public class QuestionDto {
    private Long id;
    private String questionText;
    private String questionImageUrl;
    
    private Long testId;
    private List<OptionDto> options;
    private Long subjectId;
    private Long chapterId;
    private Integer questionNumber;
    private Double marks;
    private String language;
    private String subject;
    
    
    private Boolean isSubjectHeader;
    private String subjectName;
    private Integer questionCount;
    
    // For backward compatibility
    public Boolean getIsSubjectHeader() {
        return isSubjectHeader != null ? isSubjectHeader : false;
    }
    
    @JsonIgnore
    private transient MultipartFile questionImage;
    
}

