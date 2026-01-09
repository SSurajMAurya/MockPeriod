package com.mockperiod.main.service;

import java.util.List;

import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.entities.TestRanking;

public interface TestRankingService {
	
	Integer calculateAndSaveRank(Long testId, Long studentId, Double totalMarks, 
            Integer correctAnswers, String timeSpent, ExamType examType);
	Integer getCurrentRank(Long testId, Long studentId);
	List<TestRanking> getTopRankings(Long testId, int limit);
	boolean hasStudentTakenTest(Long testId, Long studentId);
	
	List<TestRanking> getRankingsWithTieInfo(Long testId);
	
	 public void recalculateAllRanksForTest(Long testId);
	
//	 Integer calculateAndSaveRank(Long testId, Long studentId, Double totalMarks, 
//	            Integer correctAnswers, String timeSpent, String examType);

}
