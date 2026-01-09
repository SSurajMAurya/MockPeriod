package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.mockperiod.main.entities.ExamType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentPerformanceDto {
    
    // Basic identifiers
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long testId;
    private String testName;
    private Long examId;
    private String examName;
    private Long subjectId;
    private String subjectName;
    private String instituteEmail;
    
    // Performance metrics
    private Double marksObtained;
    private Double totalMarks;
    private Double percentage;
    private Integer rank;
    private Integer totalParticipants;
    private Double averageMarks;
    private Double highestMarks;
    private Double lowestMarks;
    private Double improvementPercentage;
    private ExamType examType;
    
    // Subject-wise performance
    private Map<String, SubjectPerformance> subjectPerformance;
    
    // Test history
    private List<TestHistory> testHistory;
    
    // Leaderboard data
    private List<RankEntry> leaderboard;
    
    // Dates
    private LocalDate testDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    // Additional metadata
    private String performanceType; // EXAM_WISE, SUBJECT_WISE, OVERALL
    private String analysisType; // RANKING, PERFORMANCE, LEADERBOARD, SUBJECT_ANALYSIS
    
    // Nested classes for complex data
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestHistory {
        private Long testId;
        private String testName;
        private Double marksObtained;
        private Double percentage;
        private Integer rank;
        private LocalDate testDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankEntry {
//        private Integer rank;
//        private Long studentId;
//        private String studentName;
//        private String studentEmail;
//        private Double marksObtained;
//        private Double percentage;
    	
    	 private Integer rank;
         private Long studentId;
         private String studentName;
         private String studentEmail;
         private Double marksObtained;
         private String timeSpent;
         private Integer noOfCorrectAnswers;
         private Integer noOfIncorrectAnswers;
         private Integer totalTestsTaken;
         private Double averageMarks;
         private Double highestMarks;
         private Double lowestMarks;
         private Double accuracy;
         private Integer totalCorrectAnswers;
         private Integer totalIncorrectAnswers;
         private Long testId;
         private String testName;
         private LocalDate testAttemptedDate;
         private String remark;
         private Long selectedLanguageSubjectId;
         private String selectedLanguageSubjectName;
    }
    
  
}