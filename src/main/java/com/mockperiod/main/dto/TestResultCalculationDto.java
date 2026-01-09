package com.mockperiod.main.dto;

import com.mockperiod.main.entities.Tests;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestResultCalculationDto {
    private Long testId;
    private Long studentId;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer incorrectCount;
    private Integer unansweredCount;
    private List<Long> correctQuestionIds;
    private List<Long> incorrectQuestionIds;
    private List<Long> unansweredQuestionIds;
    private Double correctMarks;
    private Double negativeMarks;
    private Double totalObtainedMarks;
    private Double accuracy;
    private String performanceLevel;
    private Tests test;
    private Long selectedLanguageSubjectId;
}