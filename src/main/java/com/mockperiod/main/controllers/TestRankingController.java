package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.RankingResponseDto;
import com.mockperiod.main.dto.StudentTestResultDto;
import com.mockperiod.main.dto.UserDto;
import com.mockperiod.main.entities.StudentTestResult;
import com.mockperiod.main.entities.TestRanking;
import com.mockperiod.main.entities.Users;
import com.mockperiod.main.service.StudentTestResultService;
//import com.mockperiod.main.service.RankingService;
import com.mockperiod.main.service.TestRankingService;
import com.mockperiod.main.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TestRankingController {

    private final TestRankingService rankingService;
    private final UserService userService;
    private final StudentTestResultService studentTestResultService;
    /**
     * Get current rank of a student for a specific test
     */
    @GetMapping("/test/{testId}/student/{studentId}")
    public ResponseEntity<?> getStudentRank(
            @PathVariable Long testId,
            @PathVariable Long studentId) {
        try {
            log.info("Fetching rank for studentId: {} in testId: {}", studentId, testId);
            
            Integer currentRank = rankingService.getCurrentRank(testId, studentId);
            
            if (currentRank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "No ranking found for the student in this test",
                                "testId", testId,
                                "studentId", studentId
                        ));
            }
            
            UserDto users =   userService.getUserById(studentId);

            RankingResponseDto response = RankingResponseDto.builder()
                    .testId(testId)
                    .studentId(studentId)
                    .rank(currentRank)
                    .studentName(users.getName())
                    .studentEmail(users.getEmail())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching rank for studentId: {} in testId: {}: {}", studentId, testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error fetching rank: " + e.getMessage(),
                            "testId", testId,
                            "studentId", studentId
                    ));
        }
    }

    /**
     * Get top rankings for a specific test
     */
    @GetMapping("/test/{testId}/top")
    public ResponseEntity<?> getTopRankings(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Fetching top {} rankings for testId: {}", limit, testId);
            
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Limit must be between 1 and 100",
                                "providedLimit", limit
                        ));
            }

            List<TestRanking> topRankings = rankingService.getTopRankings(testId, limit);
            
          
            
            return ResponseEntity.ok(Map.of(
                    "testId", testId,
                    "limit", limit,
                    "rankings", topRankings,
                    "total", topRankings.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching top rankings for testId: {}: {}", testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error fetching top rankings: " + e.getMessage(),
                            "testId", testId
                    ));
        }
    }

    /**
     * Get complete leaderboard for a test
     */
    @GetMapping("/test/{testId}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable Long testId) {
        try {
            log.info("Fetching complete leaderboard for testId: {}", testId);
            
            List<TestRanking> leaderboard = rankingService.getTopRankings(testId, 1000);
            
            return ResponseEntity.ok(Map.of(
                    "testId", testId,
                    "rankings", leaderboard,
                    "totalParticipants", leaderboard.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching leaderboard for testId: {}: {}", testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error fetching leaderboard: " + e.getMessage(),
                            "testId", testId
                    ));
        }
    }

    /**
     * Get student's rank history across all tests
     */
    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<?> getStudentRankHistory(@PathVariable Long studentId) {
        try {
            log.info("Fetching rank history for studentId: {}", studentId);
            
            // This would require a new method in repository and service
            // For now, returning not implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of(
                            "error", "Rank history feature not implemented yet",
                            "studentId", studentId
                    ));

        } catch (Exception e) {
            log.error("Error fetching rank history for studentId: {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error fetching rank history: " + e.getMessage(),
                            "studentId", studentId
                    ));
        }
    }

    /**
     * Calculate rank immediately (for testing purposes)
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateRank(
            @RequestParam Long testId,
            @RequestParam Long studentId,
            @RequestParam Double totalMarks,
            @RequestParam Integer correctAnswers,
            @RequestParam String timeSpent,
            @RequestParam String examType) {
        try {
            log.info("Calculating rank for studentId: {} in testId: {} with marks: {}", 
                    studentId, testId, totalMarks);
            
            Integer rank = rankingService.calculateAndSaveRank(
                    testId, studentId, totalMarks, correctAnswers, timeSpent, 
                    com.mockperiod.main.entities.ExamType.valueOf(examType));
            
            RankingResponseDto response = RankingResponseDto.builder()
                    .testId(testId)
                    .studentId(studentId)
                    .rank(rank)
                    .totalMarks(totalMarks)
                    .correctAnswers(correctAnswers)
                    .timeSpent(timeSpent)
                    .examType(examType)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid exam type: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Invalid exam type",
                            "message", "Valid values: MOCK_TEST, PRACTICE_TEST, QUIZ, ASSIGNMENT, FINAL_EXAM",
                            "providedExamType", examType
                    ));
        } catch (Exception e) {
            log.error("Error calculating rank: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error calculating rank",
                            "message", e.getMessage(),
                            "testId", testId,
                            "studentId", studentId
                    ));
        }
    }

    /**
     * Check if student has taken a test
     */
    @GetMapping("/test/{testId}/student/{studentId}/exists")
    public ResponseEntity<?> hasStudentTakenTest(
            @PathVariable Long testId,
            @PathVariable Long studentId) {
        try {
            log.info("Checking if student {} has taken test {}", studentId, testId);
            
            boolean hasTakenTest = rankingService.hasStudentTakenTest(testId, studentId);
            
            return ResponseEntity.ok(Map.of(
                    "testId", testId,
                    "studentId", studentId,
                    "hasTakenTest", hasTakenTest
            ));

        } catch (Exception e) {
            log.error("Error checking test participation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error checking test participation",
                            "message", e.getMessage(),
                            "testId", testId,
                            "studentId", studentId
                    ));
        }
    }

    /**
     * Get ranking statistics for a test
     */
    @GetMapping("/test/{testId}/stats")
    public ResponseEntity<?> getRankingStats(@PathVariable Long testId) {
        try {
            log.info("Fetching ranking statistics for testId: {}", testId);
            
            List<TestRanking> allRankings = rankingService.getTopRankings(testId, 1000);
            
            if (allRankings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "No rankings found for this test",
                                "testId", testId
                        ));
            }

            double averageMarks = allRankings.stream()
                    .mapToDouble(TestRanking::getTotalMarksObtained)
                    .average()
                    .orElse(0.0);

            double highestMarks = allRankings.stream()
                    .mapToDouble(TestRanking::getTotalMarksObtained)
                    .max()
                    .orElse(0.0);

            double lowestMarks = allRankings.stream()
                    .mapToDouble(TestRanking::getTotalMarksObtained)
                    .min()
                    .orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "testId", testId,
                    "totalParticipants", allRankings.size(),
                    "averageMarks", Math.round(averageMarks * 100.0) / 100.0,
                    "highestMarks", highestMarks,
                    "lowestMarks", lowestMarks,
                    "rankingsAvailable", true
            ));

        } catch (Exception e) {
            log.error("Error fetching ranking stats for testId: {}: {}", testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error fetching ranking statistics",
                            "message", e.getMessage(),
                            "testId", testId
                    ));
        }
    }

    /**
     * Health check endpoint for rankings
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Ranking Controller",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
    
    
    
    
    
    @GetMapping("/top-ranks/latest/{instituteEmail}")
    public ResponseEntity<List<RankingResponseDto>> getTopRanksForLatestTest(
            @PathVariable String instituteEmail,
            @RequestParam(required = false, defaultValue = "10") Integer topN) {
        
        List<RankingResponseDto> topRankList = studentTestResultService.getTopRankListForLatestTest(instituteEmail, topN);
        return ResponseEntity.ok(topRankList);
    }
    
    /**
     * Get top ranks for a specific test
     */
    @GetMapping("/top-ranks/test/{testId}")
    public ResponseEntity<List<RankingResponseDto>> getTopRanksForTest(
            @PathVariable Long testId,
            @RequestParam(required = false, defaultValue = "10") Integer topN) {
        
        List<RankingResponseDto> topRankList = studentTestResultService.getTopRanksForTest(testId, topN);
        return ResponseEntity.ok(topRankList);
    }
    
    /**
     * Get basic test statistics for dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics(@RequestParam String instituteEmail) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total tests for institute
        List<StudentTestResultDto> allResults = studentTestResultService.getAllTestResultByInstitute(instituteEmail);
        stats.put("totalTests", allResults.size());
        
        // Get unique students count
        long uniqueStudents = allResults.stream()
                .map(StudentTestResultDto::getStudentId)
                .distinct()
                .count();
        stats.put("totalStudents", uniqueStudents);
        
        // Get average marks
        double avgMarks = allResults.stream()
                .mapToDouble(StudentTestResultDto::getTotalObtainedMarks)
                .average()
                .orElse(0.0);
        stats.put("averageMarks", avgMarks);
        
        return ResponseEntity.ok(stats);
    }
}