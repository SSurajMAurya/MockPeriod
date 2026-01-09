package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.mockperiod.main.entities.ExamType;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestSubmissionDto {
	
    private Long testId;
    private Long studentId;
    private String instituteEmail;
    private List<QuestionAttemptDto> questionAttempts;
    private Integer totalTimeSpent; // in seconds (optional)
    
    private Long selectedLanguageSubjectId;
    private Integer currentRank;
    private Integer totalParticipants;
    private String timeSpent;
    private ExamType examType;
//    private String language;
    private String testAttemptedLanguage;

}