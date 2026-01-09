package com.mockperiod.main.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mockperiod.main.dto.PerformanceAnalyticsDto;
import com.mockperiod.main.dto.StudentLeaderboardDto;
import com.mockperiod.main.dto.StudentPerformanceDto;
import com.mockperiod.main.dto.StudentTestResultDto;
import com.mockperiod.main.dto.TestNameDto;
import com.mockperiod.main.dto.TestResultCalculationDto;
import com.mockperiod.main.dto.TestSubmissionDto;
import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.entities.Language;
import com.mockperiod.main.entities.StudentTestResult;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.service.StudentTestResultService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/test-results")
@RequiredArgsConstructor
@Slf4j
public class StudentTestResultController {

    private final StudentTestResultService testResultService;

    @PostMapping
    public ResponseEntity<?> createTestResult(@RequestBody StudentTestResultDto testResultDto) {
        try {
            StudentTestResultDto createdResult = testResultService.createTestResult(testResultDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdResult);
        } catch (CustomException e) {
        	log.error("Custom exception in createTestResult: {}", e.getMessage());
           throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
        	log.error("Custom exception in createTestResult: {}", e.getMessage());
           throw new RuntimeException("Error creating test Result");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTestResults() {
        try {
            List<StudentTestResultDto> testResults = testResultService.getAllTestResult();
            return ResponseEntity.ok(testResults);
        } catch (Exception e) {
        	throw new RuntimeException("Error retriving test Result");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTestResultById(@PathVariable Long id) {
        try {
            StudentTestResultDto testResult = testResultService.getTestResultById(id);
            return ResponseEntity.ok(testResult);
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test result"));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getTestResultsByStudent(@PathVariable Long studentId) {
        try {
            List<StudentTestResultDto> testResults = testResultService.getTestResultByStudentId(studentId);
            return ResponseEntity.ok(testResults);
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test results for student"));
        }
    }

    @GetMapping("/institute")
    public ResponseEntity<?> getTestResultsByInstitute(@RequestParam String instituteEmail) {
        try {
            List<StudentTestResultDto> testResults = testResultService.getAllTestResultByInstitute(instituteEmail);
            return ResponseEntity.ok(testResults);
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test results for institute"));
        }
    }

    @GetMapping("/institute/date")
    public ResponseEntity<?> getTestResultsByDateAndInstitute(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String instituteEmail) {
        try {
            List<StudentTestResultDto> testResults = testResultService.getAllTestResultByDateAndInstitute(date, instituteEmail);
            return ResponseEntity.ok(testResults);
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test results by date and institute"));
        }
    }

    @GetMapping("/student-institute")
    public ResponseEntity<?> getTestResultsByStudentAndInstitute(
            @RequestParam Long studentId,
            @RequestParam String instituteEmail) {
        try {
            List<StudentTestResultDto> testResults = testResultService.getTestResultByStudentAndInstitute(studentId, instituteEmail);
            return ResponseEntity.ok(testResults);
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test results by student and institute"));
        }
    }
    
    @GetMapping("/student/{studentId}/test/{testId}")
    public ResponseEntity<?> getTestResultByStudentAndTest(
            @PathVariable Long studentId,
            @PathVariable Long testId) {
        try {
            StudentTestResultDto testResult = testResultService.getTestResultByStudentIdAndTestId(studentId, testId);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            throw new CustomException("error fetching the test result", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}") 
    public ResponseEntity<?> deleteTestResult(@PathVariable Long id) {
        try {
            testResultService.deleteTestResult(id);
            return ResponseEntity.ok().body(createSuccessResponse("Test result deleted successfully"));
        } catch (CustomException e) {
        	throw new CustomException("Error creating test Result", null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting test result"));
        }
    }

    private ErrorResponse createErrorResponse(String message) {
        return new ErrorResponse(message);
    }
    
    private SuccessResponse createSuccessResponse(String message) {
        return new SuccessResponse(message);
    }

    // Inner classes for response formatting
    private static class ErrorResponse {
        private String error;
        private boolean success = false;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    private static class SuccessResponse {
        private String message;
        private boolean success = true;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }
    
    
    
    
//    @GetMapping("/student/{studentId}/performance/monthly")
//    public ResponseEntity<?> getStudentMonthlyPerformance(
//            @PathVariable Long studentId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
//        
//        try {
//            log.info("Getting monthly performance for student: {}", studentId);
//            
//            // If month is not provided, use current month
//            LocalDate targetMonth = (month != null) ? month : LocalDate.now();
//            
//            // Calculate month start and end dates
//            LocalDate startDate = targetMonth.withDayOfMonth(1);
//            LocalDate endDate = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());
//            
//            // Get all test results for the student in the specified month
//            List<StudentTestResult> monthlyResults = testResultRepository
//                    .findByStudentIdAndTestAttemptedDateBetween(studentId, startDate, endDate);
//            
//            if (monthlyResults.isEmpty()) {
//                return ResponseEntity.ok(createSuccessResponse(
//                        "No test results found for the specified month",
//                        createEmptyMonthlyPerformanceDto(studentId, startDate)));
//            }
//            
//            // Calculate monthly performance metrics
//            MonthlyPerformanceDto performanceDto = calculateMonthlyPerformance(studentId, monthlyResults, startDate);
//            
//            return ResponseEntity.ok(createSuccessResponse(
//                    "Monthly performance fetched successfully", performanceDto));
//            
//        } catch (CustomException e) {
//            log.error("Custom exception: {}", e.getMessage());
//            throw new CustomException(e.getMessage(), e.getStatus());
//        } catch (Exception e) {
//            log.error("Unexpected error: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching monthly performance: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    
    
    
    
    @GetMapping("/institute/{instituteEmail}/last-test-leaderboard")
    public ResponseEntity<?> getLastTestLeaderboardForInstitute(
            @PathVariable String instituteEmail,
            @RequestParam(required = false) Integer topN,
            @RequestParam(required = false) Boolean detailed) {
        
        try {
            log.info("Getting last test leaderboard for institute: {}, detailed: {}", instituteEmail, detailed);
            
            // Use the service method
            StudentPerformanceDto leaderboard = testResultService.getLastTestLeaderboardForInstitute(
                    instituteEmail, 
                    topN != null ? topN : 10, // Default top 10
                    detailed != null ? detailed : false
            );
            
            return ResponseEntity.ok(createSuccessResponse(
                    "Last test leaderboard fetched successfully", 
                    leaderboard));
            
        } catch (CustomException e) {
            log.error("Custom exception in getLastTestLeaderboardForInstitute: {}", e.getMessage());
            throw new CustomException("Error fetching leaderboard: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getLastTestLeaderboardForInstitute: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching last test leaderboard: " + e.getMessage()));
        }
    }
    
    
    
 // 1. Get Rankings (Exam-wise or Subject-wise)
    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(
            @RequestParam String type, // "EXAM" or "SUBJECT"
            @RequestParam Long referenceId, // examId or subjectId
            @RequestParam String instituteEmail,
            @RequestParam(required = false) Integer topN) {
        
        try {
            log.info("Getting {} ranking for referenceId: {}, institute: {}", type, referenceId, instituteEmail);
            
            StudentPerformanceDto ranking = testResultService.getRanking(type, referenceId, instituteEmail, topN);
            
            return ResponseEntity.ok(createSuccessResponse("Ranking fetched successfully", ranking));
            
        } catch (CustomException e) {
            log.error("Custom exception in getRanking: {}", e.getMessage());
            throw new CustomException("Error: "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getRanking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching ranking: " + e.getMessage()));
        }
    }

    // 2. Get Student Performance (Exam-wise or Subject-wise)
    @GetMapping("/student/{studentId}/performance")
    public ResponseEntity<?> getStudentPerformance( 
            @PathVariable Long studentId,
            @RequestParam String type, // "EXAM" or "SUBJECT"
            @RequestParam Long referenceId, // examId or subjectId
            @RequestParam String instituteEmail) {
        
        try {
            log.info("Getting {} performance for studentId: {}, referenceId: {}", type, studentId, referenceId);
            
            StudentPerformanceDto performance = testResultService.getStudentPerformance(
                    studentId, type, referenceId, instituteEmail);
            
            return ResponseEntity.ok(createSuccessResponse("Performance data fetched successfully", performance));
            
        } catch (CustomException e) {
            log.error("Custom exception in getStudentPerformance: {}", e.getMessage());
            throw new CustomException("Error: "
            		+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getStudentPerformance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching performance data: " + e.getMessage()));
        }
    }

    // 3. Get Subject Performance in Specific Exam
    @GetMapping("/student/{studentId}/test/{testId}/subjects")
    public ResponseEntity<?> getSubjectPerformanceInExam(
            @PathVariable Long studentId,
            @PathVariable Long testId) {
        
        try {
            log.info("Getting subject performance for studentId: {}, testId: {}", studentId, testId);
            
            StudentPerformanceDto subjectPerformance = testResultService.getSubjectPerformanceInExam(studentId, testId);
            
            return ResponseEntity.ok(createSuccessResponse("Subject performance fetched successfully", subjectPerformance));
            
        } catch (CustomException e) {
            log.error("Custom exception in getSubjectPerformanceInExam: {}", e.getMessage());
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getSubjectPerformanceInExam: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching subject performance: " + e.getMessage()));
        }
    }

    // 4. Get Student's Last Exam Rank
    @GetMapping("/student/{studentId}/last-exam-rank")
    public ResponseEntity<?> getStudentLastExamRank(
            @PathVariable Long studentId,
            @RequestParam String instituteEmail) {
        
        try {
            log.info("Getting last exam rank for studentId: {}, institute: {}", studentId, instituteEmail);
            
            StudentPerformanceDto lastExamRank = testResultService.getStudentLastExamRank(studentId, instituteEmail);
            
            return ResponseEntity.ok(createSuccessResponse("Last exam rank fetched successfully", lastExamRank));
            
        } catch (CustomException e) {
            log.error("Custom exception in getStudentLastExamRank: {}", e.getMessage());
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getStudentLastExamRank: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching last exam rank: " + e.getMessage()));
        }
    }

    // 5. Get Test Leaderboard
    @GetMapping("/test/{testId}/leaderboard")
    public ResponseEntity<?> getTestLeaderboard(
            @PathVariable Long testId,
            @RequestParam(required = false) Integer topN) {
        
        try {
            log.info("Getting leaderboard for testId: {}, top: {}", testId, topN);
            
            StudentPerformanceDto leaderboard = testResultService.getTestLeaderboard(testId, topN);
            
            return ResponseEntity.ok(createSuccessResponse("Leaderboard fetched successfully", leaderboard));
            
        } catch (CustomException e) {
            log.error("Custom exception in getTestLeaderboard: {}", e.getMessage());
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getTestLeaderboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching leaderboard: " + e.getMessage()));
        }
    }

    // 6. Get Subject Averages for Exam
    @GetMapping("/test/{testId}/subject-averages")
    public ResponseEntity<?> getSubjectAveragesForExam(@PathVariable Long testId) {
        
        try {
            log.info("Getting subject averages for testId: {}", testId);
            
            StudentPerformanceDto subjectAverages = testResultService.getSubjectAveragesForExam(testId);
            
            return ResponseEntity.ok(createSuccessResponse("Subject averages fetched successfully", subjectAverages));
            
        } catch (CustomException e) {
            log.error("Custom exception in getSubjectAveragesForExam: {}", e.getMessage());
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getSubjectAveragesForExam: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching subject averages: " + e.getMessage()));
        }
    }

    // 7. Get Student's Overall Performance Summary
    @GetMapping("/student/{studentId}/institute/{instituteEmail}/summary")
    public ResponseEntity<?> getStudentPerformanceSummary(
            @PathVariable Long studentId,
            @PathVariable String instituteEmail,
            @RequestParam(required = false) String period) { // "WEEK", "MONTH", "YEAR"
        
        try {
            log.info("Getting performance summary for studentId: {}, institute: {}, period: {}", 
                    studentId, instituteEmail, period);
            
            // This would combine multiple service calls for a comprehensive summary
            StudentPerformanceDto summary = getComprehensiveStudentSummary(studentId, instituteEmail, period);
            
            return ResponseEntity.ok(createSuccessResponse("Performance summary fetched successfully", summary));
            
        } catch (CustomException e) {
            log.error("Custom exception in getStudentPerformanceSummary: {}", e.getMessage());
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getStudentPerformanceSummary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching performance summary: " + e.getMessage()));
        }
    }

    // 8. Get Institute-wide Analytics
    @GetMapping("/institute/{instituteEmail}/analytics/{startDate}/{endDate}")
    public ResponseEntity<?> getInstituteAnalytics(
            @PathVariable String instituteEmail,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            
            @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            
            {
        
        try {
            log.info("Getting analytics for institute: {}, date range: {} to {}", instituteEmail, startDate, endDate);
            
            // This would provide institute-wide analytics
            StudentPerformanceDto analytics = getInstituteAnalyticsData(instituteEmail, startDate, endDate);
            
            return ResponseEntity.ok(createSuccessResponse("Institute analytics fetched successfully", analytics));
            
        } catch (CustomException e) {
            log.error("Custom exception in getInstituteAnalytics: {}", e.getMessage());
//            return ResponseEntity.status(e.getStatus()).body(createErrorResponse(e.getMessage()));
            throw new CustomException("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in getInstituteAnalytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching institute analytics: " + e.getMessage()));
        }
    }

    // Helper method for comprehensive student summary
    private StudentPerformanceDto getComprehensiveStudentSummary(Long studentId, String instituteEmail, String period) {
        // Get last exam rank
        StudentPerformanceDto lastRank = testResultService.getStudentLastExamRank(studentId, instituteEmail);
        
        // Get recent test history (last 5 tests)
        List<StudentTestResultDto> recentTests = testResultService.getTestResultByStudentId(studentId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        
        // Convert to test history format
        List<StudentPerformanceDto.TestHistory> testHistory = recentTests.stream()
                .map(test -> StudentPerformanceDto.TestHistory.builder()
                        .testId(test.getTestId())
                        .testName(test.getTestName())
                        .marksObtained(test.getTotalObtainedMarks())
                        .testDate(LocalDate.parse(test.getTestAttemptedDate()))
                        .build())
                .collect(Collectors.toList());
        
        // Build comprehensive summary
        return StudentPerformanceDto.builder()
                .studentId(studentId)
                .studentName(lastRank.getStudentName())
                .studentEmail(lastRank.getStudentEmail())
                .analysisType("COMPREHENSIVE_SUMMARY")
                .performanceType("OVERALL")
                .rank(lastRank.getRank())
                .marksObtained(lastRank.getMarksObtained())
                .totalMarks(lastRank.getTotalMarks())
                .percentage(lastRank.getPercentage())
                .totalParticipants(lastRank.getTotalParticipants())
                .testHistory(testHistory)
                .instituteEmail(instituteEmail)
                .build();
    }

    // Helper method for institute analytics
//    private StudentPerformanceDto getInstituteAnalyticsData(String instituteEmail, LocalDate startDate, LocalDate endDate) {
//        // Get all test results for the institute
//        List<StudentTestResultDto> instituteResults = testResultService.getAllTestResultByInstitute(instituteEmail);
//        
//        // Filter by date range if provided
//        if (startDate != null && endDate != null) {
//            instituteResults = instituteResults.stream()
//                    .filter(result -> {
//                        LocalDate testDate = LocalDate.parse(result.getTestAttemptedDate());
//                        return !testDate.isBefore(startDate) && !testDate.isAfter(endDate);
//                    })
//                    .collect(Collectors.toList());
//        }
//        
//        // Calculate basic analytics
//        long totalTests = instituteResults.size();
//        long totalStudents = instituteResults.stream()
//                .map(StudentTestResultDto::getStudentId)
//                .distinct()
//                .count();
//        
//        double averageMarks = instituteResults.stream()
//                .mapToDouble(StudentTestResultDto::getTotalObtainedMarks)
//                .average()
//                .orElse(0.0);
//        
//        return StudentPerformanceDto.builder()
//                .analysisType("INSTITUTE_ANALYTICS")
//                .performanceType("OVERALL")
//                .totalParticipants((int) totalStudents)
//                .averageMarks(averageMarks)
//                .instituteEmail(instituteEmail)
//                .fromDate(startDate)
//                .toDate(endDate)
//                .build();
//    }
    
    private StudentPerformanceDto getInstituteAnalyticsData(String instituteEmail, LocalDate startDate, LocalDate endDate) {
        // Get all test results for the institute
        List<StudentTestResultDto> instituteResults = testResultService.getAllTestResultByInstitute(instituteEmail);
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            instituteResults = instituteResults.stream()
                    .filter(result -> {
                        try {
                            LocalDate testDate = LocalDate.parse(result.getTestAttemptedDate());
                            return !testDate.isBefore(startDate) && !testDate.isAfter(endDate);
                        } catch (Exception e) {
                            return false; // Skip invalid dates
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        // Check if we have any results
        if (instituteResults.isEmpty()) {
            return StudentPerformanceDto.builder()
                    .analysisType("INSTITUTE_ANALYTICS")
                    .performanceType("OVERALL")
                    .totalParticipants(0)
                    .averageMarks(0.0)
                    .highestMarks(0.0)
                    .lowestMarks(0.0)
                    .instituteEmail(instituteEmail)
                    .fromDate(startDate)
                    .toDate(endDate)
                    .build();
        }
        
        // Calculate basic analytics
        long totalTests = instituteResults.size();
        long totalStudents = instituteResults.stream()
                .map(StudentTestResultDto::getStudentId)
                .distinct()
                .count();
        
        // Calculate average, highest, and lowest marks
        double averageMarks = instituteResults.stream()
                .mapToDouble(result -> result.getTotalObtainedMarks() != null ? result.getTotalObtainedMarks() : 0.0)
                .average()
                .orElse(0.0);
        
        double highestMarks = instituteResults.stream()
                .mapToDouble(result -> result.getTotalObtainedMarks() != null ? result.getTotalObtainedMarks() : 0.0)
                .max()
                .orElse(0.0);
        
        double lowestMarks = instituteResults.stream()
                .mapToDouble(result -> result.getTotalObtainedMarks() != null ? result.getTotalObtainedMarks() : 0.0)
                .min()
                .orElse(0.0);
        
        return StudentPerformanceDto.builder()
                .analysisType("INSTITUTE_ANALYTICS")
                .performanceType("OVERALL")
                .totalParticipants((int) totalStudents)
//                .totalTests((int) totalTests)
                .averageMarks(averageMarks)
                .highestMarks(highestMarks)
                .lowestMarks(lowestMarks)
                .instituteEmail(instituteEmail)
                .fromDate(startDate)
                .toDate(endDate)
                .build();
    }

    // Response helper methods
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

//    private Map<String, Object> createErrorResponse(String message) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", false);
//        response.put("message", message);
//        response.put("timestamp", LocalDateTime.now());
//        return response;
//    }
    
    
    
//    @PostMapping("/submit")
//    public ResponseEntity<?> submitTest(@RequestBody TestSubmissionDto testSubmissionDto) {
//        try {
//            log.info("Received test submission for studentId: {}, testId: {}", 
//                    testSubmissionDto.getStudentId(), testSubmissionDto.getTestId());
//
//            StudentTestResultDto result = testResultService.submitTestAndCalculateResult(testSubmissionDto);
//
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(createSuccessResponse("Test submitted successfully", result));
//
//        } catch (CustomException e) {
//            log.error("Custom exception in submitTest: {}", e.getMessage());
//            throw new CustomException("Error submitting test", HttpStatus.INTERNAL_SERVER_ERROR);
//        } catch (Exception e) {
//            log.error("Unexpected error in submitTest: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(createErrorResponse("Error submitting test: " + e.getMessage()));
//        }
//    }
    
    
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitTest(@RequestBody TestSubmissionDto testSubmissionDto) {
        try {
            log.info("Received test submission for studentId: {}, testId: {}", 
                    testSubmissionDto.getStudentId(), testSubmissionDto.getTestId());

            StudentTestResultDto result = testResultService.submitTestAndCalculateResult(testSubmissionDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Test submitted successfully", result));

        } catch (CustomException e) {
            log.error("Custom exception in submitTest: {}", e.getMessage());
           
            throw new CustomException("Error :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in submitTest: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error submitting test: " + e.getMessage()));
        }
    }
    
    

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateTestResult(@RequestBody TestSubmissionDto testSubmissionDto) {
        try {
            log.info("Calculating test result for studentId: {}, testId: {}", 
                    testSubmissionDto.getStudentId(), testSubmissionDto.getTestId());

            TestResultCalculationDto calculation = testResultService.calculateTestResult(testSubmissionDto);

            return ResponseEntity.ok(createSuccessResponse("Test result calculated successfully", calculation));

        } catch (CustomException e) {
            log.error("Custom exception in calculateTestResult: {}", e.getMessage());
            throw new CustomException("Error calculating test result", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error in calculateTestResult: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error calculating test result: " + e.getMessage()));
        }
    }
    
    
    @GetMapping("/student/{studentId}/monthly")
    public ResponseEntity<?> getMonthlyPerformance(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        
        try {
            PerformanceAnalyticsDto performance = testResultService.getMonthlyPerformance(studentId, month);
            return ResponseEntity.ok(createSuccessResponse("Monthly performance fetched", performance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching monthly performance: " + e.getMessage()));
        }
    }
    
    // Get last month performance (for current 1st date of month)
    @GetMapping("/student/{studentId}/last-month")
    public ResponseEntity<?> getLastMonthPerformance(@PathVariable Long studentId) {
        
        try {
            PerformanceAnalyticsDto performance = testResultService.getLastMonthPerformance(studentId);
            return ResponseEntity.ok(createSuccessResponse("Last month performance fetched", performance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching last month performance: " + e.getMessage()));
        }
    }
    
    // Get weekly performance
    @GetMapping("/student/{studentId}/weekly")
    public ResponseEntity<?> getWeeklyPerformance(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        try {
            PerformanceAnalyticsDto performance = testResultService.getWeeklyPerformance(studentId, weekStart);
            return ResponseEntity.ok(createSuccessResponse("Weekly performance fetched", performance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching weekly performance: " + e.getMessage()));
        }
    }
    
    // Get last month top ranks (institute-wise)
    @GetMapping("/institute/{instituteEmail}/top-ranks/last-month")
    public ResponseEntity<?> getLastMonthTopRanks(
            @PathVariable String instituteEmail,
            @RequestParam(required = false) Integer topN) {
        
        try {
            PerformanceAnalyticsDto topRanks = testResultService.getLastMonthTopRanks(instituteEmail, topN);
            return ResponseEntity.ok(createSuccessResponse("Last month top ranks fetched", topRanks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching top ranks: " + e.getMessage()));
        }
    }
    
    
    @GetMapping("/getStudentAttemptedTest/{studentId}/{examType}")
    public ResponseEntity<?> getAttemptedName(
            @PathVariable Long studentId,
            @PathVariable String examType
    		) {
         
        try {
        	
        	ExamType examType2 = ExamType.valueOf(examType);
            
        	List<TestNameDto> attemptedTestNames = testResultService.getTestListByStudentIdExamType(studentId , examType2);
        	
            return ResponseEntity.ok(attemptedTestNames); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test : " + e.getMessage()));
        }
    }
    
    
    @GetMapping("/student/{studentId}/last-test/{instituteEmail}")
    public ResponseEntity<?> getStudentLeaderboardForLastAttemptedTest(
            @Parameter(description = "ID of the student", required = true, example = "123")
            @PathVariable @NotNull Long studentId,
            
            @Parameter(description = "Institute email of the student", required = true, 
                      example = "institute@example.com")
            @PathVariable @NotNull String instituteEmail,
            
            @Parameter(description = "Number of top ranks to return (optional)", example = "10")
            @RequestParam(required = false) Integer topN,
            
            @Parameter(description = "Whether to include the current student in the response (default: true)", 
                      example = "true")
            @RequestParam(defaultValue = "true") Boolean includeStudent) {
        
        try {
            log.info("Request received for student leaderboard - Student ID: {}, Institute: {}, topN: {}, includeStudent: {}",
                    studentId, instituteEmail, topN, includeStudent);
            
            // Validate parameters
            if (instituteEmail == null || instituteEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Institute email is required");
            }
            
            StudentLeaderboardDto leaderboard = testResultService
                    .getStudentLeaderboardForLastAttemptedTest(studentId, instituteEmail, topN, includeStudent);
            
            log.info("Successfully retrieved leaderboard for student ID: {}, Total participants: {}",
                    studentId, leaderboard.getTotalParticipants());
            
            return ResponseEntity.ok(leaderboard);
            
        } catch (Exception e) {
            log.error("Error retrieving student leaderboard for student ID {}: {}", studentId, e.getMessage(), e);
            
            // Handle different types of exceptions
            if (e.getMessage() != null) {
                if (e.getMessage().contains("not attempted any tests")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Student has not attempted any tests yet");
                } else if (e.getMessage().contains("not found")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(e.getMessage());
                }
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving leaderboard: " + e.getMessage());
        }
    }
    
    
//    @GetMapping("/getStudentLastTest/{studentId}/institute{instituteEmail}")
//    public ResponseEntity<?> getStudentLastExamRank(
//            @PathVariable Long studentId,
//            @PathVariable String instituteEmail
//    		) {
//         
//        try {
//        	        	
//            
//        	StudentPerformanceDto testResult = testResultService.getStudentLastExamRank(studentId , instituteEmail);
//        	
//            return ResponseEntity.ok(testResult); 
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(createErrorResponse("Error fetching test : " + e.getMessage()));
//        }
//    }
    
    
    
    
    
    
    
}
