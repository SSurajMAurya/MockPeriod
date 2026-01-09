//package com.mockperiod.main.serviceImpl;
//
//
//
//import com.mockperiod.main.entities.TestRanking;
//import com.mockperiod.main.entities.Users;
//import com.mockperiod.main.service.TestRankingService;
//import com.mockperiod.main.entities.ExamType;
//import com.mockperiod.main.repository.TestRankingRepository;
//import com.mockperiod.main.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TestRankingServiceImpl implements TestRankingService {
//
//    private final TestRankingRepository testRankingRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public Integer calculateAndSaveRank(Long testId, Long studentId, Double totalMarks, 
//                                      Integer correctAnswers, String timeSpent, ExamType examType) {
//        try {
//            // Get all existing rankings for this test
//            List<TestRanking> existingRankings = testRankingRepository.findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(testId);
//            
//              Users  user = userRepository.findById(studentId).orElse(null);
//              
//            
//            // Check if student already has a ranking for this test
//            TestRanking existingStudentRanking = existingRankings.stream()
//                    .filter(ranking -> ranking.getStudentId().equals(studentId))
//                    .findFirst()
//                    .orElse(null);
//
//            // Remove existing ranking if present (for update)
//            if (existingStudentRanking != null) {
//                existingRankings.remove(existingStudentRanking);
//            }
//
//            // Create new/updated ranking entry
//            TestRanking newRanking;
//            if (existingStudentRanking != null) {
//                // Update existing ranking
//                newRanking = existingStudentRanking;
//                newRanking.setTotalMarksObtained(totalMarks);
//                newRanking.setNoOfCorrectAnswers(correctAnswers);
//                newRanking.setTimeSpent(timeSpent);
//                newRanking.setSubmissionTimestamp(LocalDateTime.now());
//                newRanking.setStudentName(user.getName());
//            } else {
//                // Create new ranking
//                newRanking = TestRanking.builder()
//                        .testId(testId)
//                        .studentId(studentId)
//                        .totalMarksObtained(totalMarks)
//                        .noOfCorrectAnswers(correctAnswers)
//                        .timeSpent(timeSpent)
//                        .examType(examType)
//                        .submissionTimestamp(LocalDateTime.now())
//                        .studentName(user.getName())
//                        .build();
//            }
//
//            // Add new/updated ranking to the list
//            existingRankings.add(newRanking);
//
//            // Sort all rankings by marks (descending) and submission time (ascending)
//            List<TestRanking> sortedRankings = existingRankings.stream()
//                    .sorted(Comparator
//                            .comparing(TestRanking::getTotalMarksObtained).reversed()
//                            .thenComparing(TestRanking::getSubmissionTimestamp))
//                    .collect(Collectors.toList());
//
//            // Assign ranks (handling ties)
//            int currentRank = 1;
//            for (int i = 0; i < sortedRankings.size(); i++) {
//                TestRanking ranking = sortedRankings.get(i);
//                
//                // For first element or when marks are different from previous, assign new rank
//                if (i == 0 || !ranking.getTotalMarksObtained().equals(sortedRankings.get(i-1).getTotalMarksObtained())) {
//                    ranking.setRank(currentRank);
//                } else {
//                    // Same marks as previous, same rank
//                    ranking.setRank(currentRank - 1);
//                }
//                currentRank++;
//            }
//
//            // Save all rankings
//            testRankingRepository.saveAll(sortedRankings);
//
//            // Find and return the student's rank
//            Integer studentRank = sortedRankings.stream()
//                    .filter(ranking -> ranking.getStudentId().equals(studentId))
//                    .findFirst()
//                    .map(TestRanking::getRank)
//                    .orElse(null);
//
//            log.info("Rank calculated for student {} in test {}: Rank {}", studentId, testId, studentRank);
//            return studentRank;
//
//        } catch (Exception e) {
//            log.error("Error calculating rank for student {} in test {}: {}", studentId, testId, e.getMessage(), e);
//            throw new RuntimeException("Failed to calculate rank: " + e.getMessage());
//        }
//    }
//
//    public Integer getCurrentRank(Long testId, Long studentId) {
//    	
//        return testRankingRepository.findByTestIdAndStudentId(testId, studentId)
//                .map(TestRanking::getRank)
//                .orElse(null);
//    }
//
//    public List<TestRanking> getTopRankings(Long testId, int limit) {
//        return testRankingRepository.findTopByTestIdOrderByRankAsc(testId, limit);
//    }
//
//    public boolean hasStudentTakenTest(Long testId, Long studentId) {
//        return testRankingRepository.existsByTestIdAndStudentId(testId, studentId);
//    }
//}



