package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLeaderboardDto {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long testId;
    private String testName;
    private String examType;
    private Double studentMarks;
    private Integer studentRank;
    private Integer totalParticipants;
    private Double totalMarks;
    private Double averageMarks;
    private Double highestMarks;
    private Double lowestMarks;
    private LocalDate testDate;
    private String timeSpent;
    private List<StudentRankEntry> leaderboard;
    private String instituteEmail;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentRankEntry {
        private Integer rank;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private Double marksObtained;
        private Integer noOfCorrectAnswers;
        private Integer noOfIncorrectAnswers;
        private String timeSpent;
        private String remark;
        private Long selectedLanguageSubjectId;
        private String selectedLanguageSubjectName;
        private String attemptedTestLanguage;
        private Boolean isCurrentStudent; // Flag to identify current student
    }
}
