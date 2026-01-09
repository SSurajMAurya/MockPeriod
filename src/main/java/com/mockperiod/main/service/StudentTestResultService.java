package com.mockperiod.main.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.dto.PerformanceAnalyticsDto;
import com.mockperiod.main.dto.RankingResponseDto;
import com.mockperiod.main.dto.StudentLeaderboardDto;
import com.mockperiod.main.dto.StudentPerformanceDto;
import com.mockperiod.main.dto.StudentTestResultDto;
import com.mockperiod.main.dto.TestNameDto;
import com.mockperiod.main.dto.TestResultCalculationDto;
import com.mockperiod.main.dto.TestSubmissionDto;
import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.entities.StudentTestResult;

public interface StudentTestResultService {

    StudentTestResultDto createTestResult(StudentTestResultDto testResultDto);
    
    List<StudentTestResultDto> getAllTestResult();
    
    List<StudentTestResultDto> getAllTestResultByInstitute(String instituteEmail);
    
    void deleteTestResult(Long id);
    
    List<StudentTestResultDto> getAllTestResultByDateAndInstitute(LocalDate date, String instituteEmail);
    
    List<StudentTestResultDto> getTestResultByStudentAndInstitute(Long studentId, String instituteEmail);
    
    StudentTestResultDto getTestResultById(Long id);
    
    List<StudentTestResultDto> getTestResultByStudentId(Long studentId);
    
    StudentTestResultDto getTestResultByStudentIdAndTestId(Long studentId , Long testId);
    
    
    StudentPerformanceDto getRanking(String rankingType, Long referenceId, String instituteEmail, Integer topN);
    StudentPerformanceDto getStudentPerformance(Long studentId, String performanceType, Long referenceId, String instituteEmail);
    StudentPerformanceDto getSubjectPerformanceInExam(Long studentId, Long testId);
    StudentPerformanceDto getStudentLastExamRank(Long studentId, String instituteEmail);
    StudentPerformanceDto getTestLeaderboard(Long testId, Integer topN);
    StudentPerformanceDto getSubjectAveragesForExam(Long testId);
    
    StudentTestResultDto submitTestAndCalculateResult(TestSubmissionDto testSubmissionDto);
    TestResultCalculationDto calculateTestResult(TestSubmissionDto testSubmissionDto);
    
    
    
 List<RankingResponseDto> getTopRankListForLatestTest(String instituteEmail, Integer topN);
    
    List<RankingResponseDto> getTopRanksForTest(Long testId, Integer topN);
    
    PerformanceAnalyticsDto getMonthlyPerformance(Long studentId, LocalDate monthDate);
    
    PerformanceAnalyticsDto getWeeklyPerformance(Long studentId, LocalDate weekStart);
    PerformanceAnalyticsDto getLastMonthPerformance(Long studentId);
    PerformanceAnalyticsDto getLastMonthTopRanks(String instituteEmail, Integer topN);
    
    StudentPerformanceDto getLastTestLeaderboardForInstitute(String instituteEmail, Integer topN, Boolean detailed);
    StudentPerformanceDto getOverallInstituteLeaderboard(String instituteEmail, Integer topN, 
                                                        LocalDate startDate, LocalDate endDate);
    
     List<TestNameDto> getTestListByStudentIdExamType(Long studentId , ExamType examType);
     
  // Add this method to StudentTestResultService interface
     StudentLeaderboardDto getStudentLeaderboardForLastAttemptedTest(Long studentId, String instituteEmail, Integer topN, Boolean includeStudent);
    
    
   
    
    
    
}