package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingResponseDto {
    private Long testId;
    private Long studentId;
//    private String StudentName;
    private String studentEmail;
    private Integer rank;
    private Double totalMarks;
    private Integer correctAnswers;
    private String timeSpent;
    private String examType;
    private String studentName; // Can be populated if needed
    private String testName;    // Can be populated if needed
}