package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.entities.TestRanking;
import com.mockperiod.main.entities.Users;
import com.mockperiod.main.service.TestRankingService;
import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.repository.TestRankingRepository;
import com.mockperiod.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestRankingServiceImpl implements TestRankingService {

    private final TestRankingRepository testRankingRepository;
    private final UserRepository userRepository;


    @Override
    public Integer getCurrentRank(Long testId, Long studentId) {
        return testRankingRepository.findByTestIdAndStudentId(testId, studentId)
                .map(TestRanking::getRank)
                .orElse(null);
    }

    @Override
    public List<TestRanking> getTopRankings(Long testId, int limit) {
        return testRankingRepository.findTopByTestIdOrderByRankAsc(testId, limit);
    }

    @Override
    public boolean hasStudentTakenTest(Long testId, Long studentId) {
        return testRankingRepository.existsByTestIdAndStudentId(testId, studentId);
    }

    // Helper method to convert time string to seconds
    private int convertTimeToSeconds(String timeSpent) {
        if (timeSpent == null || timeSpent.trim().isEmpty() || timeSpent.equals("N/A")) {
            return Integer.MAX_VALUE; // Return max value for sorting
        }
        
        try {
            int totalSeconds = 0;
            String timeStr = timeSpent.toLowerCase().trim();
            
            // Parse hours
            if (timeStr.contains("h")) {
                String[] parts = timeStr.split("h");
                if (parts[0].trim().matches("\\d+")) {
                    totalSeconds += Integer.parseInt(parts[0].trim()) * 3600;
                }
                timeStr = parts.length > 1 ? parts[1].trim() : "";
            }
            
            // Parse minutes
            if (timeStr.contains("m")) {
                String[] parts = timeStr.split("m");
                if (parts[0].trim().matches("\\d+")) {
                    totalSeconds += Integer.parseInt(parts[0].trim()) * 60;
                }
                timeStr = parts.length > 1 ? parts[1].trim() : "";
            }
            
            // Parse seconds
            if (timeStr.contains("s")) {
                String secondsStr = timeStr.replace("s", "").trim();
                if (secondsStr.matches("\\d+")) {
                    totalSeconds += Integer.parseInt(secondsStr);
                }
            }
            
            return totalSeconds;
            
        } catch (Exception e) {
            log.warn("Error parsing time string '{}': {}", timeSpent, e.getMessage());
            return Integer.MAX_VALUE;
        }
    }

    // Custom comparator for ranking
  // Custom comparator for ranking - FIXED VERSION
private class TestRankingComparator implements Comparator<TestRanking> {
    @Override
    public int compare(TestRanking r1, TestRanking r2) {
        // Primary: Total marks (descending)
        int marksCompare = r2.getTotalMarksObtained().compareTo(r1.getTotalMarksObtained());
        if (marksCompare != 0) {
            return marksCompare;
        }
        
        // Secondary: Time spent (ascending)
        int time1 = convertTimeToSeconds(r1.getTimeSpent());
        int time2 = convertTimeToSeconds(r2.getTimeSpent());
        
        if (time1 != time2) {
            return Integer.compare(time1, time2);
        }
        
        // Tertiary: Submission timestamp (ascending - earlier is better)
        return r1.getSubmissionTimestamp().compareTo(r2.getSubmissionTimestamp());
    }
}

    // Additional utility methods for better ranking management
    
    @Override
    @Transactional
    public void recalculateAllRanksForTest(Long testId) {
        try {
            log.info("Recalculating all ranks for testId: {}", testId);
            
            List<TestRanking> allRankings = testRankingRepository.findByTestId(testId);
            
            // Sort with comparator
            List<TestRanking> sortedRankings = allRankings.stream()
                    .sorted(new TestRankingComparator())
                    .collect(Collectors.toList());
            
            // Assign new ranks
            int currentRank = 1;
            for (int i = 0; i < sortedRankings.size(); i++) {
                TestRanking ranking = sortedRankings.get(i);
                
                if (i == 0) {
                    ranking.setRank(currentRank);
                } else {
                    TestRanking previousRanking = sortedRankings.get(i - 1);
                    
                    if (ranking.getTotalMarksObtained().equals(previousRanking.getTotalMarksObtained())) {
                        int currentTime = convertTimeToSeconds(ranking.getTimeSpent());
                        int previousTime = convertTimeToSeconds(previousRanking.getTimeSpent());
                        
                        if (currentTime == previousTime) {
                            ranking.setRank(previousRanking.getRank());
                        } else {
                            ranking.setRank(currentRank);
                        }
                    } else {
                        ranking.setRank(currentRank);
                    }
                }
                currentRank++;
            }
            
            testRankingRepository.saveAll(sortedRankings);
            log.info("Successfully recalculated ranks for {} students in test {}", 
                    sortedRankings.size(), testId);
            
        } catch (Exception e) {
            log.error("Error recalculating ranks for test {}: {}", testId, e.getMessage(), e);
            throw new RuntimeException("Failed to recalculate ranks: " + e.getMessage());
        }
    }
    
    @Override
    public List<TestRanking> getRankingsWithTieInfo(Long testId) {
        List<TestRanking> rankings = testRankingRepository
                .findByTestIdOrderByRankAsc(testId);
        
        // Add tie information
        for (int i = 0; i < rankings.size(); i++) {
            TestRanking current = rankings.get(i);
            
            // Check if tied with next student
            if (i < rankings.size() - 1) {
                TestRanking next = rankings.get(i + 1);
                if (current.getRank().equals(next.getRank())) {
                    current.setTied(true);
                    next.setTied(true);
                }
            }
        }
        
        return rankings;
    }




	@Override
	public Integer calculateAndSaveRank(Long testId, Long studentId, Double totalMarks, Integer correctAnswers,
			String timeSpent, ExamType examType) {
        try {
            log.info("Calculating rank for testId: {}, studentId: {}, marks: {}, time: {}", 
                    testId, studentId, totalMarks, timeSpent);
            
            // Get all existing rankings for this test
            List<TestRanking> existingRankings = testRankingRepository
                    .findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(testId);
            
            Users user = userRepository.findById(studentId).orElse(null);
            
            // Check if student already has a ranking for this test
            TestRanking existingStudentRanking = existingRankings.stream()
                    .filter(ranking -> ranking.getStudentId().equals(studentId))
                    .findFirst()
                    .orElse(null);

            // If student already has a ranking, don't allow resubmission
            if (existingStudentRanking != null) {
                log.warn("Student {} already has ranking for test {}. Returning existing rank: {}", 
                        studentId, testId, existingStudentRanking.getRank());
                return existingStudentRanking.getRank();
            }

            // Create new ranking entry
            TestRanking newRanking = TestRanking.builder()
                    .testId(testId)
                    .studentId(studentId)
                    .totalMarksObtained(totalMarks)
                    .noOfCorrectAnswers(correctAnswers)
                    .timeSpent(timeSpent)
                    .examType((examType))
                    .submissionTimestamp(LocalDateTime.now())
                    .studentName(user != null ? user.getName() : "Unknown")
//                    .studentEmail(user != null ? user.getEmail() : "")
                    .build();

            // Add new ranking to the list
            existingRankings.add(newRanking);

            // Sort with proper tie-breaking logic
            List<TestRanking> sortedRankings = existingRankings.stream()
                    .sorted(new TestRankingComparator())
                    .collect(Collectors.toList());

            // Assign ranks with proper tie-breaking
//            int currentRank = 1;
//            for (int i = 0; i < sortedRankings.size(); i++) {
//                TestRanking ranking = sortedRankings.get(i);
//                
//                if (i == 0) {
//                    // First rank
//                    ranking.setRank(currentRank);
//                } else {
//                    TestRanking previousRanking = sortedRankings.get(i - 1);
//                    
//                    // Compare with previous ranking
//                    if (ranking.getTotalMarksObtained().equals(previousRanking.getTotalMarksObtained())) {
//                        // Same marks, compare time spent
//                        int currentTimeSeconds = convertTimeToSeconds(ranking.getTimeSpent());
//                        int previousTimeSeconds = convertTimeToSeconds(previousRanking.getTimeSpent());
//                        
//                        if (currentTimeSeconds == previousTimeSeconds) {
//                            // Same time, compare submission timestamp
//                            long currentTimeDiff = ChronoUnit.SECONDS.between(
//                                    ranking.getSubmissionTimestamp(), 
//                                    previousRanking.getSubmissionTimestamp()
//                            );
//                            
//                            if (currentTimeDiff >= 0) {
//                                // Same rank if submitted at same time or later
//                                ranking.setRank(previousRanking.getRank());
//                            } else {
//                                // Submitted earlier, gets better rank
//                                ranking.setRank(currentRank);
//                            }
//                        } else if (currentTimeSeconds < previousTimeSeconds) {
//                            // Less time spent, gets better rank
//                            ranking.setRank(currentRank);
//                        } else {
//                            // More time spent, gets same rank as previous
//                            ranking.setRank(previousRanking.getRank());
//                        }
//                    } else {
//                        // Different marks, new rank
//                        ranking.setRank(currentRank);
//                    }
//                }
//                
//                // Update the ranking in the list
//                sortedRankings.set(i, ranking);
//                currentRank++;
//            }
            
            
         // CLEANER APPROACH using comparator grouping
            int currentRank = 1;
            for (int i = 0; i < sortedRankings.size(); i++) {
                TestRanking ranking = sortedRankings.get(i);
                
                if (i == 0) {
                    ranking.setRank(currentRank);
                } else {
                    TestRanking previousRanking = sortedRankings.get(i - 1);
                    
                    // Use the comparator to determine if they should have same rank
                    TestRankingComparator comparator = new TestRankingComparator();
                    int comparison = comparator.compare(ranking, previousRanking);
                    
                    if (comparison == 0) {
                        // Exactly equal in all criteria (marks, time, timestamp), same rank
                        ranking.setRank(previousRanking.getRank());
                    } else {
                        // Not equal, gets next rank
                        ranking.setRank(previousRanking.getRank() + 1);
                    }
                }
            }

            // Save all rankings
            testRankingRepository.saveAll(sortedRankings);

            // Find and return the student's rank
            Integer studentRank = sortedRankings.stream()
                    .filter(ranking -> ranking.getStudentId().equals(studentId))
                    .findFirst()
                    .map(TestRanking::getRank)
                    .orElse(null);

            log.info("Rank calculated for student {} in test {}: Rank {}", studentId, testId, studentRank);
            return studentRank;

        } catch (Exception e) {
            log.error("Error calculating rank for student {} in test {}: {}", studentId, testId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate rank: " + e.getMessage());
        }
    }
}