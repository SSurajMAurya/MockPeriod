package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.entities.Language;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentTestResultDto {
    private Long id;
    private Long testId;
    private String testName;
    private Long studentId;
    private String studentEmail;
    private String instituteEmail;
    private Double noOfCorrectAns;
    private List<Long> incurrectquestionIds;
    private Double noOfInCorrectAns;
    private List<Long> currectquestionIds;
    
    // For backward compatibility - contains only correct questions
    private List<QuestionDto> questionDtos;
    
    // New fields for separated questions
    private List<QuestionDto> correctQuestions;
    private List<QuestionDto> incorrectQuestions;
    private List<QuestionDto> unansweredQuestions;
    
    private Double totalObtainedMarks;
    private String testAttemptedDate;
    private String remark;
//    private String attemptedTestLanguage;
    
    // Ranking information
    private Integer currentRank;
    private Integer totalParticipants;
    private String timeSpent;
    private ExamType examType;
    
    
    private Long selectedLanguageSubjectId;
    private String selectedLanguageSubjectName;
    private String testAttemptedLanguage;
    
    
    // New additional fields
    private Double totalMarks;  // Added
    private Double negativeMarks; 
    
    private Double highestMarks;
    private Double lowestMarks;
    private Integer totalTests;
//    private String attemptedTestLanguage;
    // Added (if you want to show negative marks separately)
   
    
    // Add these getter methods for calculated fields
    public Double getTotalMarks() {
        if (correctQuestions != null && incorrectQuestions != null && unansweredQuestions != null) {
            int totalQuestions = correctQuestions.size() + incorrectQuestions.size() + unansweredQuestions.size();
            // Assuming each question has 1 mark - adjust based on your logic
            return (double) totalQuestions;
        }
        return null;
    }
    
    public Double getNegativeMarks() {
        if (noOfInCorrectAns != null) {
            // Assuming 0.25 negative marks per wrong answer - adjust based on your test
            return noOfInCorrectAns * 0.25;
        }
        return null;
    }
    
}