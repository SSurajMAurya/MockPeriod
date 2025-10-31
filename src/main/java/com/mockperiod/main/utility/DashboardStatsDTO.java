package com.mockperiod.main.utility;

import java.util.Map;

import com.mockperiod.main.entities.Language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDTO {
	
	 private Long totalTests;
	    private Long totalQuestions;
	    private Long totalExams;
	    private Long totalSubjects;
	    private Long todayTests;
	    private Long todayQuestions;
//	    private Map<SubjectCategory, Long> questionsByCategory;
	    private Map<Language, Long> testsByLanguage;

}
