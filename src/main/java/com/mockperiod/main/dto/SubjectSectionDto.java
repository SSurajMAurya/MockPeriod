package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectSectionDto {
    private Long subjectId;
    private String subjectName;
    private List<QuestionDto> questions;
    private Integer questionCount;
    private Double totalMarks;
}
