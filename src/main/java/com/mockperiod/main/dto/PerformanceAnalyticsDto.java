package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAnalyticsDto {
    
    // Common fields
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String instituteEmail;
    
    // Time period information
    private String periodType; // "MONTHLY", "WEEKLY", "DAILY", "CUSTOM"
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel; // e.g., "January 2024", "Week 1", "Today"
    
    // Performance metrics
    private Integer totalTests;
    private Double totalMarksObtained;
    private Double averageMarks;
    private Double highestMarks;
    private Double lowestMarks;
    private Double improvementPercentage;
    
    // Breakdown data (flexible structure)
    private List<PerformanceBreakdown> breakdown;
    
    // Test history
    private List<TestResultSummary> testHistory;
    
    // Ranking information
    private Integer currentRank;
    private Integer totalParticipants;
    private List<RankEntry> topRankers;
    
    // Subject-wise performance
    private Map<String, SubjectPerformance> subjectPerformance;
    
    // Nested DTO classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceBreakdown {
        private String periodLabel; // e.g., "Week 1", "Monday", "Physics"
        private Integer testCount;
        private Double averageMarks;
        private Double totalMarks;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Double accuracy;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultSummary {
        private Long testId;
        private String testName;
        private Double marksObtained;
        private Double totalMarks;
        private LocalDate testDate;
        private Integer rank;
        private String examType;
        private String timeSpent;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankEntry {
        private Integer rank;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private Double marksObtained;
        private Integer correctAnswers;
        private String timeSpent;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectPerformance {
        private Double averageMarks;
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Double accuracy;
        private String strengthLevel;
    }
}