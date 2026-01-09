//package com.mockperiod.main.serviceImpl;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mockperiod.main.dto.OptionDto;
//import com.mockperiod.main.dto.PerformanceAnalyticsDto;
//import com.mockperiod.main.dto.QuestionAttemptDto;
//import com.mockperiod.main.dto.QuestionDto;
//import com.mockperiod.main.dto.RankingResponseDto;
//import com.mockperiod.main.dto.StudentPerformanceDto;
//import com.mockperiod.main.dto.StudentPerformanceDto.RankEntry;
//import com.mockperiod.main.dto.StudentTestResultDto;
//import com.mockperiod.main.dto.SubjectFEDto;
//import com.mockperiod.main.dto.TestResultCalculationDto;
//import com.mockperiod.main.dto.TestSubmissionDto;
//import com.mockperiod.main.entities.Options;
//import com.mockperiod.main.entities.Questions;
//import com.mockperiod.main.entities.StudentTestResult;
//import com.mockperiod.main.entities.Subjects;
//import com.mockperiod.main.entities.Tests;
//import com.mockperiod.main.entities.Users;
//import com.mockperiod.main.exceptions.CustomException;
//import com.mockperiod.main.repository.OptionRepository;
//import com.mockperiod.main.repository.QuestionRepository;
//import com.mockperiod.main.repository.StudentTestResultRepository;
//import com.mockperiod.main.repository.SubjectRepository;
//import com.mockperiod.main.repository.TestRankingRepository;
//import com.mockperiod.main.repository.TestRepository;
//import com.mockperiod.main.repository.UserRepository;
//import com.mockperiod.main.service.StudentTestResultService;
//import com.mockperiod.main.service.TestRankingService;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class StudentTestResultServiceImpl implements StudentTestResultService {
//
//    private final TestRepository testRepository;
//    private final StudentTestResultRepository testResultRepository;
//    private final UserRepository userRepository;
//    private final QuestionRepository questionRepository;
//    private final OptionRepository optionRepository;
//    private final SubjectRepository subjectRepository;
//    private final TestRankingRepository testRankingRepository; 
//    
//    private final TestRankingService rankingService;
//    private final ObjectMapper objectMapper;
//
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @Override
//    public StudentTestResultDto createTestResult(StudentTestResultDto testResultDto) {
//        try {
//            Tests tests = testRepository.findById(testResultDto.getTestId())
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//            
//            Users users = userRepository.findById(testResultDto.getStudentId())
//                    .orElseThrow(() -> new CustomException("User(Student) not found", HttpStatus.NOT_FOUND));
//
//            LocalDate date = LocalDate.parse(testResultDto.getTestAttemptedDate(), formatter);
//
//            StudentTestResult testResult = new StudentTestResult();
//            
//            // Direct assignment - negative marks are allowed
//            testResult.setNoOfCorrectAns(testResultDto.getNoOfCorrectAns());
//            testResult.setStudentEmail(users.getEmail());
//            testResult.setNoOfInCorrectAns(testResultDto.getNoOfInCorrectAns());
//            testResult.setInstituteEmail(testResultDto.getInstituteEmail());
//            testResult.setStudentId(users.getId());
//            testResult.setTestAttemptedDate(date);
//            
//            // IMPORTANT: Store negative marks as they are (no Math.max here)
//            testResult.setTotalObtainedMarks(testResultDto.getTotalObtainedMarks()); // Could be negative
//            
//            testResult.setTestId(tests.getId());
//            testResult.setTestName(tests.getTestName());
//            testResult.setCurrectquestionIds(testResultDto.getCurrectquestionIds());
//            testResult.setIncurrectquestionIds(testResultDto.getIncurrectquestionIds());
//            
//            // Calculate remark based on negative marks
//            String remark = calculateRemarkBasedOnNegativeMarks(
//                testResultDto.getTotalObtainedMarks(), 
//                tests
//            );
//            testResult.setRemark(remark);
//
//            // Set time spent if available in DTO
//            if (testResultDto.getTimeSpent() != null) {
//                testResult.setTimeSpent(testResultDto.getTimeSpent());
//            }
//
//            StudentTestResult savedResult = testResultRepository.save(testResult);
//            
//            return convertToDto(savedResult);
//
//        } catch (CustomException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException("Error creating test result: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    private StudentTestResultDto convertToDto(StudentTestResult testResult) {
//        StudentTestResultDto dto = new StudentTestResultDto();
//        BeanUtils.copyProperties(testResult, dto);
//        dto.setTestAttemptedDate(testResult.getTestAttemptedDate().format(formatter));
//        return dto;
//    }
//
//    @Override
//    public List<StudentTestResultDto> getAllTestResult() {
//        try {
//            List<StudentTestResult> testResults = testResultRepository.findAll();
//            return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public List<StudentTestResultDto> getAllTestResultByInstitute(String instituteEmail) {
//        try {
//            List<StudentTestResult> testResults = testResultRepository.findByInstituteEmail(instituteEmail);
//            return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results by institute: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public void deleteTestResult(Long id) {
//        try {
//            StudentTestResult testResult = testResultRepository.findById(id).orElseThrow(
//                    () -> new CustomException("Test result not found with id: " + id, HttpStatus.NOT_FOUND));
//            testResultRepository.delete(testResult);
//        } catch (CustomException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException("Error deleting test result: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public List<StudentTestResultDto> getAllTestResultByDateAndInstitute(LocalDate date, String instituteEmail) {
//        try {
//            List<StudentTestResult> testResults = testResultRepository.findByTestAttemptedDateAndInstituteEmail(date,
//                    instituteEmail);
//            return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results by date and institute: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public List<StudentTestResultDto> getTestResultByStudentAndInstitute(Long studentId, String instituteEmail) {
//        try {
//            List<StudentTestResult> testResults = testResultRepository.findByStudentIdAndInstituteEmail(studentId,
//                    instituteEmail);
//            return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results by student and institute: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public StudentTestResultDto getTestResultById(Long id) {
//        try {
//            StudentTestResult testResult = testResultRepository.findById(id).orElseThrow(
//                    () -> new CustomException("Test result not found with id: " + id, HttpStatus.NOT_FOUND));
//
//            return convertToDto(testResult);
//        } catch (CustomException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test result: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public List<StudentTestResultDto> getTestResultByStudentId(Long studentId) {
//        try {
//            List<StudentTestResult> testResults = testResultRepository.findByStudentId(studentId);
//            return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results by student: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public StudentTestResultDto getTestResultByStudentIdAndTestId(Long studentId, Long testId) {
//        try {
//            StudentTestResult testResults = testResultRepository.findByStudentIdAndTestId(studentId, testId);
//
//            if (testResults == null) {
//                throw new CustomException(
//                        "Test result not found for student ID: " + studentId + " and test ID: " + testId,
//                        HttpStatus.NOT_FOUND);
//            }
//
//            // Get the test details
//            Tests test = testRepository.findById(testId)
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//            // Get all questions for the test
//            List<Questions> allTestQuestions = questionRepository.findByTestId(testId);
//            
//            // Get correct and incorrect question IDs from the result
//            List<Long> correctQuestionIds = testResults.getCurrectquestionIds() != null ? 
//                    testResults.getCurrectquestionIds() : new ArrayList<>();
//            List<Long> incorrectQuestionIds = testResults.getIncurrectquestionIds() != null ? 
//                    testResults.getIncurrectquestionIds() : new ArrayList<>();
//            
//            // Identify unanswered questions
//            List<Long> unansweredQuestionIds = allTestQuestions.stream()
//                    .map(Questions::getId)
//                    .filter(questionId -> !correctQuestionIds.contains(questionId) && !incorrectQuestionIds.contains(questionId))
//                    .collect(Collectors.toList());
//
//            // Calculate total marks for the test
//            double totalMarks = allTestQuestions.stream()
//                    .mapToDouble(question -> question.getMarks() != null ? question.getMarks() : test.getCorrectMark())
//                    .sum();
//
//            // Calculate negative marks
//            double negativeMarks = incorrectQuestionIds.size() * test.getNegativeMark();
//            
//            // Get obtained marks (could be negative)
//            double obtainedMarks = testResults.getTotalObtainedMarks() != null ? 
//                    testResults.getTotalObtainedMarks() : 0.0;
//            
//            // Keep negative marks as they are
//            double finalObtainedMarks = obtainedMarks;
//
//            // Get rank information
//            Integer currentRank = getRankForStudent(testId, studentId);
////            Integer totalParticipants = getTotalParticipants(testId);
//            
//            // Get time spent
//            String timeSpent = testResults.getTimeSpent() != null ? 
//                    testResults.getTimeSpent() : "Not Available";
//
//            // Fetch questions without IDs for clean response
//            List<QuestionDto> correctQuestionDtos = fetchQuestionsWithoutIds(correctQuestionIds);
//            List<QuestionDto> incorrectQuestionDtos = fetchQuestionsWithoutIds(incorrectQuestionIds);
//            List<QuestionDto> unansweredQuestionDtos = fetchQuestionsWithoutIds(unansweredQuestionIds);
//
//            // Build the response DTO
//            StudentTestResultDto response = StudentTestResultDto.builder()
//                    .id(testResults.getId())
//                    .testId(testResults.getTestId())
//                    .testName(testResults.getTestName())
//                    .studentId(testResults.getStudentId())
//                    .studentEmail(testResults.getStudentEmail())
//                    .instituteEmail(testResults.getInstituteEmail())
//                    .noOfCorrectAns(testResults.getNoOfCorrectAns())
////                    .incurrectquestionIds(testResults.getIncurrectquestionIds())
//                    .noOfInCorrectAns(testResults.getNoOfInCorrectAns())
////                    .currectquestionIds(testResults.getCurrectquestionIds())
////                    .questionDtos(correctQuestionDtos) // For backward compatibility
//                    .correctQuestions(correctQuestionDtos)
//                    .incorrectQuestions(incorrectQuestionDtos)
//                    .unansweredQuestions(unansweredQuestionDtos)
//                    .totalObtainedMarks(finalObtainedMarks) // Could be negative
//                    .testAttemptedDate(testResults.getTestAttemptedDate() != null ? 
//                        testResults.getTestAttemptedDate().toString() : null)
//                    .remark(testResults.getRemark())
//                    .currentRank(currentRank)
////                    .totalParticipants(totalParticipants)
//                    .timeSpent(timeSpent)
//                    .examType(test.getExamType())
//                    .build();
//
//            return response;
//
//        } catch (CustomException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException("Error fetching test results by student: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Helper method to fetch questions WITHOUT IDs for clean response
//    private List<QuestionDto> fetchQuestionsWithoutIds(List<Long> questionIds) {
//        return questionIds.stream()
//                .map(questionId -> {
//                    Questions question = questionRepository.findById(questionId)
//                            .orElseThrow(() -> new CustomException("Question not found with ID: " + questionId, HttpStatus.NOT_FOUND));
//                    
//                    // Create QuestionDto WITHOUT setting the ID
//                    QuestionDto questionDto = QuestionDto.builder()
//                            .questionText(question.getQuestionText())
//                            .questionImageUrl(question.getQuestionImageUrl())
//                            .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
//                            .subjectName(question.getSubject() != null ? question.getSubject().getName() : null)
//                            .chapterId(question.getChapter() != null ? question.getChapter().getId() : null)
//                            .questionNumber(question.getQuestionNumber())
//                            .marks(question.getMarks())
//                            .language(question.getLanguage() != null ? question.getLanguage().name() : null)
//                            .build();
//                    
//                    // Fetch options WITHOUT IDs
//                    List<Options> options = optionRepository.findByQuestion(question);
//                    List<OptionDto> optionDtos = options.stream()
//                            .map(option -> OptionDto.builder()
//                                    .optionText(option.getOptionText())
//                                    .optionImageUrl(option.getOptionImageUrl())
//                                    .optionNumber(option.getOptionNumber())
//                                    .isCorrect(option.getIsCorrect())
//                                    .build())
//                            .collect(Collectors.toList());
//                    
//                    questionDto.setOptions(optionDtos);
//                    return questionDto;
//                })
//                .collect(Collectors.toList());
//    }
//
//    private QuestionDto maptoQuestionDto(Questions question) {
//        if (question == null) {
//            return null;
//        }
//
//        return QuestionDto.builder()
//                .id(question.getId())
//                .questionText(question.getQuestionText())
//                .questionImageUrl(question.getQuestionImageUrl())
//                .testId(question.getTest() != null ? question.getTest().getId() : null)
//                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
//                .chapterId(question.getChapter() != null ? question.getChapter().getId() : null)
//                .questionNumber(question.getQuestionNumber())
//                .marks(question.getMarks())
//                .language(question.getLanguage() != null ? question.getLanguage().name() : null)
//                .build();
//    }
//
//    // Map single Options entity to OptionDto
//    private OptionDto maptoOptionDto(Options option) {
//        if (option == null) {
//            return null;
//        }
//
//        return OptionDto.builder()
//                .id(option.getId())
//                .optionText(option.getOptionText())
//                .optionImageUrl(option.getOptionImageUrl())
//                .questionId(option.getQuestion() != null ? option.getQuestion().getId() : null)
//                .optionNumber(option.getOptionNumber())
//                .isCorrect(option.getIsCorrect())
//                .build();
//    }
//
//    // Map List<Options> to List<OptionDto>
//    private List<OptionDto> maptoOptionDto(List<Options> options) {
//        if (options == null || options.isEmpty()) {
//            return List.of();
//        }
//
//        return options.stream().map(this::maptoOptionDto).collect(Collectors.toList());
//    }
//
//    // Calculate remark based on negative marks
//    private String calculateRemarkBasedOnNegativeMarks(double obtainedMarks, Tests test) {
//        if (test == null || test.getTotalMarks() == null || test.getTotalMarks() == 0) {
//            return obtainedMarks < 0 ? "Very Poor (Negative Marks)" : "Average";
//        }
//
//        double percentage = (obtainedMarks / test.getTotalMarks()) * 100;
//
//        if (obtainedMarks < 0) {
//            return "Negative Score";
//        } else if (percentage >= 90) {
//            return "Excellent";
//        } else if (percentage >= 75) {
//            return "Very Good";
//        } else if (percentage >= 60) {
//            return "Good";
//        } else if (percentage >= 40) {
//            return "Average";
//        } else if (percentage >= 0) {
//            return "Needs Improvement";
//        } else {
//            return "Negative Score";
//        }
//    }
//
//    // 1. Get Exam-wise and Subject-wise Ranking
//    @Override
//    public StudentPerformanceDto getRanking(String rankingType, Long referenceId, String instituteEmail, Integer topN) {
//        try {
//            log.info("Fetching {} ranking for referenceId: {}, institute: {}", rankingType, referenceId,
//                    instituteEmail);
//
//            List<StudentTestResult> allResults; 
//            String performanceType = rankingType.toUpperCase();
//
//            if ("EXAM".equals(rankingType)) {
//                // Get all tests for this exam
//                List<Tests> examTests = testRepository.findByExamIdAndInstitutesEmail(referenceId, instituteEmail);
//                if (examTests.isEmpty()) {
//                    throw new CustomException("No tests found for the given exam and institute", HttpStatus.NOT_FOUND);
//                }
//                List<Long> testIds = examTests.stream().map(Tests::getId).collect(Collectors.toList());
//                allResults = testResultRepository.findByTestIdInAndInstituteEmail(testIds, instituteEmail);
//
//            } else if ("SUBJECT".equals(rankingType)) {
//                // Get all tests for this subject
//                List<Tests> subjectTests = testRepository.findByExamIdAndInstitutesEmail(referenceId, instituteEmail);
//                if (subjectTests.isEmpty()) {
//                    throw new CustomException("No tests found for the given subject and institute",
//                            HttpStatus.NOT_FOUND);
//                }
//                List<Long> testIds = subjectTests.stream().map(Tests::getId).collect(Collectors.toList());
//                allResults = testResultRepository.findByTestIdInAndInstituteEmail(testIds, instituteEmail);
//
//            } else {
//                throw new CustomException("Invalid ranking type. Use 'EXAM' or 'SUBJECT'", HttpStatus.BAD_REQUEST);
//            }
//
//            // Calculate total marks per student and rank (negative marks included)
//            Map<Long, StudentMarks> studentMarksMap = calculateStudentMarks(allResults);
//            List<StudentPerformanceDto.RankEntry> leaderboard = generateRanking(studentMarksMap, topN);
//
//            return StudentPerformanceDto.builder()
//                    .analysisType("RANKING")
//                    .performanceType(performanceType)
//                    .leaderboard(leaderboard)
//                    .totalParticipants(leaderboard.size())
//                    .instituteEmail(instituteEmail)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error generating ranking: {}", e.getMessage(), e);
//            throw new CustomException("Error generating ranking: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
// // 2. Get Student Performance (Exam-wise and Subject-wise)
//    @Override
//    public StudentPerformanceDto getStudentPerformance(Long studentId, String performanceType, Long referenceId,
//            String instituteEmail) {
//        try {
//            log.info("Fetching {} performance for studentId: {}, referenceId: {}", performanceType, studentId,
//                    referenceId);
//
//            List<StudentTestResult> studentResults;
//            String analysisType = "PERFORMANCE";
//
//            if ("EXAM".equalsIgnoreCase(performanceType)) {
//                // Get all tests for this exam that belong to the institute
//                List<Tests> examTests = testRepository.findByExamIdAndInstitutesEmail(referenceId, instituteEmail);
//                if (examTests.isEmpty()) {
//                    throw new CustomException("No tests found for the given exam and institute", HttpStatus.NOT_FOUND);
//                }
//                List<Long> testIds = examTests.stream()
//                        .map(Tests::getId)
//                        .collect(Collectors.toList());
//                
//                // Get results for this student for those tests
//                studentResults = testResultRepository.findByStudentIdAndTestIdIn(studentId, testIds);
//
//            } else if ("SUBJECT".equalsIgnoreCase(performanceType)) {
//                // Get all tests for this subject that belong to the institute
//                List<Tests> subjectTests = testRepository.findBySubjectIdAndInstitutesEmail(referenceId,
//                        instituteEmail);
//                if (subjectTests.isEmpty()) {
//                    throw new CustomException("No tests found for the given subject and institute", HttpStatus.NOT_FOUND);
//                }
//                List<Long> testIds = subjectTests.stream()
//                        .map(Tests::getId)
//                        .collect(Collectors.toList());
//                
//                // Get results for this student for those tests
//                studentResults = testResultRepository.findByStudentIdAndTestIdIn(studentId, testIds);
//
//            } else {
//                throw new CustomException("Invalid performance type. Use 'EXAM' or 'SUBJECT'", HttpStatus.BAD_REQUEST);
//            }
//
//            if (studentResults.isEmpty()) {
//                // Return empty performance instead of throwing exception
//                return getEmptyStudentPerformanceDto(studentId, performanceType, instituteEmail);
//            }
//
//            // Calculate performance metrics (negative marks considered)
//            PerformanceMetrics metrics = calculatePerformanceMetrics(studentResults);
//            List<StudentPerformanceDto.TestHistory> testHistory = generateTestHistory(studentResults);
//
//            Users student = userRepository.findById(studentId)
//                    .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
//
//            return StudentPerformanceDto.builder()
//                    .studentId(studentId)
//                    .studentName(student.getName())
//                    .studentEmail(student.getEmail())
//                    .analysisType(analysisType)
//                    .performanceType(performanceType.toUpperCase())
//                    .averageMarks(metrics.averageMarks)
//                    .highestMarks(metrics.highestMarks)
//                    .lowestMarks(metrics.lowestMarks)
//                    .improvementPercentage(metrics.improvementPercentage)
//                    .testHistory(testHistory)
//                    .totalParticipants(getTotalParticipantsForTests(studentResults))
//                    .instituteEmail(instituteEmail)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching student performance: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching student performance: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Helper method to return empty performance when no results found
//    private StudentPerformanceDto getEmptyStudentPerformanceDto(Long studentId, String performanceType, String instituteEmail) {
//        Users student = userRepository.findById(studentId)
//                .orElse(new Users()); // Create empty user if not found
//        
//        return StudentPerformanceDto.builder()
//                .studentId(studentId)
//                .studentName(student.getName() != null ? student.getName() : "Student")
//                .studentEmail(student.getEmail() != null ? student.getEmail() : "")
//                .analysisType("PERFORMANCE")
//                .performanceType(performanceType.toUpperCase())
//                .averageMarks(0.0)
//                .highestMarks(0.0)
//                .lowestMarks(0.0)
//                .improvementPercentage(0.0)
//                .testHistory(new ArrayList<>())
//                .totalParticipants(0)
//                .instituteEmail(instituteEmail)
////                .message("No test results found for this student")
//                .build();
//    }
//
//    // Helper method to get total participants across all tests
//    private Integer getTotalParticipantsForTests(List<StudentTestResult> studentResults) {
//        try {
//            // Get unique test IDs from student results
//            Set<Long> testIds = studentResults.stream()
//                    .map(StudentTestResult::getTestId)
//                    .collect(Collectors.toSet());
//            
//            int total = 0;
//            for (Long testId : testIds) {
//                Integer count = testResultRepository.countByTestId(testId);
//                if (count != null) {
//                    total += count;
//                }
//            }
//            return total;
//        } catch (Exception e) {
//            log.warn("Error counting total participants: {}", e.getMessage());
//            return 0;
//        }
//    }
//
//    // 3. Get Subject Performance in Exam
//    @Override
//    public StudentPerformanceDto getSubjectPerformanceInExam(Long studentId, Long testId) {
//        try {
//            log.info("Fetching subject performance for studentId: {}, testId: {}", studentId, testId);
//
//            Tests test = testRepository.findById(testId)
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//            StudentTestResult testResult = testResultRepository.findByStudentIdAndTestId(studentId, testId);
//            if (testResult == null) {
//                throw new CustomException("Test result not found for the student", HttpStatus.NOT_FOUND);
//            }
//
//            // Get all questions for this test grouped by subject
//            List<Questions> testQuestions = questionRepository.findByTestId(testId);
//            Map<Long, List<Questions>> questionsBySubject = testQuestions.stream()
//                    .filter(q -> q.getSubject() != null)
//                    .collect(Collectors.groupingBy(q -> q.getSubject().getId()));
//
//            // Calculate subject-wise performance (with negative marking)
//            Map<String, StudentPerformanceDto.SubjectPerformance> subjectPerformance = new HashMap<>();
//
//            for (Map.Entry<Long, List<Questions>> entry : questionsBySubject.entrySet()) {
//                Long subjectId = entry.getKey();
//                List<Questions> subjectQuestions = entry.getValue();
//
//                long totalQuestions = subjectQuestions.size();
//                long correctAnswers = testResult.getCurrectquestionIds() != null
//                        ? subjectQuestions.stream().filter(q -> testResult.getCurrectquestionIds().contains(q.getId()))
//                                .count()
//                        : 0;
//                long incorrectAnswers = testResult.getIncurrectquestionIds() != null ? subjectQuestions.stream()
//                        .filter(q -> testResult.getIncurrectquestionIds().contains(q.getId())).count() : 0;
//
//                double accuracy = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0;
//                double totalMarks = correctAnswers * test.getCorrectMark() - incorrectAnswers * test.getNegativeMark();
//
//                Subjects subject = subjectRepository.findById(subjectId)
//                        .orElseThrow(() -> new CustomException("Subject not found", HttpStatus.NOT_FOUND));
//
//                subjectPerformance.put(subject.getName(),
//                        StudentPerformanceDto.SubjectPerformance.builder()
//                                .averageMarks(totalMarks)
//                                .totalQuestions((int) totalQuestions)
//                                .correctAnswers((int) correctAnswers)
//                                .incorrectAnswers((int) incorrectAnswers)
//                                .accuracy(accuracy)
//                                .strengthLevel(calculateStrengthLevel(accuracy))
//                                .build());
//            }
//
//            Users student = userRepository.findById(studentId)
//                    .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
//
//            return StudentPerformanceDto.builder()
//                    .studentId(studentId)
//                    .studentName(student.getName())
//                    .studentEmail(student.getEmail())
//                    .testId(testId)
//                    .testName(test.getTestName())
//                    .analysisType("SUBJECT_ANALYSIS")
//                    .performanceType("EXAM_WISE")
//                    .subjectPerformance(subjectPerformance)
//                    .marksObtained(testResult.getTotalObtainedMarks())
//                    .totalMarks(calculateTotalMarks(test))
//                    .percentage(calculateTotalMarks(test) > 0
//                            ? (testResult.getTotalObtainedMarks() / calculateTotalMarks(test)) * 100
//                            : 0)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching subject performance in exam: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching subject performance in exam: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // 4. Get Student's Last Exam Rank
//    @Override
//    public StudentPerformanceDto getStudentLastExamRank(Long studentId, String instituteEmail) {
//        try {
//            log.info("Fetching last exam rank for studentId: {}, institute: {}", studentId, instituteEmail);
//
//            StudentTestResult lastResult = testResultRepository
//                    .findTopByStudentIdAndInstituteEmailOrderByTestAttemptedDateDesc(studentId, instituteEmail);
//
//            if (lastResult == null) {
//                throw new CustomException("No test results found for the student", HttpStatus.NOT_FOUND);
//            }
//
//            // Get all results for the same test to calculate rank (negative marks considered)
//            List<StudentTestResult> testResults = testResultRepository
//                    .findByTestIdAndInstituteEmail(lastResult.getTestId(), instituteEmail);
//            int rank = calculateRank(testResults, studentId);
//
//            Users student = userRepository.findById(studentId)
//                    .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
//
//            Tests test = testRepository.findById(lastResult.getTestId())
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//            return StudentPerformanceDto.builder()
//                    .studentId(studentId)
//                    .studentName(student.getName())
//                    .studentEmail(student.getEmail())
//                    .testId(lastResult.getTestId())
//                    .testName(lastResult.getTestName())
//                    .analysisType("RANK")
//                    .performanceType(test.getExamType().name())
//                    .rank(rank)
//                    .marksObtained(lastResult.getTotalObtainedMarks())
//                    .totalMarks(calculateTotalMarks(test))
//                    .percentage(calculateTotalMarks(test) > 0
//                            ? (lastResult.getTotalObtainedMarks() / calculateTotalMarks(test)) * 100
//                            : 0)
//                    .totalParticipants(testResults.size())
//                    .testDate(lastResult.getTestAttemptedDate())
//                    .instituteEmail(instituteEmail)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching last exam rank: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching last exam rank: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // 5. Get Test Leaderboard (with negative marks)
//    @Override
//    public StudentPerformanceDto getTestLeaderboard(Long testId, Integer topN) {
//        try {
//            log.info("Fetching leaderboard for testId: {}, top: {}", testId, topN);
//
//            Tests test = testRepository.findById(testId)
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//            List<StudentTestResult> testResults = testResultRepository
//                    .findByTestIdOrderByTotalObtainedMarksDesc(testId);
//
//            if (testResults.isEmpty()) {
//                throw new CustomException("No results found for this test", HttpStatus.NOT_FOUND);
//            }
//
//            List<StudentPerformanceDto.RankEntry> leaderboard = new ArrayList<>();
//            int rank = 1;
//
//            for (StudentTestResult result : testResults) {
//                if (topN != null && rank > topN)
//                    break;
//
//                Users student = userRepository.findById(result.getStudentId()).orElse(new Users());
//
//                double totalMarks = calculateTotalMarks(test);
//                double percentage = totalMarks > 0 ? (result.getTotalObtainedMarks() / totalMarks) * 100 : 0;
//
//                leaderboard.add(StudentPerformanceDto.RankEntry.builder()
//                        .rank(rank)
//                        .studentId(result.getStudentId())
//                        .studentName(student.getName())
//                        .studentEmail(student.getEmail())
//                        .marksObtained(result.getTotalObtainedMarks()) // Could be negative
//                        .percentage(percentage)
//                        .build());
//
//                rank++;
//            }
//
//            return StudentPerformanceDto.builder()
//                    .testId(testId)
//                    .testName(test.getTestName())
//                    .analysisType("LEADERBOARD")
//                    .performanceType(test.getExamType().name())
//                    .leaderboard(leaderboard)
//                    .totalParticipants(testResults.size())
//                    .testDate(testResults.get(0).getTestAttemptedDate())
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching test leaderboard: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching test leaderboard: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // 6. Get Subject Averages for Exam
//    @Override
//    public StudentPerformanceDto getSubjectAveragesForExam(Long testId) {
//        try {
//            log.info("Fetching subject averages for testId: {}", testId);
//
//            Tests test = testRepository.findById(testId)
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//            List<StudentTestResult> testResults = testResultRepository.findByTestId(testId);
//
//            if (testResults.isEmpty()) {
//                throw new CustomException("No results found for this test", HttpStatus.NOT_FOUND);
//            }
//
//            List<Questions> testQuestions = questionRepository.findByTestId(testId);
//            Map<Long, List<Questions>> questionsBySubject = testQuestions.stream()
//                    .filter(q -> q.getSubject() != null)
//                    .collect(Collectors.groupingBy(q -> q.getSubject().getId()));
//
//            Map<String, StudentPerformanceDto.SubjectPerformance> subjectAverages = new HashMap<>();
//
//            for (Map.Entry<Long, List<Questions>> entry : questionsBySubject.entrySet()) {
//                Long subjectId = entry.getKey();
//                List<Questions> subjectQuestions = entry.getValue();
//
//                double totalMarks = 0;
//                int studentCount = 0;
//
//                for (StudentTestResult result : testResults) {
//                    long correctAnswers = result.getCurrectquestionIds() != null
//                            ? subjectQuestions.stream().filter(q -> result.getCurrectquestionIds().contains(q.getId()))
//                                    .count()
//                            : 0;
//                    long incorrectAnswers = result.getIncurrectquestionIds() != null ? subjectQuestions.stream()
//                            .filter(q -> result.getIncurrectquestionIds().contains(q.getId())).count() : 0;
//
//                    double subjectMarks = correctAnswers * test.getCorrectMark()
//                            - incorrectAnswers * test.getNegativeMark();
//                    totalMarks += subjectMarks;
//                    studentCount++;
//                }
//
//                double averageMarks = studentCount > 0 ? totalMarks / studentCount : 0;
//
//                Subjects subject = subjectRepository.findById(subjectId)
//                        .orElseThrow(() -> new CustomException("Subject not found", HttpStatus.NOT_FOUND));
//
//                subjectAverages.put(subject.getName(),
//                        StudentPerformanceDto.SubjectPerformance.builder()
//                                .averageMarks(averageMarks)
//                                .totalQuestions(subjectQuestions.size())
//                                .correctAnswers(0)
//                                .incorrectAnswers(0)
//                                .accuracy(0.0)
//                                .strengthLevel("")
//                                .build());
//            }
//
//            return StudentPerformanceDto.builder()
//                    .testId(testId)
//                    .testName(test.getTestName())
//                    .analysisType("SUBJECT_AVERAGES")
//                    .performanceType(test.getExamType().name())
//                    .subjectPerformance(subjectAverages)
//                    .totalParticipants(testResults.size())
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error fetching subject averages: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching subject averages: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Submit test and calculate result with negative marking
//    @Override
//    @Transactional
//    public StudentTestResultDto submitTestAndCalculateResult(TestSubmissionDto testSubmissionDto) {
//        try {
//            log.info("Submitting test for studentId: {}, testId: {}", testSubmissionDto.getStudentId(),
//                    testSubmissionDto.getTestId());
//
//            // Validate input
//            validateTestSubmission(testSubmissionDto);
//
//            // Get test details
//            Tests test = testRepository.findById(testSubmissionDto.getTestId())
//                    .orElseThrow(() -> new CustomException("Test not found with id: " + testSubmissionDto.getTestId(),
//                            HttpStatus.NOT_FOUND));
//
//            // Validate student exists
//            Users student = userRepository.findById(testSubmissionDto.getStudentId()).orElseThrow(
//                    () -> new CustomException("Student not found with id: " + testSubmissionDto.getStudentId(),
//                            HttpStatus.NOT_FOUND));
//
//            // Validate institute exists
//            userRepository.findByEmail(testSubmissionDto.getInstituteEmail())
//                    .orElseThrow(() -> new CustomException(
//                            "Institute not found with email: " + testSubmissionDto.getInstituteEmail(),
//                            HttpStatus.NOT_FOUND));
//
//            // Calculate test result with negative marking
//            TestResultCalculationDto calculatedResult = calculateTestResult(testSubmissionDto);
//
//            // Get the calculated marks (can be negative)
//            double totalObtainedMarks = calculatedResult.getTotalObtainedMarks();
//            
//            // Format time spent
//            String timeSpentFormatted = formatTimeSpent(testSubmissionDto.getTotalTimeSpent());
//            
//            // Create DTO with the actual marks (negative if applicable)
//            StudentTestResultDto resultDto = StudentTestResultDto.builder()
//                    .testId(testSubmissionDto.getTestId())
//                    .testName(test.getTestName())
//                    .studentId(testSubmissionDto.getStudentId())
//                    .studentEmail(student.getEmail())
//                    .instituteEmail(testSubmissionDto.getInstituteEmail())
//                    .noOfCorrectAns((double) calculatedResult.getCorrectCount())
//                    .noOfInCorrectAns((double) calculatedResult.getIncorrectCount())
//                    .currectquestionIds(calculatedResult.getCorrectQuestionIds())
//                    .incurrectquestionIds(calculatedResult.getIncorrectQuestionIds())
//                    .totalObtainedMarks(totalObtainedMarks) // Negative if applicable
//                    .testAttemptedDate(LocalDate.now().toString())
//                    .remark(calculateRemarkBasedOnNegativeMarks(totalObtainedMarks, test))
//                    .timeSpent(timeSpentFormatted)
//                    .examType(testSubmissionDto.getExamType())
//                    .build();
//
//            // Save the result
//            StudentTestResultDto savedResult = createTestResult(resultDto);
//
//            // Calculate and assign rank (ranking should handle negative marks)
//            Integer currentRank = rankingService.calculateAndSaveRank(
//                    testSubmissionDto.getTestId(),
//                    testSubmissionDto.getStudentId(),
//                    totalObtainedMarks, // Pass the actual marks (could be negative)
//                    savedResult.getNoOfCorrectAns().intValue(),
//                    timeSpentFormatted,
//                    testSubmissionDto.getExamType()
//            );
//
//            // Update the saved result with additional info
//            savedResult.setCurrentRank(currentRank);
//            
//            // Get total participants for this test
//            Integer totalParticipants = testRankingRepository.findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(
//                    testSubmissionDto.getTestId()).size();
//            savedResult.setTotalParticipants(totalParticipants);
//            
//            // Ensure time spent is set
//            savedResult.setTimeSpent(timeSpentFormatted);
//
//            log.info("Test submitted successfully. Student ID: {}, Marks: {} (negative allowed), Rank: {}", 
//                    testSubmissionDto.getStudentId(), totalObtainedMarks, currentRank);
//            
//            return savedResult;
//
//        } catch (CustomException e) {
//            log.error("Custom exception in submitTestAndCalculateResult: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error in submitTestAndCalculateResult: {}", e.getMessage(), e);
//            throw new CustomException("Error submitting test: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Calculate test result with negative marking
//    @Override
//    public TestResultCalculationDto calculateTestResult(TestSubmissionDto testSubmissionDto) {
//        try {
//            log.info("Calculating test result for studentId: {}, testId: {}", testSubmissionDto.getStudentId(),
//                    testSubmissionDto.getTestId());
//
//            // Get test details
//            Tests test = testRepository.findById(testSubmissionDto.getTestId())
//                    .orElseThrow(() -> new CustomException("Test not found with id: " + testSubmissionDto.getTestId(),
//                            HttpStatus.NOT_FOUND));
//
//            // Get all questions for the test
//            List<Questions> testQuestions = questionRepository.findByTestId(testSubmissionDto.getTestId());
//
//            // Get all options for the questions
//            List<Long> questionIds = testQuestions.stream().map(Questions::getId).collect(Collectors.toList());
//            List<Options> allOptions = optionRepository.findByQuestionIds(questionIds);
//            Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
//                    .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
//
//            // Calculate results
//            List<Long> correctQuestionIds = new ArrayList<>();
//            List<Long> incorrectQuestionIds = new ArrayList<>();
//            List<Long> unansweredQuestionIds = new ArrayList<>();
//            int correctCount = 0;
//            int incorrectCount = 0;
//            int unansweredCount = 0;
//
//            // Create a map of student's attempts for quick lookup
//            Map<Long, QuestionAttemptDto> studentAttemptsMap = testSubmissionDto.getQuestionAttempts().stream()
//                    .collect(Collectors.toMap(QuestionAttemptDto::getQuestionId, attempt -> attempt));
//
//            // Evaluate each question
//            for (Questions question : testQuestions) {
//                QuestionAttemptDto studentAttempt = studentAttemptsMap.get(question.getId());
//                List<Options> questionOptions = optionsByQuestionId.get(question.getId());
//
//                if (studentAttempt == null || studentAttempt.getSelectedOptionId() == null) {
//                    // Unanswered question
//                    unansweredQuestionIds.add(question.getId());
//                    unansweredCount++;
//                } else {
//                    // Find the selected option and check if it's correct
//                    Optional<Options> selectedOption = questionOptions.stream()
//                            .filter(option -> option.getId().equals(studentAttempt.getSelectedOptionId()))
//                            .findFirst();
//
//                    if (selectedOption.isPresent() && Boolean.TRUE.equals(selectedOption.get().getIsCorrect())) {
//                        // Correct answer
//                        correctQuestionIds.add(question.getId());
//                        correctCount++;
//                    } else {
//                        // Incorrect answer
//                        incorrectQuestionIds.add(question.getId());
//                        incorrectCount++;
//                    }
//                }
//            }
//
//            // Calculate marks - ALLOW NEGATIVE VALUES
//            double correctMarks = correctCount * test.getCorrectMark();
//            double negativeMarks = incorrectCount * test.getNegativeMark();
//            double totalObtainedMarks = correctMarks - negativeMarks; // This can be negative
//
//            // Calculate accuracy (only for attempted questions)
//            int attemptedQuestions = correctCount + incorrectCount;
//            double accuracy = attemptedQuestions > 0 ? (correctCount * 100.0) / attemptedQuestions : 0;
//
//            // Determine performance level (handles negative marks)
//            String performanceLevel = calculatePerformanceLevel(totalObtainedMarks, test, accuracy);
//
//            log.info("Test calculation completed - Correct: {}, Incorrect: {}, Unanswered: {}, Total Marks: {} (can be negative)",
//                    correctCount, incorrectCount, unansweredCount, totalObtainedMarks);
//
//            return TestResultCalculationDto.builder()
//                    .testId(testSubmissionDto.getTestId())
//                    .studentId(testSubmissionDto.getStudentId())
//                    .totalQuestions(testQuestions.size())
//                    .correctCount(correctCount)
//                    .incorrectCount(incorrectCount)
//                    .unansweredCount(unansweredCount)
//                    .correctQuestionIds(correctQuestionIds)
//                    .incorrectQuestionIds(incorrectQuestionIds)
//                    .unansweredQuestionIds(unansweredQuestionIds)
//                    .correctMarks(correctMarks)
//                    .negativeMarks(negativeMarks)
//                    .totalObtainedMarks(totalObtainedMarks) // Can be negative
//                    .accuracy(accuracy)
//                    .performanceLevel(performanceLevel)
//                    .test(test)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error calculating test result: {}", e.getMessage(), e);
//            throw new CustomException("Error calculating test result: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Helper methods
//
//    private void validateTestSubmission(TestSubmissionDto testSubmissionDto) {
//        if (testSubmissionDto.getTestId() == null) {
//            throw new CustomException("Test ID is required", HttpStatus.BAD_REQUEST);
//        }
//        if (testSubmissionDto.getStudentId() == null) {
//            throw new CustomException("Student ID is required", HttpStatus.BAD_REQUEST);
//        }
//        if (testSubmissionDto.getInstituteEmail() == null || testSubmissionDto.getInstituteEmail().trim().isEmpty()) {
//            throw new CustomException("Institute email is required", HttpStatus.BAD_REQUEST);
//        }
//        if (testSubmissionDto.getQuestionAttempts() == null) {
//            throw new CustomException("Question attempts are required", HttpStatus.BAD_REQUEST);
//        }
//
//        // Validate that student exists
//        userRepository.findById(testSubmissionDto.getStudentId())
//                .orElseThrow(() -> new CustomException("Student not found with id: " + testSubmissionDto.getStudentId(),
//                        HttpStatus.NOT_FOUND));
//
//        // Validate that institute exists
//        userRepository.findByEmail(testSubmissionDto.getInstituteEmail())
//                .orElseThrow(() -> new CustomException(
//                        "Institute not found with email: " + testSubmissionDto.getInstituteEmail(),
//                        HttpStatus.NOT_FOUND));
//    }
//
//    private String calculatePerformanceLevel(double totalObtainedMarks, Tests test, double accuracy) {
//        if (totalObtainedMarks < 0) {
//            return "Negative Score";
//        }
//        
//        if (test == null || test.getTotalMarks() == null || test.getTotalMarks() == 0) {
//            return "Average";
//        }
//        
//        double percentage = (totalObtainedMarks / test.getTotalMarks()) * 100;
//        
//        if (percentage >= 90) return "Excellent";
//        else if (percentage >= 75) return "Very Good";
//        else if (percentage >= 60) return "Good";
//        else if (percentage >= 40) return "Average";
//        else return "Needs Improvement";
//    }
//
//    private String formatTimeSpent(Integer totalSeconds) {
//        if (totalSeconds == null) return "0m 0s";
//        
//        int hours = totalSeconds / 3600;
//        int minutes = (totalSeconds % 3600) / 60;
//        int seconds = totalSeconds % 60;
//        
//        if (hours > 0) {
//            return String.format("%dh %dm %ds", hours, minutes, seconds);
//        } else {
//            return String.format("%dm %ds", minutes, seconds);
//        }
//    }
//
//    // Get rank for student
//    private Integer getRankForStudent(Long testId, Long studentId) {
//        try {
//            List<StudentTestResult> allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(testId);
//            
//            for (int i = 0; i < allResults.size(); i++) {
//                if (allResults.get(i).getStudentId().equals(studentId)) {
//                    return i + 1;
//                }
//            }
//            return null;
//        } catch (Exception e) {
//            log.warn("Error fetching rank for student: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    // Get total participants
////    private Integer getTotalParticipants(Long testId) {
////        try {
////            return testResultRepository.countByTestId(testId);
////        } catch (Exception e) {
////            log.warn("Error counting participants: {}", e.getMessage());
////            return null;
////        }
////    }
//
//    // Helper classes for ranking calculations
//    private Map<Long, StudentMarks> calculateStudentMarks(List<StudentTestResult> results) {
//        Map<Long, StudentMarks> studentMarksMap = new HashMap<>();
//        for (StudentTestResult result : results) {
//            StudentMarks marks = studentMarksMap.getOrDefault(result.getStudentId(),
//                    new StudentMarks(result.getStudentId(), result.getStudentEmail()));
//            marks.addMarks(result.getTotalObtainedMarks());
//            studentMarksMap.put(result.getStudentId(), marks);
//        }
//        return studentMarksMap;
//    }
//
//    private List<StudentPerformanceDto.RankEntry> generateRanking(Map<Long, StudentMarks> studentMarksMap,
//            Integer topN) {
//        return studentMarksMap.values().stream()
//                .sorted((s1, s2) -> Double.compare(s2.getTotalMarks(), s1.getTotalMarks()))
//                .map(marks -> {
//                    int rank = studentMarksMap.values().stream()
//                            .filter(s -> s.getTotalMarks() > marks.getTotalMarks())
//                            .collect(Collectors.toList()).size() + 1;
//
//                    return StudentPerformanceDto.RankEntry.builder()
//                            .rank(rank)
//                            .studentId(marks.getStudentId())
//                            .studentName(getStudentName(marks.getStudentId()))
//                            .studentEmail(marks.getStudentEmail())
//                            .marksObtained(marks.getTotalMarks()) // Could be negative
//                            .build();
//                })
//                .limit(topN != null ? topN : Long.MAX_VALUE)
//                .collect(Collectors.toList());
//    }
//
//    private PerformanceMetrics calculatePerformanceMetrics(List<StudentTestResult> results) {
//        double totalMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).sum();
//        double averageMarks = totalMarks / results.size();
//        double highestMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).max().orElse(0);
//        double lowestMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).min().orElse(0);
//
//        double improvementPercentage = 0.0;
//        if (results.size() > 1) {
//            StudentTestResult firstTest = results.get(0);
//            StudentTestResult lastTest = results.get(results.size() - 1);
//            improvementPercentage = ((lastTest.getTotalObtainedMarks() - firstTest.getTotalObtainedMarks())
//                    / firstTest.getTotalObtainedMarks()) * 100;
//        }
//
//        return new PerformanceMetrics(averageMarks, highestMarks, lowestMarks, improvementPercentage);
//    }
//
//    private List<StudentPerformanceDto.TestHistory> generateTestHistory(List<StudentTestResult> results) {
//        return results.stream()
//                .map(result -> StudentPerformanceDto.TestHistory.builder()
//                        .testId(result.getTestId())
//                        .testName(result.getTestName())
//                        .marksObtained(result.getTotalObtainedMarks())
//                        .testDate(result.getTestAttemptedDate())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    private int calculateRank(List<StudentTestResult> results, Long studentId) {
//        List<StudentTestResult> sortedResults = results.stream()
//                .sorted((r1, r2) -> Double.compare(r2.getTotalObtainedMarks(), r1.getTotalObtainedMarks()))
//                .collect(Collectors.toList());
//
//        for (int i = 0; i < sortedResults.size(); i++) {
//            if (sortedResults.get(i).getStudentId().equals(studentId)) {
//                return i + 1;
//            }
//        }
//        return -1;
//    }
//
//    private String getStudentName(Long studentId) {
//        return userRepository.findById(studentId).map(Users::getName).orElse("Unknown Student");
//    }
//
//    private Double calculateTotalMarks(Tests test) {
//        if (test == null) return 0.0;
//        
//        // Get total questions for the test
//        List<Questions> testQuestions = questionRepository.findByTestId(test.getId());
//        return testQuestions.stream()
//                .mapToDouble(q -> q.getMarks() != null ? q.getMarks() : test.getCorrectMark())
//                .sum();
//    }
//
//    private String calculateStrengthLevel(double accuracy) {
//        if (accuracy >= 80) return "Strong";
//        else if (accuracy >= 60) return "Average";
//        else return "Weak";
//    }
//
//    // Helper classes
//    private static class StudentMarks {
//        private Long studentId;
//        private String studentEmail;
//        private Double totalMarks = 0.0;
//
//        public StudentMarks(Long studentId, String studentEmail) {
//            this.studentId = studentId;
//            this.studentEmail = studentEmail;
//        }
//
//        public void addMarks(Double marks) {
//            this.totalMarks += marks;
//        }
//
//        public Long getStudentId() {
//            return studentId;
//        }
//
//        public String getStudentEmail() {
//            return studentEmail;
//        }
//
//        public Double getTotalMarks() {
//            return totalMarks;
//        }
//    }
//
//    private static class PerformanceMetrics {
//        double averageMarks;
//        double highestMarks;
//        double lowestMarks;
//        double improvementPercentage;
//
//        public PerformanceMetrics(double averageMarks, double highestMarks, double lowestMarks,
//                double improvementPercentage) {
//            this.averageMarks = averageMarks;
//            this.highestMarks = highestMarks;
//            this.lowestMarks = lowestMarks;
//            this.improvementPercentage = improvementPercentage;
//        }
//    }
//    
//    
//    
//    
//    @Override
//    public List<RankingResponseDto> getTopRankListForLatestTest(String instituteEmail, Integer topN) {
//        try {
//            log.info("Fetching top {} ranks for latest test for institute: {}", topN, instituteEmail);
//            
//            // 1. Find the latest test for this institute
//            Tests latestTest = getLatestTestForInstitute(instituteEmail);
//            if (latestTest == null) {
//                log.warn("No tests found for institute: {}", instituteEmail);
//                return new ArrayList<>();
//            }
//            
//            log.info("Latest test found: {} (ID: {})", latestTest.getTestName(), latestTest.getId());
//            
//            // 2. Get all results for this test, ordered by marks (descending)
//            List<StudentTestResult> allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(latestTest.getId());
//            
//            if (allResults.isEmpty()) {
//                log.warn("No results found for test ID: {}", latestTest.getId());
//                return new ArrayList<>();
//            }
//            
//            // 3. Prepare top rank list
//            List<RankingResponseDto> topRankList = new ArrayList<>();
//            int rank = 1;
//            
//            for (StudentTestResult result : allResults) {
//                if (topN != null && rank > topN) {
//                    break;
//                }
//                
//                RankingResponseDto rankDto = createRankingResponseDto(result, rank, latestTest);
//                topRankList.add(rankDto);
//                rank++;
//            }
//            
//            log.info("Successfully fetched top {} ranks for latest test", topRankList.size());
//            return topRankList;
//            
//        } catch (Exception e) {
//            log.error("Error fetching top rank list for latest test: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching top rank list: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Helper method to create RankingResponseDto
//    private RankingResponseDto createRankingResponseDto(StudentTestResult result, int rank, Tests test) {
//        try {
//            // Get student details
//            Users student = userRepository.findById(result.getStudentId())
//                    .orElse(new Users()); // Create empty user if not found
//            
//            // Count correct answers
//            Integer correctAnswers = result.getNoOfCorrectAns() != null ? 
//                    result.getNoOfCorrectAns().intValue() : 0;
//            
//            return RankingResponseDto.builder()
//                    .testId(result.getTestId())
//                    .studentId(result.getStudentId())
//                    .studentEmail(result.getStudentEmail())
//                    .studentName(student.getName() != null ? student.getName() : "Unknown")
//                    .rank(rank)
//                    .totalMarks(result.getTotalObtainedMarks())
//                    .correctAnswers(correctAnswers)
//                    .timeSpent(result.getTimeSpent() != null ? result.getTimeSpent() : "N/A")
//                    .examType(test.getExamType() != null ? test.getExamType().name() : "Unknown")
//                    .testName(test.getTestName())
//                    .build();
//                    
//        } catch (Exception e) {
//            log.error("Error creating RankingResponseDto: {}", e.getMessage());
//            return RankingResponseDto.builder()
//                    .testId(result.getTestId())
//                    .studentId(result.getStudentId())
//                    .studentEmail(result.getStudentEmail())
//                    .rank(rank)
//                    .totalMarks(result.getTotalObtainedMarks())
//                    .build();
//        }
//    }
//
////    // Helper method to find the latest test
////    private Tests getLatestTestForInstitute(String instituteEmail) {
////        try {
////            // Method 1: Get latest test from test results (most reliable)
////            List<StudentTestResult> testResults = testResultRepository.findByInstituteEmailOrderByTestAttemptedDateDesc(instituteEmail);
////            
////            if (!testResults.isEmpty()) {
////                Long latestTestId = testResults.get(0).getTestId();
////                return testRepository.findById(latestTestId).orElse(null);
////            }
////            
////            // Method 2: Get latest test from test repository
////            List<Tests> instituteTests = testRepository.findByInstituteEmail(instituteEmail);
////            
////            if (!instituteTests.isEmpty()) {
////                // Find the test with most recent creation date
////                return instituteTests.stream()
////                        .filter(test -> test.getCreatedAt() != null)
////                        .max((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
////                        .orElse(instituteTests.get(0));
////            }
////            
////            return null;
////            
////        } catch (Exception e) {
////            log.error("Error finding latest test: {}", e.getMessage());
////            return null;
////        }
////    }
//    
//    
//    private Tests getLatestTestForInstitute(String instituteEmail) {
//        try {
//            // Method 1: Gets the latest test based on test attempt date from StudentTestResult
//            List<StudentTestResult> testResults = testResultRepository
//                .findByInstituteEmailOrderByTestAttemptedDateDesc(instituteEmail);
//            
//            if (!testResults.isEmpty()) {
//                Long latestTestId = testResults.get(0).getTestId(); // Gets the most recent test attempt
//                return testRepository.findById(latestTestId).orElse(null);
//            }
//            
//            // Method 2: Falls back to latest test creation date
//            List<Tests> instituteTests = testRepository.findByInstituteEmail(instituteEmail);
//            
//            if (!instituteTests.isEmpty()) {
//                return instituteTests.stream()
//                        .filter(test -> test.getCreatedAt() != null)
//                        .max((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
//                        .orElse(instituteTests.get(0));
//            }
//            
//            return null;
//        } catch (Exception e) {
//            log.error("Error finding latest test: {}", e.getMessage());
//            return null;
//        }
//    }
//    
//    
//    
//    @Override
//    public PerformanceAnalyticsDto getMonthlyPerformance(Long studentId, LocalDate monthDate) {
//        try {
//            log.info("Getting monthly performance for student: {}, month: {}", studentId, monthDate);
//            
//            // Calculate month boundaries
//            LocalDate startDate = (monthDate != null) ? monthDate.withDayOfMonth(1) : 
//                    LocalDate.now().withDayOfMonth(1);
//            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
//            
//            // Get results for the month
//            List<StudentTestResult> monthlyResults = getTestResultsByDateRange(studentId, startDate, endDate);
//            
//            // Calculate weekly breakdown
//            List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = 
//                    calculateWeeklyBreakdown(monthlyResults, startDate);
//            
//            // Build comprehensive response
//            return buildPerformanceAnalytics(
//                    studentId, 
//                    monthlyResults, 
//                    startDate, 
//                    endDate, 
//                    "MONTHLY", 
//                    weeklyBreakdown
//            );
//            
//        } catch (Exception e) {
//            log.error("Error fetching monthly performance: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching monthly performance: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    
//    // 2. Get Weekly Performance
//    @Override
//    public PerformanceAnalyticsDto getWeeklyPerformance(Long studentId, LocalDate weekStart) {
//        try {
//            log.info("Getting weekly performance for student: {}, weekStart: {}", studentId, weekStart);
//            
//            // Calculate week boundaries
//            LocalDate startDate = (weekStart != null) ? weekStart : 
//                    LocalDate.now().with(DayOfWeek.MONDAY);
//            LocalDate endDate = startDate.plusDays(6);
//            
//            // Get results for the week
//            List<StudentTestResult> weeklyResults = getTestResultsByDateRange(studentId, startDate, endDate);
//            
//            // Calculate daily breakdown
//            List<PerformanceAnalyticsDto.PerformanceBreakdown> dailyBreakdown = 
//                    calculateDailyBreakdown(weeklyResults);
//            
//            // Build comprehensive response
//            return buildPerformanceAnalytics(
//                    studentId, 
//                    weeklyResults, 
//                    startDate, 
//                    endDate, 
//                    "WEEKLY", 
//                    dailyBreakdown
//            );
//            
//        } catch (Exception e) {
//            log.error("Error fetching weekly performance: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching weekly performance: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    
//    // 3. Get Last Month Performance (for current month 1st date)
//    @Override
//    public PerformanceAnalyticsDto getLastMonthPerformance(Long studentId) {
//        try {
//            log.info("Getting last month performance for student: {}", studentId);
//            
//            // Calculate last month boundaries
//            LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
//            LocalDate lastDayOfLastMonth = firstDayOfCurrentMonth.minusDays(1);
//            LocalDate firstDayOfLastMonth = lastDayOfLastMonth.withDayOfMonth(1);
//            
//            // Get results for last month
//            List<StudentTestResult> lastMonthResults = getTestResultsByDateRange(
//                    studentId, firstDayOfLastMonth, lastDayOfLastMonth);
//            
//            // Calculate weekly breakdown for last month
//            List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = 
//                    calculateWeeklyBreakdown(lastMonthResults, firstDayOfLastMonth);
//            
//            // Build comprehensive response
//            PerformanceAnalyticsDto response = buildPerformanceAnalytics(
//                    studentId, 
//                    lastMonthResults, 
//                    firstDayOfLastMonth, 
//                    lastDayOfLastMonth, 
//                    "MONTHLY", 
//                    weeklyBreakdown
//            );
//            
//            // Set period label
//            response.setPeriodLabel(firstDayOfLastMonth.getMonth().toString() + " " + 
//                    firstDayOfLastMonth.getYear());
//            
//            return response;
//            
//        } catch (Exception e) {
//            log.error("Error fetching last month performance: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching last month performance: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    
//    // 4. Get Last Month Top Ranks (Institute-wise)
//    @Override
//    public PerformanceAnalyticsDto getLastMonthTopRanks(String instituteEmail, Integer topN) {
//        try {
//            log.info("Getting last month top ranks for institute: {}, top: {}", instituteEmail, topN);
//            
//            // Calculate last month boundaries
//            LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
//            LocalDate lastDayOfLastMonth = firstDayOfCurrentMonth.minusDays(1);
//            LocalDate firstDayOfLastMonth = lastDayOfLastMonth.withDayOfMonth(1);
//            
//            // Get all results for institute in last month
//            List<StudentTestResult> lastMonthResults = testResultRepository
//                    .findByInstituteEmailAndTestAttemptedDateBetween(
//                            instituteEmail, firstDayOfLastMonth, lastDayOfLastMonth);
//            
//            if (lastMonthResults.isEmpty()) {
//                throw new CustomException("No results found for last month", HttpStatus.NOT_FOUND);
//            }
//            
//            // Find the most popular test in last month
//            Long mostPopularTestId = findMostPopularTest(lastMonthResults);
//            
//            // Get top ranks for that test
//            List<PerformanceAnalyticsDto.RankEntry> topRankers = getTopRankersForTest(
//                    mostPopularTestId, topN != null ? topN : 10);
//            
//            // Get test details
//            Tests test = testRepository.findById(mostPopularTestId)
//                    .orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//            
//            // Build response focused on ranking
//            return PerformanceAnalyticsDto.builder()
//                    .instituteEmail(instituteEmail)
//                    .periodType("MONTHLY") 
//                    .startDate(firstDayOfLastMonth)
//                    .endDate(lastDayOfLastMonth)
//                    .periodLabel("Top Ranks - " + firstDayOfLastMonth.getMonth().toString())
//                    .totalTests(lastMonthResults.size())
////                    .totalParticipants(getTotalParticipants(mostPopularTestId))
//                    .topRankers(topRankers)
//                    .testHistory(List.of(
//                            PerformanceAnalyticsDto.TestResultSummary.builder()
//                                    .testId(test.getId())
//                                    .testName(test.getTestName())
//                                    .examType(test.getExamType().name())
//                                    .totalMarks(calculateTotalMarks(test))
//                                    .build()
//                    ))
//                    .build();
//            
//        } catch (Exception e) {
//            log.error("Error fetching last month top ranks: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching last month top ranks: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    
//    // Helper Methods
//    
//    private List<StudentTestResult> getTestResultsByDateRange(Long studentId, 
//            LocalDate startDate, LocalDate endDate) {
//        try {
//            // Try repository method if available
//            return testResultRepository.findByStudentIdAndTestAttemptedDateBetween(
//                    studentId, startDate, endDate);
//        } catch (Exception e) {
//            // Fallback: manual filtering
//            log.warn("Using manual filtering for date range query");
//            return testResultRepository.findByStudentId(studentId).stream()
//                    .filter(result -> result.getTestAttemptedDate() != null &&
//                            !result.getTestAttemptedDate().isBefore(startDate) &&
//                            !result.getTestAttemptedDate().isAfter(endDate))
//                    .collect(Collectors.toList());
//        }
//    }
//    
//    private List<PerformanceAnalyticsDto.PerformanceBreakdown> calculateWeeklyBreakdown(
//            List<StudentTestResult> monthlyResults, LocalDate monthStart) {
//        
//        // Group results by week
//        Map<Integer, List<StudentTestResult>> resultsByWeek = monthlyResults.stream()
//                .collect(Collectors.groupingBy(result -> 
//                        (result.getTestAttemptedDate().getDayOfMonth() - 1) / 7 + 1));
//        
//        List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = new ArrayList<>();
//        
//        // Calculate metrics for each week
//        resultsByWeek.forEach((weekNumber, weekResults) -> {
//            double weekTotalMarks = weekResults.stream()
//                    .mapToDouble(StudentTestResult::getTotalObtainedMarks)
//                    .sum();
//            double weekAvgMarks = weekResults.size() > 0 ? weekTotalMarks / weekResults.size() : 0;
//            
//            // Calculate accuracy if needed
//            int correctAnswers = weekResults.stream()
//                    .mapToInt(result -> result.getNoOfCorrectAns() != null ? 
//                            result.getNoOfCorrectAns().intValue() : 0)
//                    .sum();
//            int totalAttempted = correctAnswers + weekResults.stream()
//                    .mapToInt(result -> result.getNoOfInCorrectAns() != null ? 
//                            result.getNoOfInCorrectAns().intValue() : 0)
//                    .sum();
//            double accuracy = totalAttempted > 0 ? (correctAnswers * 100.0) / totalAttempted : 0;
//            
//            weeklyBreakdown.add(PerformanceAnalyticsDto.PerformanceBreakdown.builder()
//                    .periodLabel("Week " + weekNumber)
//                    .testCount(weekResults.size())
//                    .averageMarks(weekAvgMarks)
//                    .totalMarks(weekTotalMarks)
//                    .correctAnswers(correctAnswers)
//                    .accuracy(accuracy)
//                    .build());
//        });
//        
//        // Sort by week number
//        weeklyBreakdown.sort(Comparator.comparing(b -> 
//                Integer.parseInt(b.getPeriodLabel().replace("Week ", ""))));
//        
//        return weeklyBreakdown;
//    }
//    
//    private List<PerformanceAnalyticsDto.PerformanceBreakdown> calculateDailyBreakdown(
//            List<StudentTestResult> weeklyResults) {
//        
//        // Group results by day of week
//        Map<DayOfWeek, List<StudentTestResult>> resultsByDay = weeklyResults.stream()
//                .collect(Collectors.groupingBy(result -> 
//                        result.getTestAttemptedDate().getDayOfWeek()));
//        
//        List<PerformanceAnalyticsDto.PerformanceBreakdown> dailyBreakdown = new ArrayList<>();
//        
//        // Initialize for all days
//        for (DayOfWeek day : DayOfWeek.values()) {
//            List<StudentTestResult> dayResults = resultsByDay.getOrDefault(day, new ArrayList<>());
//            
//            double dayTotalMarks = dayResults.stream()
//                    .mapToDouble(StudentTestResult::getTotalObtainedMarks)
//                    .sum();
//            double dayAvgMarks = dayResults.size() > 0 ? dayTotalMarks / dayResults.size() : 0;
//            
//            dailyBreakdown.add(PerformanceAnalyticsDto.PerformanceBreakdown.builder()
//                    .periodLabel(day.toString())
//                    .testCount(dayResults.size())
//                    .averageMarks(dayAvgMarks)
//                    .totalMarks(dayTotalMarks)
//                    .build());
//        }
//        
//        return dailyBreakdown;
//    }
//    
//    private PerformanceAnalyticsDto buildPerformanceAnalytics(
//            Long studentId,
//            List<StudentTestResult> results,
//            LocalDate startDate,
//            LocalDate endDate,
//            String periodType,
//            List<PerformanceAnalyticsDto.PerformanceBreakdown> breakdown) {
//        
//        if (results.isEmpty()) {
//            return buildEmptyPerformanceAnalytics(studentId, startDate, endDate, periodType);
//        }
//        
//        // Get student details
//        Users student = userRepository.findById(studentId)
//                .orElse(new Users());
//        
//        // Calculate basic metrics
//        int totalTests = results.size();
//        double totalMarks = results.stream()
//                .mapToDouble(StudentTestResult::getTotalObtainedMarks)
//                .sum();
//        double avgMarks = totalTests > 0 ? totalMarks / totalTests : 0;
//        double highestMarks = results.stream()
//                .mapToDouble(StudentTestResult::getTotalObtainedMarks)
//                .max()
//                .orElse(0);
//        double lowestMarks = results.stream()
//                .mapToDouble(StudentTestResult::getTotalObtainedMarks)
//                .min()
//                .orElse(0);
//        
//        // Calculate improvement percentage if multiple tests
//        double improvementPercentage = 0.0;
//        if (results.size() > 1) {
//            StudentTestResult firstTest = results.get(0);
//            StudentTestResult lastTest = results.get(results.size() - 1);
//            if (firstTest.getTotalObtainedMarks() != 0) {
//                improvementPercentage = ((lastTest.getTotalObtainedMarks() - firstTest.getTotalObtainedMarks())
//                        / firstTest.getTotalObtainedMarks()) * 100;
//            }
//        }
//        
//        // Build test history
//        List<PerformanceAnalyticsDto.TestResultSummary> testHistory = results.stream()
//                .map(result -> {
//                    Tests test = testRepository.findById(result.getTestId()).orElse(new Tests());
//                    return PerformanceAnalyticsDto.TestResultSummary.builder()
//                            .testId(result.getTestId())
//                            .testName(result.getTestName())
//                            .marksObtained(result.getTotalObtainedMarks())
//                            .totalMarks(calculateTotalMarks(test))
//                            .testDate(result.getTestAttemptedDate())
//                            .rank(getRankForStudent(result.getTestId(), studentId))
//                            .examType(test.getExamType() != null ? test.getExamType().name() : "")
//                            .timeSpent(result.getTimeSpent())
//                            .build();
//                })
//                .collect(Collectors.toList());
//        
//        // Calculate current rank (average rank across all tests)
//        Integer currentRank = calculateAverageRank(results, studentId);
//        
//        // Build subject performance if needed
//        Map<String, PerformanceAnalyticsDto.SubjectPerformance> subjectPerformance = 
//                calculateSubjectPerformance(results);
//        
//        return PerformanceAnalyticsDto.builder()
//                .studentId(studentId)
//                .studentName(student.getName() != null ? student.getName() : "Student")
//                .studentEmail(student.getEmail() != null ? student.getEmail() : "")
//                .periodType(periodType)
//                .startDate(startDate)
//                .endDate(endDate)
//                .periodLabel(generatePeriodLabel(startDate, endDate, periodType))
//                .totalTests(totalTests)
//                .totalMarksObtained(totalMarks)
//                .averageMarks(avgMarks)
//                .highestMarks(highestMarks)
//                .lowestMarks(lowestMarks)
//                .improvementPercentage(improvementPercentage)
//                .breakdown(breakdown)
//                .testHistory(testHistory)
//                .currentRank(currentRank)
//                .totalParticipants(calculateTotalParticipants(results))
//                .subjectPerformance(subjectPerformance)
//                .build();
//    }
//    
//    private PerformanceAnalyticsDto buildEmptyPerformanceAnalytics(
//            Long studentId, LocalDate startDate, LocalDate endDate, String periodType) {
//        
//        Users student = userRepository.findById(studentId).orElse(new Users());
//        
//        return PerformanceAnalyticsDto.builder()
//                .studentId(studentId)
//                .studentName(student.getName() != null ? student.getName() : "Student")
//                .studentEmail(student.getEmail() != null ? student.getEmail() : "")
//                .periodType(periodType)
//                .startDate(startDate)
//                .endDate(endDate)
//                .periodLabel(generatePeriodLabel(startDate, endDate, periodType))
//                .totalTests(0)
//                .totalMarksObtained(0.0)
//                .averageMarks(0.0)
//                .highestMarks(0.0)
//                .lowestMarks(0.0)
//                .improvementPercentage(0.0)
//                .breakdown(new ArrayList<>())
//                .testHistory(new ArrayList<>())
//                .currentRank(null)
//                .totalParticipants(0)
//                .subjectPerformance(new HashMap<>())
//                .build();
//    }
//    
//    private String generatePeriodLabel(LocalDate startDate, LocalDate endDate, String periodType) {
//        switch (periodType) {
//            case "MONTHLY":
//                return startDate.getMonth().toString() + " " + startDate.getYear();
//            case "WEEKLY":
//                return "Week of " + startDate.format(DateTimeFormatter.ofPattern("MMM dd"));
//            case "DAILY":
//                return startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
//            default:
//                return startDate + " to " + endDate;
//        }
//    }
//    
//    private Long findMostPopularTest(List<StudentTestResult> results) {
//        return results.stream()
//                .collect(Collectors.groupingBy(StudentTestResult::getTestId, Collectors.counting()))
//                .entrySet().stream()
//                .max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .orElse(results.get(0).getTestId());
//    }
//    
//    private List<PerformanceAnalyticsDto.RankEntry> getTopRankersForTest(Long testId, int topN) {
//        List<StudentTestResult> allResults = testResultRepository
//                .findByTestIdOrderByTotalObtainedMarksDesc(testId);
//        
//        return allResults.stream()
//                .limit(topN)
//                .map(result -> {
//                    Users student = userRepository.findById(result.getStudentId()).orElse(new Users());
//                    return PerformanceAnalyticsDto.RankEntry.builder()
//                            .studentId(result.getStudentId())
//                            .studentName(student.getName() != null ? student.getName() : "Student")
//                            .studentEmail(result.getStudentEmail())
//                            .marksObtained(result.getTotalObtainedMarks())
//                            .correctAnswers(result.getNoOfCorrectAns() != null ? 
//                                    result.getNoOfCorrectAns().intValue() : 0)
//                            .timeSpent(result.getTimeSpent())
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }
//    
//    private Map<String, PerformanceAnalyticsDto.SubjectPerformance> calculateSubjectPerformance(
//            List<StudentTestResult> results) {
//        
//        Map<String, PerformanceAnalyticsDto.SubjectPerformance> subjectPerformance = new HashMap<>();
//        
//        // This would require more complex logic to calculate subject-wise performance
//        // For now, return empty map or implement based on your needs
//        
//        return subjectPerformance;
//    }
//    
//    private Integer calculateAverageRank(List<StudentTestResult> results, Long studentId) {
//        if (results.isEmpty()) return null;
//        
//        int totalRank = 0;
//        int rankedTests = 0;
//        
//        for (StudentTestResult result : results) {
//            Integer rank = getRankForStudent(result.getTestId(), studentId);
//            if (rank != null) {
//                totalRank += rank;
//                rankedTests++;
//            }
//        }
//        
//        return rankedTests > 0 ? totalRank / rankedTests : null;
//    }
//    
//    private Integer calculateTotalParticipants(List<StudentTestResult> results) {
//        Set<Long> testIds = results.stream()
//                .map(StudentTestResult::getTestId)
//                .collect(Collectors.toSet());
//        
//        int total = 0;
//        for (Long testId : testIds) {
//            List<StudentTestResult> testResults = testResultRepository.findByTestId(testId);
//            if (testResults != null) {
//                total += testResults.size();
//            }
//        }
//        return total;
//    }
//
// // Updated method for specific test
//    @Override
//    public List<RankingResponseDto> getTopRanksForTest(Long testId, Integer topN) {
//        try {
//            log.info("Fetching top {} ranks for test ID: {}", topN, testId);
//            
//            // Get test details
//            Tests test = testRepository.findById(testId)
//                    .orElseThrow(() -> new CustomException("Test not found with ID: " + testId, HttpStatus.NOT_FOUND));
//            
//            // Get all results for this test (from repository or manual filtering)
//            List<StudentTestResult> allResults;
//            
//            // Try to use repository method if it exists
//            try {
//                // This will throw an error if method doesn't exist
//                allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(testId);
//            } catch (Exception e) {
//                // Fallback: Get all results and sort manually
//                log.warn("Repository method not found, using manual sorting");
//                allResults = testResultRepository.findAll().stream()
//                        .filter(result -> result.getTestId() != null && result.getTestId().equals(testId))
//                        .sorted((r1, r2) -> Double.compare(r2.getTotalObtainedMarks(), r1.getTotalObtainedMarks()))
//                        .collect(Collectors.toList());
//            }
//            
//            if (allResults.isEmpty()) {
//                log.warn("No results found for test ID: {}", testId);
//                return new ArrayList<>();
//            }
//            
//            // Prepare top rank list
//            List<RankingResponseDto> topRankList = new ArrayList<>();
//            int rank = 1;
//            
//            for (StudentTestResult result : allResults) {
//                if (topN != null && rank > topN) {
//                    break;
//                }
//                
//                RankingResponseDto rankDto = createRankingResponseDto(result, rank, test);
//                topRankList.add(rankDto);
//                rank++;
//            }
//            
//            log.info("Successfully fetched top {} ranks for test {}", topRankList.size(), testId);
//            return topRankList;
//            
//        } catch (Exception e) {
//            log.error("Error fetching top ranks for test: {}", e.getMessage(), e);
//            throw new CustomException("Error fetching top ranks: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}

package com.mockperiod.main.serviceImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockperiod.main.dto.*;
import com.mockperiod.main.entities.*;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.repository.*;
import com.mockperiod.main.service.StudentTestResultService;
import com.mockperiod.main.service.TestRankingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentTestResultServiceImpl implements StudentTestResultService {

	private final TestRepository testRepository;
	private final StudentTestResultRepository testResultRepository;
	private final UserRepository userRepository;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;
	private final SubjectRepository subjectRepository;
	private final TestRankingRepository testRankingRepository;
	private final TestInstituteTimeRepository testInstituteTimeRepository;
	private final ChapterRepository chapterRepository;
	private final ExamRepository examRepository;

	private final TestRankingService rankingService;
	private final ObjectMapper objectMapper;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

//	@Override
//	public StudentTestResultDto createTestResult(StudentTestResultDto testResultDto) {
//		try {
//			Tests tests = testRepository.findById(testResultDto.getTestId())
//					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));
//
//			Users users = userRepository.findById(testResultDto.getStudentId())
//					.orElseThrow(() -> new CustomException("User(Student) not found", HttpStatus.NOT_FOUND));
//
//			LocalDate date = LocalDate.parse(testResultDto.getTestAttemptedDate(), formatter);
//
//			StudentTestResult testResult = new StudentTestResult();
//
//			// Direct assignment - negative marks are allowed
//			testResult.setNoOfCorrectAns(testResultDto.getNoOfCorrectAns());
//			testResult.setStudentEmail(users.getEmail());
//			testResult.setNoOfInCorrectAns(testResultDto.getNoOfInCorrectAns());
//			testResult.setInstituteEmail(testResultDto.getInstituteEmail());
//			testResult.setStudentId(users.getId());
//			testResult.setTestAttemptedDate(date);
//
//			// IMPORTANT: Store negative marks as they are (no Math.max here)
//			testResult.setTotalObtainedMarks(testResultDto.getTotalObtainedMarks()); // Could be negative
//
//			testResult.setTestId(tests.getId());
//			testResult.setTestName(tests.getTestName());
//			testResult.setCurrectquestionIds(testResultDto.getCurrectquestionIds());
//			testResult.setIncurrectquestionIds(testResultDto.getIncurrectquestionIds());
//			testResult.setAttemptedTestLanguage(Language.valueOf(testResultDto.getAttemptedTestLanguage()));
//
//			// Store selected language subject ID
//			if (testResultDto.getSelectedLanguageSubjectId() != null) {
//				testResult.setSelectedLanguageSubjectId(testResultDto.getSelectedLanguageSubjectId());
//			}
//
//			// Calculate remark based on negative marks
//			String remark = calculateRemarkBasedOnNegativeMarks(testResultDto.getTotalObtainedMarks(), tests);
//			testResult.setRemark(remark);
//
//			// Set time spent if available in DTO
//			if (testResultDto.getTimeSpent() != null) {
//				testResult.setTimeSpent(testResultDto.getTimeSpent());
//			}
//
//			StudentTestResult savedResult = testResultRepository.save(testResult);
//
//			return convertToDto(savedResult);
//
//		} catch (CustomException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new CustomException("Error creating test result: " + e.getMessage(),
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	@Override
	public StudentTestResultDto createTestResult(StudentTestResultDto testResultDto) {
		try {
			Tests tests = testRepository.findById(testResultDto.getTestId())
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			Users users = userRepository.findById(testResultDto.getStudentId())
					.orElseThrow(() -> new CustomException("User(Student) not found", HttpStatus.NOT_FOUND));

			// ADD NULL CHECK FOR USER'S NAME AND EMAIL
			String studentEmail = users.getEmail();
			if (studentEmail == null) {
				log.warn("User email is null for user ID: {}", users.getId());
				throw new CustomException("Student email is required", HttpStatus.BAD_REQUEST);
			}

			LocalDate date = LocalDate.parse(testResultDto.getTestAttemptedDate(), formatter);

			StudentTestResult testResult = new StudentTestResult();

			// Direct assignment - negative marks are allowed
			testResult.setNoOfCorrectAns(testResultDto.getNoOfCorrectAns());
			testResult.setStudentEmail(studentEmail);
			testResult.setNoOfInCorrectAns(testResultDto.getNoOfInCorrectAns());
			testResult.setInstituteEmail(testResultDto.getInstituteEmail());
			testResult.setStudentId(users.getId());
			testResult.setTestAttemptedDate(date);

			// IMPORTANT: Store negative marks as they are (no Math.max here)
			testResult.setTotalObtainedMarks(testResultDto.getTotalObtainedMarks()); // Could be negative

			testResult.setTestId(tests.getId());
			testResult.setTestName(tests.getTestName());
			testResult.setCurrectquestionIds(testResultDto.getCurrectquestionIds());
			testResult.setIncurrectquestionIds(testResultDto.getIncurrectquestionIds());
//			testResult.setAttemptedTestLanguage(Language.valueOf(testResultDto.getTestAttemptedLanguage()));

			// Handle AttemptedTestLanguage - ADD NULL CHECK AND LOGGING
			String attemptedLanguage = testResultDto.getTestAttemptedLanguage();
			log.info("Setting attemptedTestLanguage from DTO: {}", attemptedLanguage);

			if (attemptedLanguage != null && !attemptedLanguage.trim().isEmpty()) {
				try {
					testResult.setAttemptedTestLanguage(Language.valueOf(attemptedLanguage.toUpperCase()));
					log.info("Successfully set attemptedTestLanguage to: {}", attemptedLanguage.toUpperCase());
				} catch (IllegalArgumentException e) {
					log.warn("Invalid language provided: {}. Defaulting to ENGLISH", attemptedLanguage);
					testResult.setAttemptedTestLanguage(Language.ENGLISH);
				}
			} else {
				// Default to ENGLISH if not provided or empty
				log.info("No language provided, defaulting to ENGLISH");
				testResult.setAttemptedTestLanguage(Language.ENGLISH);
			}

			// Store selected language subject ID
			if (testResultDto.getSelectedLanguageSubjectId() != null) {
				testResult.setSelectedLanguageSubjectId(testResultDto.getSelectedLanguageSubjectId());
			}

			// Calculate remark based on negative marks
			String remark = calculateRemarkBasedOnNegativeMarks(testResultDto.getTotalObtainedMarks(), tests);
			testResult.setRemark(remark);

			// Set time spent if available in DTO
			if (testResultDto.getTimeSpent() != null) {
				testResult.setTimeSpent(testResultDto.getTimeSpent());
			}

			log.info("Creating test result for student ID: {}, email: {}, test: {}", users.getId(), studentEmail,
					tests.getTestName());

			StudentTestResult savedResult = testResultRepository.save(testResult);

			return convertToDto(savedResult);

		} catch (CustomException e) {
			log.error("CustomException in createTestResult: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Error creating test result: {}", e.getMessage(), e);
			throw new CustomException("Error creating test result: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private StudentTestResultDto convertToDto(StudentTestResult testResult) {
		StudentTestResultDto dto = new StudentTestResultDto();
		BeanUtils.copyProperties(testResult, dto);
		dto.setTestAttemptedDate(testResult.getTestAttemptedDate().format(formatter));

		// Add selected language subject name if available
		if (testResult.getSelectedLanguageSubjectId() != null) {
			try {
				Subjects subject = subjectRepository.findById(testResult.getSelectedLanguageSubjectId()).orElse(null);
				if (subject != null) {
					dto.setSelectedLanguageSubjectName(subject.getName());
				}
			} catch (Exception e) {
				log.warn("Could not fetch selected language subject name: {}", e.getMessage());
			}
		}

		return dto;
	}

	// ========== SUBMIT TEST AND CALCULATE RESULT ==========

//	@Override
//	@Transactional
//	public StudentTestResultDto submitTestAndCalculateResult(TestSubmissionDto testSubmissionDto) {
//		try {
//			log.info("Submitting test for studentId: {}, testId: {}", testSubmissionDto.getStudentId(),
//					testSubmissionDto.getTestId());
//
//			boolean alreadyAttempted = testResultRepository.existsByStudentIdAndTestId(testSubmissionDto.getStudentId(),
//					testSubmissionDto.getTestId());
//
//			if (alreadyAttempted) {
//				throw new CustomException(
//						"Student has already attempted this test. Multiple submissions are not allowed.",
//						HttpStatus.BAD_REQUEST);
//			}
//
//			// Validate input
//			validateTestSubmission(testSubmissionDto);
//
//			// Get test details
//			Tests test = testRepository.findById(testSubmissionDto.getTestId())
//					.orElseThrow(() -> new CustomException("Test not found with id: " + testSubmissionDto.getTestId(),
//							HttpStatus.NOT_FOUND));
//
//			// Validate student exists
//			Users student = userRepository.findById(testSubmissionDto.getStudentId()).orElseThrow(
//					() -> new CustomException("Student not found with id: " + testSubmissionDto.getStudentId(),
//							HttpStatus.NOT_FOUND));
//
//			// Validate institute exists
//			userRepository.findByEmail(testSubmissionDto.getInstituteEmail())
//					.orElseThrow(() -> new CustomException(
//							"Institute not found with email: " + testSubmissionDto.getInstituteEmail(),
//							HttpStatus.NOT_FOUND));
//
//			// Calculate test result with negative marking
//			TestResultCalculationDto calculatedResult = calculateTestResult(testSubmissionDto);
//
//			// Get the calculated marks (can be negative)
//			double totalObtainedMarks = calculatedResult.getTotalObtainedMarks();
//
//			// Format time spent
//			String timeSpentFormatted = formatTimeSpent(testSubmissionDto.getTotalTimeSpent());
//
//			// Create DTO with the actual marks (negative if applicable)
//			StudentTestResultDto resultDto = StudentTestResultDto.builder().testId(testSubmissionDto.getTestId())
//					.testName(test.getTestName()).studentId(testSubmissionDto.getStudentId())
//					.studentEmail(student.getEmail()).instituteEmail(testSubmissionDto.getInstituteEmail())
//					.noOfCorrectAns((double) calculatedResult.getCorrectCount())
//					.noOfInCorrectAns((double) calculatedResult.getIncorrectCount())
//					.currectquestionIds(calculatedResult.getCorrectQuestionIds())
//					.incurrectquestionIds(calculatedResult.getIncorrectQuestionIds())
//					.totalObtainedMarks(totalObtainedMarks) // Negative if applicable
//					.testAttemptedDate(LocalDate.now().toString())
//					.selectedLanguageSubjectId(testSubmissionDto.getSelectedLanguageSubjectId())
//					.remark(calculateRemarkBasedOnNegativeMarks(totalObtainedMarks, test)).timeSpent(timeSpentFormatted)
//					.examType(test.getExamType())
//					.testAttemptedLanguage(testSubmissionDto.getTestAttemptedLanguage())
//					.build();
//
//			// Save the result
//			StudentTestResultDto savedResult = createTestResult(resultDto);
//
//			// Calculate and assign rank (ranking should handle negative marks)
//			Integer currentRank = rankingService.calculateAndSaveRank(testSubmissionDto.getTestId(),
//					testSubmissionDto.getStudentId(), totalObtainedMarks, // Pass the actual marks (could be negative)
//					savedResult.getNoOfCorrectAns().intValue(), timeSpentFormatted, test.getExamType());
//
//			// Update the saved result with additional info
//			savedResult.setCurrentRank(currentRank);
//
//			// Get total participants for this test
//			Integer totalParticipants = testRankingRepository
//					.findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(testSubmissionDto.getTestId())
//					.size();
//			savedResult.setTotalParticipants(totalParticipants);
//
//			// Ensure time spent is set
//			savedResult.setTimeSpent(timeSpentFormatted);
//
//			// Add selected language subject name
//			if (testSubmissionDto.getSelectedLanguageSubjectId() != null) {
//				Subjects selectedSubject = subjectRepository.findById(testSubmissionDto.getSelectedLanguageSubjectId())
//						.orElse(null);
//				if (selectedSubject != null) {
//					savedResult.setSelectedLanguageSubjectName(selectedSubject.getName());
//				}
//			}
//
//			log.info("Test submitted successfully. Student ID: {}, Marks: {} (negative allowed), Rank: {}",
//					testSubmissionDto.getStudentId(), totalObtainedMarks, currentRank);
//
//			return savedResult;
//
//		} catch (CustomException e) {
//			log.error("Custom exception in submitTestAndCalculateResult: {}", e.getMessage());
//			throw e;
//		} catch (Exception e) {
//			log.error("Unexpected error in submitTestAndCalculateResult: {}", e.getMessage(), e);
//			throw new CustomException("Error submitting test: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	@Override
	@Transactional
	public StudentTestResultDto submitTestAndCalculateResult(TestSubmissionDto testSubmissionDto) {
		try {
			log.info("Submitting test for studentId: {}, testId: {}", testSubmissionDto.getStudentId(),
					testSubmissionDto.getTestId());

			boolean alreadyAttempted = testResultRepository.existsByStudentIdAndTestId(testSubmissionDto.getStudentId(),
					testSubmissionDto.getTestId());

			if (alreadyAttempted) {
				throw new CustomException(
						"Student has already attempted this test. Multiple submissions are not allowed.",
						HttpStatus.BAD_REQUEST);
			}

			// Validate input
			validateTestSubmission(testSubmissionDto);

			// Get test details
			Tests test = testRepository.findById(testSubmissionDto.getTestId())
					.orElseThrow(() -> new CustomException("Test not found with id: " + testSubmissionDto.getTestId(),
							HttpStatus.NOT_FOUND));

			// Validate student exists
			Users student = userRepository.findById(testSubmissionDto.getStudentId()).orElseThrow(
					() -> new CustomException("Student not found with id: " + testSubmissionDto.getStudentId(),
							HttpStatus.NOT_FOUND));

			// ADD LOGGING TO CHECK STUDENT DETAILS
			log.info("Student found: ID={}, Name={}, Email={}", student.getId(), student.getName(), student.getEmail());

			if (student.getEmail() == null) {
				log.error("Student email is null for student ID: {}", student.getId());
				throw new CustomException("Student email is required", HttpStatus.BAD_REQUEST);
			}

			// Validate institute exists
			Users institute = userRepository.findByEmail(testSubmissionDto.getInstituteEmail())
					.orElseThrow(() -> new CustomException(
							"Institute not found with email: " + testSubmissionDto.getInstituteEmail(),
							HttpStatus.NOT_FOUND));

			log.info("Institute found: ID={}, Name={}, Email={}", institute.getId(), institute.getName(),
					institute.getEmail());

			// Calculate test result with negative marking
			TestResultCalculationDto calculatedResult = calculateTestResult(testSubmissionDto);

			// Get the calculated marks (can be negative)
			double totalObtainedMarks = calculatedResult.getTotalObtainedMarks();

			// Format time spent
			String timeSpentFormatted = formatTimeSpent(testSubmissionDto.getTotalTimeSpent());

			// Create DTO with the actual marks (negative if applicable)
			StudentTestResultDto resultDto = StudentTestResultDto.builder().testId(testSubmissionDto.getTestId())
					.testName(test.getTestName()).studentId(testSubmissionDto.getStudentId())
					.studentEmail(student.getEmail()).instituteEmail(testSubmissionDto.getInstituteEmail())
					.noOfCorrectAns((double) calculatedResult.getCorrectCount())
					.noOfInCorrectAns((double) calculatedResult.getIncorrectCount())
					.currectquestionIds(calculatedResult.getCorrectQuestionIds())
					.incurrectquestionIds(calculatedResult.getIncorrectQuestionIds())
					.totalObtainedMarks(totalObtainedMarks) // Negative if applicable
					.testAttemptedDate(LocalDate.now().toString())
//	                .testAttemptedLanguage(Language.valueOf(testSubmissionDto.getTestAttemptedLanguage()))
					.selectedLanguageSubjectId(testSubmissionDto.getSelectedLanguageSubjectId())
					.remark(calculateRemarkBasedOnNegativeMarks(totalObtainedMarks, test)).timeSpent(timeSpentFormatted)
					.examType(test.getExamType()).testAttemptedLanguage(testSubmissionDto.getTestAttemptedLanguage())
					.build();

			log.info("Creating test result DTO: studentEmail={}, instituteEmail={}, testName={}",
					resultDto.getStudentEmail(), resultDto.getInstituteEmail(), resultDto.getTestName());

			// Save the result
			StudentTestResultDto savedResult = createTestResult(resultDto);

			// Calculate and assign rank (ranking should handle negative marks)
			Integer currentRank = rankingService.calculateAndSaveRank(testSubmissionDto.getTestId(),
					testSubmissionDto.getStudentId(), totalObtainedMarks, // Pass the actual marks (could be negative)
					savedResult.getNoOfCorrectAns().intValue(), timeSpentFormatted, test.getExamType());

			// Update the saved result with additional info
			savedResult.setCurrentRank(currentRank);

			// Get total participants for this test
			Integer totalParticipants = testRankingRepository
					.findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(testSubmissionDto.getTestId())
					.size();
			savedResult.setTotalParticipants(totalParticipants);

			// Ensure time spent is set
			savedResult.setTimeSpent(timeSpentFormatted);

			// Add selected language subject name
			if (testSubmissionDto.getSelectedLanguageSubjectId() != null) {
				Subjects selectedSubject = subjectRepository.findById(testSubmissionDto.getSelectedLanguageSubjectId())
						.orElse(null);
				if (selectedSubject != null) {
					savedResult.setSelectedLanguageSubjectName(selectedSubject.getName());
				}
			}

			log.info("Test submitted successfully. Student ID: {}, Marks: {} (negative allowed), Rank: {}",
					testSubmissionDto.getStudentId(), totalObtainedMarks, currentRank);

			return savedResult;

		} catch (CustomException e) {
			log.error("Custom exception in submitTestAndCalculateResult: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error in submitTestAndCalculateResult: {}", e.getMessage(), e);
			throw new CustomException("Error submitting test: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public TestResultCalculationDto calculateTestResult(TestSubmissionDto testSubmissionDto) {
		try {
			log.info("Calculating test result for studentId: {}, testId: {}", testSubmissionDto.getStudentId(),
					testSubmissionDto.getTestId());

			// Get test details
			Tests test = testRepository.findById(testSubmissionDto.getTestId())
					.orElseThrow(() -> new CustomException("Test not found with id: " + testSubmissionDto.getTestId(),
							HttpStatus.NOT_FOUND));

			// Get filtered questions based on language subject selection
//			List<Questions> testQuestions = getFilteredQuestionsForTest(testSubmissionDto.getTestId(),
//					testSubmissionDto.getSelectedLanguageSubjectId());

			List<Questions> testQuestions = getFilteredQuestionsForTestWithLanguage(testSubmissionDto.getTestId(),
					testSubmissionDto.getSelectedLanguageSubjectId(),
					Language.valueOf(testSubmissionDto.getTestAttemptedLanguage()));

			// Get all options for the questions
			List<Long> questionIds = testQuestions.stream().map(Questions::getId).collect(Collectors.toList());
			List<Options> allOptions = optionRepository.findByQuestionIds(questionIds);
			Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
					.collect(Collectors.groupingBy(option -> option.getQuestion().getId()));

			// Calculate results
			List<Long> correctQuestionIds = new ArrayList<>();
			List<Long> incorrectQuestionIds = new ArrayList<>();
			List<Long> unansweredQuestionIds = new ArrayList<>();
			int correctCount = 0;
			int incorrectCount = 0;
			int unansweredCount = 0;

			// Create a map of student's attempts for quick lookup
			Map<Long, QuestionAttemptDto> studentAttemptsMap = testSubmissionDto.getQuestionAttempts().stream()
					.collect(Collectors.toMap(QuestionAttemptDto::getQuestionId, attempt -> attempt));

			// Evaluate each question
			for (Questions question : testQuestions) {
				QuestionAttemptDto studentAttempt = studentAttemptsMap.get(question.getId());
				List<Options> questionOptions = optionsByQuestionId.get(question.getId());

				if (studentAttempt == null || studentAttempt.getSelectedOptionId() == null) {
					// Unanswered question
					unansweredQuestionIds.add(question.getId());
					unansweredCount++;
				} else {
					// Find the selected option and check if it's correct
					Optional<Options> selectedOption = questionOptions.stream()
							.filter(option -> option.getId().equals(studentAttempt.getSelectedOptionId())).findFirst();

					if (selectedOption.isPresent() && Boolean.TRUE.equals(selectedOption.get().getIsCorrect())) {
						// Correct answer
						correctQuestionIds.add(question.getId());
						correctCount++;
					} else {
						// Incorrect answer
						incorrectQuestionIds.add(question.getId());
						incorrectCount++;
					}
				}
			}

			// Calculate marks - ALLOW NEGATIVE VALUES
			double correctMarks = correctCount * test.getCorrectMark();
			double negativeMarks = incorrectCount * test.getNegativeMark();
			double totalObtainedMarks = correctMarks - negativeMarks; // This can be negative

			// Calculate accuracy (only for attempted questions)
			int attemptedQuestions = correctCount + incorrectCount;
			double accuracy = attemptedQuestions > 0 ? (correctCount * 100.0) / attemptedQuestions : 0;

			// Determine performance level (handles negative marks)
			String performanceLevel = calculatePerformanceLevel(totalObtainedMarks, test, accuracy);

			log.info(
					"Test calculation completed - Correct: {}, Incorrect: {}, Unanswered: {}, Total Marks: {} (can be negative)",
					correctCount, incorrectCount, unansweredCount, totalObtainedMarks);

			return TestResultCalculationDto.builder().testId(testSubmissionDto.getTestId())
					.studentId(testSubmissionDto.getStudentId()).totalQuestions(testQuestions.size())
					.correctCount(correctCount).incorrectCount(incorrectCount).unansweredCount(unansweredCount)
					.correctQuestionIds(correctQuestionIds).incorrectQuestionIds(incorrectQuestionIds)
					.unansweredQuestionIds(unansweredQuestionIds).correctMarks(correctMarks)
					.negativeMarks(negativeMarks).totalObtainedMarks(totalObtainedMarks) // Can be negative
					.accuracy(accuracy).performanceLevel(performanceLevel).test(test)
					.selectedLanguageSubjectId(testSubmissionDto.getSelectedLanguageSubjectId()).build();

		} catch (Exception e) {
			log.error("Error calculating test result: {}", e.getMessage(), e);
			throw new CustomException("Error calculating test result: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== GET TEST RESULTS WITH LANGUAGE FILTERING ==========

	@Override
	public StudentTestResultDto getTestResultByStudentIdAndTestId(Long studentId, Long testId) {
		return getTestResultByStudentIdAndTestIdWithLanguage(studentId, testId, null);

	}

	public StudentTestResultDto getTestResultByStudentIdAndTestIdWithLanguage(Long studentId, Long testId,
			Long selectedLanguageSubjectId) {
		try {
			StudentTestResult testResults = testResultRepository.findByStudentIdAndTestId(studentId, testId);

			if (testResults == null) {
				throw new CustomException(
						"Test result not found for student ID: " + studentId + " and test ID: " + testId,
						HttpStatus.NOT_FOUND);
			}

			// Get the test details
			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			// Get filtered questions based on language selection
//			List<Questions> allTestQuestions = getFilteredQuestionsForTest(testId,
//					selectedLanguageSubjectId != null ? selectedLanguageSubjectId
//							: testResults.getSelectedLanguageSubjectId());

			List<Questions> allTestQuestions = getFilteredQuestionsForTestWithLanguage(testId,
					selectedLanguageSubjectId != null ? selectedLanguageSubjectId
							: testResults.getSelectedLanguageSubjectId(),
					testResults.getAttemptedTestLanguage());

			// Get correct and incorrect question IDs from the result
			List<Long> correctQuestionIds = testResults.getCurrectquestionIds() != null
					? testResults.getCurrectquestionIds()
					: new ArrayList<>();
			List<Long> incorrectQuestionIds = testResults.getIncurrectquestionIds() != null
					? testResults.getIncurrectquestionIds()
					: new ArrayList<>();

			// Identify unanswered questions
			List<Long> unansweredQuestionIds = allTestQuestions.stream().map(Questions::getId)
					.filter(questionId -> !correctQuestionIds.contains(questionId)
							&& !incorrectQuestionIds.contains(questionId))
					.collect(Collectors.toList());

			// Calculate total marks for the filtered questions
			double totalMarks = allTestQuestions.stream()
					.mapToDouble(question -> question.getMarks() != null ? question.getMarks() : test.getCorrectMark())
					.sum();

			// Get obtained marks (could be negative)
			double obtainedMarks = testResults.getTotalObtainedMarks() != null ? testResults.getTotalObtainedMarks()
					: 0.0;

			// Keep negative marks as they are
			double finalObtainedMarks = obtainedMarks;

			// Get rank information
			Integer currentRank = getRankForStudent(testId, studentId);

			// Get time spent
			String timeSpent = testResults.getTimeSpent() != null ? testResults.getTimeSpent() : "Not Available";

			// Fetch questions without IDs for clean response
			List<QuestionDto> correctQuestionDtos = fetchQuestionsWithoutIds(correctQuestionIds);
			List<QuestionDto> incorrectQuestionDtos = fetchQuestionsWithoutIds(incorrectQuestionIds);
			List<QuestionDto> unansweredQuestionDtos = fetchQuestionsWithoutIds(unansweredQuestionIds);

			// Build the response DTO
			StudentTestResultDto response = StudentTestResultDto.builder().id(testResults.getId())
					.testId(testResults.getTestId()).testName(testResults.getTestName())
					.studentId(testResults.getStudentId()).studentEmail(testResults.getStudentEmail())
					.instituteEmail(testResults.getInstituteEmail()).noOfCorrectAns(testResults.getNoOfCorrectAns())
					.noOfInCorrectAns(testResults.getNoOfInCorrectAns()).correctQuestions(correctQuestionDtos)
					.incorrectQuestions(incorrectQuestionDtos).unansweredQuestions(unansweredQuestionDtos)
					.totalObtainedMarks(finalObtainedMarks) // Could be negative
					.testAttemptedDate(
							testResults.getTestAttemptedDate() != null ? testResults.getTestAttemptedDate().toString()
									: null)
					.remark(testResults.getRemark()).currentRank(currentRank).timeSpent(timeSpent)
					.examType(test.getExamType()).selectedLanguageSubjectId(testResults.getSelectedLanguageSubjectId())
					.build();

			// Add selected language subject name
			if (testResults.getSelectedLanguageSubjectId() != null) {
				Subjects selectedSubject = subjectRepository.findById(testResults.getSelectedLanguageSubjectId())
						.orElse(null);
				if (selectedSubject != null) {
					response.setSelectedLanguageSubjectName(selectedSubject.getName());
				}
			}

			return response;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException("Error fetching test results by student: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== LANGUAGE SUBJECT FILTERING METHODS ==========

	private List<Questions> getFilteredQuestionsForTest(Long testId, Long selectedLanguageSubjectId) {
		try {
			// Get all questions for the test
			List<Questions> allTestQuestions = questionRepository.findByTestId(testId);

//			List<Questions> allTestQuestions = questionRepository.findByIdsAndTestLanguage(testId , );

			// If no language subject selected, return all questions
			if (selectedLanguageSubjectId == null) {
				return allTestQuestions;
			}

			// Get the test to check for Hindi/English subjects
			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			// Get Hindi and English subjects from the test
			Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);

			// Check if selected subject is Hindi or English
			boolean isLanguageSubjectSelected = hindiEnglishSubjects.stream()
					.anyMatch(subject -> subject.getId().equals(selectedLanguageSubjectId));

			if (!isLanguageSubjectSelected) {
				// Not a language subject, return all questions
				return allTestQuestions;
			}

			// Filter questions to exclude the other language subject
			Subjects otherLanguageSubject = hindiEnglishSubjects.stream()
					.filter(subject -> !subject.getId().equals(selectedLanguageSubjectId)).findFirst().orElse(null);

			if (otherLanguageSubject == null) {
				return allTestQuestions;
			}

			// Filter out questions from the other language subject
			return allTestQuestions.stream().filter(question -> {
				if (question.getSubject() == null) {
					return true; // Keep questions without subject
				}
				// Exclude questions from the other language subject
				return !question.getSubject().getId().equals(otherLanguageSubject.getId());
			}).collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Error filtering questions by language subject: {}", e.getMessage());
			return questionRepository.findByTestId(testId); // Fallback to all questions
		}
	}

	private List<Questions> getFilteredQuestionsForTestWithLanguage(Long testId, Long selectedLanguageSubjectId,
			Language la) {
		try {

			log.info("language {}", la);
			// Get all questions for the test
//			List<Questions> allTestQuestions = testRepository.findByT(testId);

			List<Questions> allTestQuestions = questionRepository.findByTestIdAndLanguage(testId, la);

			// If no language subject selected, return all questions
			if (selectedLanguageSubjectId == null) {
				return allTestQuestions;
			}
			boolean isLanguageSubjectSelected = false;

			// Get the test to check for Hindi/English subjects
			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			// Get Hindi and English subjects from the test
			Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);

			// Check if selected subject is Hindi or English

			if (hindiEnglishSubjects != null) {
				isLanguageSubjectSelected = hindiEnglishSubjects.stream()
						.anyMatch(subject -> subject.getId().equals(selectedLanguageSubjectId));
			}

			if (!isLanguageSubjectSelected) {
				// Not a language subject, return all questions
				return allTestQuestions;
			}

			// Filter questions to exclude the other language subject
			Subjects otherLanguageSubject = hindiEnglishSubjects.stream()
					.filter(subject -> !subject.getId().equals(selectedLanguageSubjectId)).findFirst().orElse(null);

			if (otherLanguageSubject == null) {
				return allTestQuestions;
			}

			// Filter out questions from the other language subject
			return allTestQuestions.stream().filter(question -> {
				if (question.getSubject() == null) {
					return true; // Keep questions without subject
				}
				// Exclude questions from the other language subject
				return !question.getSubject().getId().equals(otherLanguageSubject.getId());
			}).collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Error filtering questions by language subject: {}", e.getMessage());
			return questionRepository.findByTestId(testId); // Fallback to all questions
		}
	}

	private Set<Subjects> getHindiAndEnglishSubjects(Tests test) {
		if (test.getSubjects() == null || test.getSubjects().isEmpty()) {
			return new HashSet<>();
		}

		return test.getSubjects().stream().filter(this::isHindiOrEnglishSubject).collect(Collectors.toSet());
	}

	private boolean isHindiOrEnglishSubject(Subjects subject) {
		if (subject == null || subject.getName() == null) {
			return false;
		}

		String subjectName = subject.getName().toLowerCase();
		return subjectName.contains("hindi") || subjectName.contains("हिंदी") || subjectName.contains("english")
				|| subjectName.contains("अंग्रेजी");
	}

	// ========== UPDATED SUBJECT PERFORMANCE METHODS ==========

	@Override
	public StudentPerformanceDto getSubjectPerformanceInExam(Long studentId, Long testId) {
		try {
			log.info("Fetching subject performance for studentId: {}, testId: {}", studentId, testId);

			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			StudentTestResult testResult = testResultRepository.findByStudentIdAndTestId(studentId, testId);
			if (testResult == null) {
				throw new CustomException("Test result not found for the student", HttpStatus.NOT_FOUND);
			}

			// Get filtered questions based on language selection
			List<Questions> testQuestions = getFilteredQuestionsForTest(testId,
					testResult.getSelectedLanguageSubjectId());

			// Group filtered questions by subject
			Map<Long, List<Questions>> questionsBySubject = testQuestions.stream().filter(q -> q.getSubject() != null)
					.collect(Collectors.groupingBy(q -> q.getSubject().getId()));

			// Calculate subject-wise performance (with negative marking)
			Map<String, StudentPerformanceDto.SubjectPerformance> subjectPerformance = new HashMap<>();

			for (Map.Entry<Long, List<Questions>> entry : questionsBySubject.entrySet()) {
				Long subjectId = entry.getKey();
				List<Questions> subjectQuestions = entry.getValue();

				long totalQuestions = subjectQuestions.size();
				long correctAnswers = testResult.getCurrectquestionIds() != null
						? subjectQuestions.stream().filter(q -> testResult.getCurrectquestionIds().contains(q.getId()))
								.count()
						: 0;
				long incorrectAnswers = testResult.getIncurrectquestionIds() != null ? subjectQuestions.stream()
						.filter(q -> testResult.getIncurrectquestionIds().contains(q.getId())).count() : 0;

				double accuracy = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0;
				double totalMarks = correctAnswers * test.getCorrectMark() - incorrectAnswers * test.getNegativeMark();

				Subjects subject = subjectRepository.findById(subjectId)
						.orElseThrow(() -> new CustomException("Subject not found", HttpStatus.NOT_FOUND));

				subjectPerformance.put(subject.getName(),
						StudentPerformanceDto.SubjectPerformance.builder().averageMarks(totalMarks)
								.totalQuestions((int) totalQuestions).correctAnswers((int) correctAnswers)
								.incorrectAnswers((int) incorrectAnswers).accuracy(accuracy)
								.strengthLevel(calculateStrengthLevel(accuracy)).build());
			}

			Users student = userRepository.findById(studentId)
					.orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));

			// Calculate total marks based on filtered questions
			double totalTestMarks = calculateTotalMarks(test, testResult.getSelectedLanguageSubjectId());

			return StudentPerformanceDto.builder().studentId(studentId).studentName(student.getName())
					.studentEmail(student.getEmail()).testId(testId).testName(test.getTestName())
					.analysisType("SUBJECT_ANALYSIS").performanceType("EXAM_WISE")
					.subjectPerformance(subjectPerformance).marksObtained(testResult.getTotalObtainedMarks())
					.totalMarks(totalTestMarks)
					.percentage(totalTestMarks > 0 ? (testResult.getTotalObtainedMarks() / totalTestMarks) * 100 : 0)
//                    .selectedLanguageSubjectId(testResult.getSelectedLanguageSubjectId())
					.build();

		} catch (Exception e) {
			log.error("Error fetching subject performance in exam: {}", e.getMessage(), e);
			throw new CustomException("Error fetching subject performance in exam: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== UPDATED TOTAL MARKS CALCULATION ==========

	private Double calculateTotalMarks(Tests test, Long selectedLanguageSubjectId) {
		if (test == null)
			return 0.0;

		// Get filtered questions
		List<Questions> testQuestions = getFilteredQuestionsForTest(test.getId(), selectedLanguageSubjectId);
		return testQuestions.stream().mapToDouble(q -> q.getMarks() != null ? q.getMarks() : test.getCorrectMark())
				.sum();
	}

	// Overloaded method for backward compatibility
	private Double calculateTotalMarks(Tests test) {
		return calculateTotalMarks(test, null);
	}

	// ========== OTHER METHODS (Updated for language filtering) ==========

	@Override
	public StudentPerformanceDto getTestLeaderboard(Long testId, Integer topN) {
		try {
			log.info("Fetching leaderboard for testId: {}, top: {}", testId, topN);

			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			List<StudentTestResult> testResults = testResultRepository
					.findByTestIdOrderByTotalObtainedMarksDesc(testId);

			if (testResults.isEmpty()) {
				throw new CustomException("No results found for this test", HttpStatus.NOT_FOUND);
			}

			List<StudentPerformanceDto.RankEntry> leaderboard = new ArrayList<>();
			int rank = 1;

			for (StudentTestResult result : testResults) {
				if (topN != null && rank > topN)
					break;

				Users student = userRepository.findById(result.getStudentId()).orElse(new Users());

				// Calculate total marks based on student's language selection
				double totalMarks = calculateTotalMarks(test, result.getSelectedLanguageSubjectId());
				double percentage = totalMarks > 0 ? (result.getTotalObtainedMarks() / totalMarks) * 100 : 0;

				leaderboard.add(StudentPerformanceDto.RankEntry.builder().rank(rank).studentId(result.getStudentId())
						.studentName(student.getName()).studentEmail(student.getEmail())
						.marksObtained(result.getTotalObtainedMarks()) // Could be negative
//						.percentage(percentage)
//                        .selectedLanguageSubjectId(result.getSelectedLanguageSubjectId())
						.build());

				rank++;
			}

			return StudentPerformanceDto.builder().testId(testId).testName(test.getTestName())
					.analysisType("LEADERBOARD").performanceType(test.getExamType().name()).leaderboard(leaderboard)
					.totalParticipants(testResults.size()).testDate(testResults.get(0).getTestAttemptedDate()).build();

		} catch (Exception e) {
			log.error("Error fetching test leaderboard: {}", e.getMessage(), e);
			throw new CustomException("Error fetching test leaderboard: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== HELPER METHODS ==========

	private void validateTestSubmission(TestSubmissionDto testSubmissionDto) {
		if (testSubmissionDto.getTestId() == null) {
			throw new CustomException("Test ID is required", HttpStatus.BAD_REQUEST);
		}
		if (testSubmissionDto.getStudentId() == null) {
			throw new CustomException("Student ID is required", HttpStatus.BAD_REQUEST);
		}
		if (testSubmissionDto.getInstituteEmail() == null || testSubmissionDto.getInstituteEmail().trim().isEmpty()) {
			throw new CustomException("Institute email is required", HttpStatus.BAD_REQUEST);
		}
		if (testSubmissionDto.getQuestionAttempts() == null) {
			throw new CustomException("Question attempts are required", HttpStatus.BAD_REQUEST);
		}

		// Validate that student exists
		userRepository.findById(testSubmissionDto.getStudentId())
				.orElseThrow(() -> new CustomException("Student not found with id: " + testSubmissionDto.getStudentId(),
						HttpStatus.NOT_FOUND));

		// Validate that institute exists
		userRepository.findByEmail(testSubmissionDto.getInstituteEmail())
				.orElseThrow(() -> new CustomException(
						"Institute not found with email: " + testSubmissionDto.getInstituteEmail(),
						HttpStatus.NOT_FOUND));

		// Validate language subject if provided
		if (testSubmissionDto.getSelectedLanguageSubjectId() != null) {
			Subjects subject = subjectRepository.findById(testSubmissionDto.getSelectedLanguageSubjectId())
					.orElseThrow(() -> new CustomException("Selected language subject not found with id: "
							+ testSubmissionDto.getSelectedLanguageSubjectId(), HttpStatus.NOT_FOUND));

			// Check if it's a valid Hindi/English subject for this test
			Tests test = testRepository.findById(testSubmissionDto.getTestId())
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);
			boolean isValidLanguageSubject = hindiEnglishSubjects.stream()
					.anyMatch(s -> s.getId().equals(testSubmissionDto.getSelectedLanguageSubjectId()));

			if (!isValidLanguageSubject && !hindiEnglishSubjects.isEmpty()) {
				throw new CustomException(
						"Selected subject is not a valid Hindi/English language subject for this test",
						HttpStatus.BAD_REQUEST);
			}
		}
	}

	private String calculatePerformanceLevel(double totalObtainedMarks, Tests test, double accuracy) {
		if (totalObtainedMarks < 0) {
			return "Negative Score";
		}

		if (test == null || test.getTotalMarks() == null || test.getTotalMarks() == 0) {
			return "Average";
		}

		double percentage = (totalObtainedMarks / test.getTotalMarks()) * 100;

		if (percentage >= 90)
			return "Excellent";
		else if (percentage >= 75)
			return "Very Good";
		else if (percentage >= 60)
			return "Good";
		else if (percentage >= 40)
			return "Average";
		else
			return "Needs Improvement";
	}

	private String formatTimeSpent(Integer totalSeconds) {
		if (totalSeconds == null)
			return "0m 0s";

		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		} else {
			return String.format("%dm %ds", minutes, seconds);
		}
	}

	// Get rank for student
	private Integer getRankForStudent(Long testId, Long studentId) {
		try {
			List<StudentTestResult> allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(testId);

			for (int i = 0; i < allResults.size(); i++) {
				if (allResults.get(i).getStudentId().equals(studentId)) {
					return i + 1;
				}
			}
			return null;
		} catch (Exception e) {
			log.warn("Error fetching rank for student: {}", e.getMessage());
			return null;
		}
	}

	private List<QuestionDto> fetchQuestionsWithoutIds(List<Long> questionIds) {
		return questionIds.stream().map(questionId -> {
			Questions question = questionRepository.findById(questionId).orElseThrow(
					() -> new CustomException("Question not found with ID: " + questionId, HttpStatus.NOT_FOUND));

			// Create QuestionDto WITHOUT setting the ID
			QuestionDto questionDto = QuestionDto.builder().questionText(question.getQuestionText())
					.questionImageUrl(question.getQuestionImageUrl())
					.subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
					.subjectName(question.getSubject() != null ? question.getSubject().getName() : null)
					.chapterId(question.getChapter() != null ? question.getChapter().getId() : null)
					.questionNumber(question.getQuestionNumber()).marks(question.getMarks())
					.language(question.getLanguage() != null ? question.getLanguage().name() : null).build();

			// Fetch options WITHOUT IDs
			List<Options> options = optionRepository.findByQuestion(question);
			List<OptionDto> optionDtos = options.stream()
					.map(option -> OptionDto.builder().optionText(option.getOptionText())
							.optionImageUrl(option.getOptionImageUrl()).optionNumber(option.getOptionNumber())
							.isCorrect(option.getIsCorrect()).build())
					.collect(Collectors.toList());

			questionDto.setOptions(optionDtos);
			return questionDto;
		}).collect(Collectors.toList());
	}

	private String calculateRemarkBasedOnNegativeMarks(double obtainedMarks, Tests test) {
		if (test == null || test.getTotalMarks() == null || test.getTotalMarks() == 0) {
			return obtainedMarks < 0 ? "Very Poor (Negative Marks)" : "Average";
		}

		double percentage = (obtainedMarks / test.getTotalMarks()) * 100;

		if (obtainedMarks < 0) {
			return "Negative Score";
		} else if (percentage >= 90) {
			return "Excellent";
		} else if (percentage >= 75) {
			return "Very Good";
		} else if (percentage >= 60) {
			return "Good";
		} else if (percentage >= 40) {
			return "Average";
		} else if (percentage >= 0) {
			return "Needs Improvement";
		} else {
			return "Negative Score";
		}
	}

	private String calculateStrengthLevel(double accuracy) {
		if (accuracy >= 80)
			return "Strong";
		else if (accuracy >= 60)
			return "Average";
		else
			return "Weak";
	}

	// ========== EXISTING METHODS (Keep as is, but update DTOs if needed)
	// ==========

	@Override
	public List<StudentTestResultDto> getAllTestResult() {
		try {
			List<StudentTestResult> testResults = testResultRepository.findAll();
			return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException("Error fetching test results: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<StudentTestResultDto> getAllTestResultByInstitute(String instituteEmail) {
		try {
			List<StudentTestResult> testResults = testResultRepository.findByInstituteEmail(instituteEmail);
			return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException("Error fetching test results by institute: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deleteTestResult(Long id) {
		try {
			StudentTestResult testResult = testResultRepository.findById(id).orElseThrow(
					() -> new CustomException("Test result not found with id: " + id, HttpStatus.NOT_FOUND));
			testResultRepository.delete(testResult);
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException("Error deleting test result: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<StudentTestResultDto> getAllTestResultByDateAndInstitute(LocalDate date, String instituteEmail) {
		try {
			List<StudentTestResult> testResults = testResultRepository.findByTestAttemptedDateAndInstituteEmail(date,
					instituteEmail);
			return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException("Error fetching test results by date and institute: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<StudentTestResultDto> getTestResultByStudentAndInstitute(Long studentId, String instituteEmail) {
		try {
			List<StudentTestResult> testResults = testResultRepository.findByStudentIdAndInstituteEmail(studentId,
					instituteEmail);
			return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException("Error fetching test results by student and institute: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public StudentTestResultDto getTestResultById(Long id) {
		try {
			StudentTestResult testResult = testResultRepository.findById(id).orElseThrow(
					() -> new CustomException("Test result not found with id: " + id, HttpStatus.NOT_FOUND));

			return convertToDto(testResult);
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException("Error fetching test result: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<StudentTestResultDto> getTestResultByStudentId(Long studentId) {
		try {
			List<StudentTestResult> testResults = testResultRepository.findByStudentId(studentId);
			return testResults.stream().map(this::convertToDto).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CustomException("Error fetching test results by student: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== RANKING METHODS (Keep as is) ==========

	@Override
	public StudentPerformanceDto getRanking(String rankingType, Long referenceId, String instituteEmail, Integer topN) {
		// [Keep existing implementation]
		return null; // Your existing implementation
	}

	@Override
	public StudentPerformanceDto getStudentPerformance(Long studentId, String performanceType, Long referenceId,
			String instituteEmail) {
		// [Keep existing implementation]
		return null; // Your existing implementation
	}

	@Override
	public StudentPerformanceDto getStudentLastExamRank(Long studentId, String instituteEmail) {
		try {
			log.info("Fetching last exam rank for studentId: {}, institute: {}", studentId, instituteEmail);

			StudentTestResult lastResult = testResultRepository
					.findTopByStudentIdAndInstituteEmailOrderByTestAttemptedDateDesc(studentId, instituteEmail);

			if (lastResult == null) {
				throw new CustomException("No test results found for the student", HttpStatus.NOT_FOUND);
			}

			// Get all results for the same test to calculate rank (negative marks
			// considered)
			List<StudentTestResult> testResults = testResultRepository
					.findByTestIdAndInstituteEmail(lastResult.getTestId(), instituteEmail);
			int rank = calculateRank(testResults, studentId);

			Users student = userRepository.findById(studentId)
					.orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));

			Tests test = testRepository.findById(lastResult.getTestId())
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			return StudentPerformanceDto.builder().studentId(studentId).studentName(student.getName())
					.studentEmail(student.getEmail()).testId(lastResult.getTestId()).testName(lastResult.getTestName())
					.analysisType("RANK").performanceType(test.getExamType().name()).rank(rank)
					.marksObtained(lastResult.getTotalObtainedMarks()).totalMarks(calculateTotalMarks(test))
					.percentage(calculateTotalMarks(test) > 0
							? (lastResult.getTotalObtainedMarks() / calculateTotalMarks(test)) * 100
							: 0)
					.totalParticipants(testResults.size()).testDate(lastResult.getTestAttemptedDate())
					.instituteEmail(instituteEmail).build();

		} catch (Exception e) {
			log.error("Error fetching last exam rank: {}", e.getMessage(), e);
			throw new CustomException("Error fetching last exam rank: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private int calculateRank(List<StudentTestResult> results, Long studentId) {
		List<StudentTestResult> sortedResults = results.stream()
				.sorted((r1, r2) -> Double.compare(r2.getTotalObtainedMarks(), r1.getTotalObtainedMarks()))
				.collect(Collectors.toList());

		for (int i = 0; i < sortedResults.size(); i++) {
			if (sortedResults.get(i).getStudentId().equals(studentId)) {
				return i + 1;
			}
		}
		return -1;
	}

	@Override
	public StudentPerformanceDto getSubjectAveragesForExam(Long testId) {
		// [Keep existing implementation]
		return null; // Your existing implementation
	}

	@Override
	public List<RankingResponseDto> getTopRankListForLatestTest(String instituteEmail, Integer topN) {
		// [Keep existing implementation]
		return null; // Your existing implementation
	}

	@Override
	public PerformanceAnalyticsDto getMonthlyPerformance(Long studentId, LocalDate monthDate) {
		try {
			log.info("Getting monthly performance for student: {}, month: {}", studentId, monthDate);

			// Calculate month boundaries
			LocalDate startDate = (monthDate != null) ? monthDate.withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);
			LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

			// Get results for the month
			List<StudentTestResult> monthlyResults = getTestResultsByDateRange(studentId, startDate, endDate);

			// Calculate weekly breakdown
			List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = calculateWeeklyBreakdown(
					monthlyResults, startDate);

			// Build comprehensive response
			return buildPerformanceAnalytics(studentId, monthlyResults, startDate, endDate, "MONTHLY", weeklyBreakdown);

		} catch (Exception e) {
			log.error("Error fetching monthly performance: {}", e.getMessage(), e);
			throw new CustomException("Error fetching monthly performance: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 2. Get Weekly Performance
	@Override
	public PerformanceAnalyticsDto getWeeklyPerformance(Long studentId, LocalDate weekStart) {
		try {
			log.info("Getting weekly performance for student: {}, weekStart: {}", studentId, weekStart);

			// Calculate week boundaries
			LocalDate startDate = (weekStart != null) ? weekStart : LocalDate.now().with(DayOfWeek.MONDAY);
			LocalDate endDate = startDate.plusDays(6);

			// Get results for the week
			List<StudentTestResult> weeklyResults = getTestResultsByDateRange(studentId, startDate, endDate);

			// Calculate daily breakdown
			List<PerformanceAnalyticsDto.PerformanceBreakdown> dailyBreakdown = calculateDailyBreakdown(weeklyResults);

			// Build comprehensive response
			return buildPerformanceAnalytics(studentId, weeklyResults, startDate, endDate, "WEEKLY", dailyBreakdown);

		} catch (Exception e) {
			log.error("Error fetching weekly performance: {}", e.getMessage(), e);
			throw new CustomException("Error fetching weekly performance: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 3. Get Last Month Performance (for current month 1st date)
	@Override
	public PerformanceAnalyticsDto getLastMonthPerformance(Long studentId) {
		try {
			log.info("Getting last month performance for student: {}", studentId);

			// Calculate last month boundaries
			LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
			LocalDate lastDayOfLastMonth = firstDayOfCurrentMonth.minusDays(1);
			LocalDate firstDayOfLastMonth = lastDayOfLastMonth.withDayOfMonth(1);

			// Get results for last month
			List<StudentTestResult> lastMonthResults = getTestResultsByDateRange(studentId, firstDayOfLastMonth,
					lastDayOfLastMonth);

			// Calculate weekly breakdown for last month
			List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = calculateWeeklyBreakdown(
					lastMonthResults, firstDayOfLastMonth);

			// Build comprehensive response
			PerformanceAnalyticsDto response = buildPerformanceAnalytics(studentId, lastMonthResults,
					firstDayOfLastMonth, lastDayOfLastMonth, "MONTHLY", weeklyBreakdown);

			// Set period label
			response.setPeriodLabel(firstDayOfLastMonth.getMonth().toString() + " " + firstDayOfLastMonth.getYear());

			return response;

		} catch (Exception e) {
			log.error("Error fetching last month performance: {}", e.getMessage(), e);
			throw new CustomException("Error fetching last month performance: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 4. Get Last Month Top Ranks (Institute-wise)
	@Override
	public PerformanceAnalyticsDto getLastMonthTopRanks(String instituteEmail, Integer topN) {
		try {
			log.info("Getting last month top ranks for institute: {}, top: {}", instituteEmail, topN);

			// Calculate last month boundaries
			LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
			LocalDate lastDayOfLastMonth = firstDayOfCurrentMonth.minusDays(1);
			LocalDate firstDayOfLastMonth = lastDayOfLastMonth.withDayOfMonth(1);

			// Get all results for institute in last month
			List<StudentTestResult> lastMonthResults = testResultRepository
					.findByInstituteEmailAndTestAttemptedDateBetween(instituteEmail, firstDayOfLastMonth,
							lastDayOfLastMonth);

			if (lastMonthResults.isEmpty()) {
				throw new CustomException("No results found for last month", HttpStatus.NOT_FOUND);
			}

			// Find the most popular test in last month
			Long mostPopularTestId = findMostPopularTest(lastMonthResults);

			// Get top ranks for that test
			List<PerformanceAnalyticsDto.RankEntry> topRankers = getTopRankersForTest(mostPopularTestId,
					topN != null ? topN : 10);

			// Get test details
			Tests test = testRepository.findById(mostPopularTestId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			// Build response focused on ranking
			return PerformanceAnalyticsDto.builder().instituteEmail(instituteEmail).periodType("MONTHLY")
					.startDate(firstDayOfLastMonth).endDate(lastDayOfLastMonth)
					.periodLabel("Top Ranks - " + firstDayOfLastMonth.getMonth().toString())
					.totalTests(lastMonthResults.size())
//                  .totalParticipants(getTotalParticipants(mostPopularTestId))
					.topRankers(topRankers)
					.testHistory(List.of(PerformanceAnalyticsDto.TestResultSummary.builder().testId(test.getId())
							.testName(test.getTestName()).examType(test.getExamType().name())
							.totalMarks(calculateTotalMarks(test)).build()))
					.build();

		} catch (Exception e) {
			log.error("Error fetching last month top ranks: {}", e.getMessage(), e);
			throw new CustomException("Error fetching last month top ranks: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Helper Methods

	private List<StudentTestResult> getTestResultsByDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
		try {
			// Try repository method if available
			return testResultRepository.findByStudentIdAndTestAttemptedDateBetween(studentId, startDate, endDate);
		} catch (Exception e) {
			// Fallback: manual filtering
			log.warn("Using manual filtering for date range query");
			return testResultRepository.findByStudentId(studentId).stream()
					.filter(result -> result.getTestAttemptedDate() != null
							&& !result.getTestAttemptedDate().isBefore(startDate)
							&& !result.getTestAttemptedDate().isAfter(endDate))
					.collect(Collectors.toList());
		}
	}

	private List<PerformanceAnalyticsDto.PerformanceBreakdown> calculateWeeklyBreakdown(
			List<StudentTestResult> monthlyResults, LocalDate monthStart) {

		// Group results by week
		Map<Integer, List<StudentTestResult>> resultsByWeek = monthlyResults.stream()
				.collect(Collectors.groupingBy(result -> (result.getTestAttemptedDate().getDayOfMonth() - 1) / 7 + 1));

		List<PerformanceAnalyticsDto.PerformanceBreakdown> weeklyBreakdown = new ArrayList<>();

		// Calculate metrics for each week
		resultsByWeek.forEach((weekNumber, weekResults) -> {
			double weekTotalMarks = weekResults.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).sum();
			double weekAvgMarks = weekResults.size() > 0 ? weekTotalMarks / weekResults.size() : 0;

			// Calculate accuracy if needed
			int correctAnswers = weekResults.stream()
					.mapToInt(result -> result.getNoOfCorrectAns() != null ? result.getNoOfCorrectAns().intValue() : 0)
					.sum();
			int totalAttempted = correctAnswers + weekResults.stream().mapToInt(
					result -> result.getNoOfInCorrectAns() != null ? result.getNoOfInCorrectAns().intValue() : 0).sum();
			double accuracy = totalAttempted > 0 ? (correctAnswers * 100.0) / totalAttempted : 0;

			weeklyBreakdown.add(PerformanceAnalyticsDto.PerformanceBreakdown.builder().periodLabel("Week " + weekNumber)
					.testCount(weekResults.size()).averageMarks(weekAvgMarks).totalMarks(weekTotalMarks)
					.correctAnswers(correctAnswers).accuracy(accuracy).build());
		});

		// Sort by week number
		weeklyBreakdown.sort(Comparator.comparing(b -> Integer.parseInt(b.getPeriodLabel().replace("Week ", ""))));

		return weeklyBreakdown;
	}

	private List<PerformanceAnalyticsDto.PerformanceBreakdown> calculateDailyBreakdown(
			List<StudentTestResult> weeklyResults) {

		// Group results by day of week
		Map<DayOfWeek, List<StudentTestResult>> resultsByDay = weeklyResults.stream()
				.collect(Collectors.groupingBy(result -> result.getTestAttemptedDate().getDayOfWeek()));

		List<PerformanceAnalyticsDto.PerformanceBreakdown> dailyBreakdown = new ArrayList<>();

		// Initialize for all days
		for (DayOfWeek day : DayOfWeek.values()) {
			List<StudentTestResult> dayResults = resultsByDay.getOrDefault(day, new ArrayList<>());

			double dayTotalMarks = dayResults.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).sum();
			double dayAvgMarks = dayResults.size() > 0 ? dayTotalMarks / dayResults.size() : 0;

			dailyBreakdown.add(PerformanceAnalyticsDto.PerformanceBreakdown.builder().periodLabel(day.toString())
					.testCount(dayResults.size()).averageMarks(dayAvgMarks).totalMarks(dayTotalMarks).build());
		}

		return dailyBreakdown;
	}

	private PerformanceAnalyticsDto buildPerformanceAnalytics(Long studentId, List<StudentTestResult> results,
			LocalDate startDate, LocalDate endDate, String periodType,
			List<PerformanceAnalyticsDto.PerformanceBreakdown> breakdown) {

		if (results.isEmpty()) {
			return buildEmptyPerformanceAnalytics(studentId, startDate, endDate, periodType);
		}

		// Get student details
		Users student = userRepository.findById(studentId).orElse(new Users());

		// Calculate basic metrics
		int totalTests = results.size();
		double totalMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).sum();
		double avgMarks = totalTests > 0 ? totalMarks / totalTests : 0;
		double highestMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).max().orElse(0);
		double lowestMarks = results.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).min().orElse(0);

		// Calculate improvement percentage if multiple tests
		double improvementPercentage = 0.0;
		if (results.size() > 1) {
			StudentTestResult firstTest = results.get(0);
			StudentTestResult lastTest = results.get(results.size() - 1);
			if (firstTest.getTotalObtainedMarks() != 0) {
				improvementPercentage = ((lastTest.getTotalObtainedMarks() - firstTest.getTotalObtainedMarks())
						/ firstTest.getTotalObtainedMarks()) * 100;
			}
		}

		// Build test history
		List<PerformanceAnalyticsDto.TestResultSummary> testHistory = results.stream().map(result -> {
			Tests test = testRepository.findById(result.getTestId()).orElse(new Tests());
			return PerformanceAnalyticsDto.TestResultSummary.builder().testId(result.getTestId())
					.testName(result.getTestName()).marksObtained(result.getTotalObtainedMarks())
					.totalMarks(calculateTotalMarks(test)).testDate(result.getTestAttemptedDate())
					.rank(getRankForStudent(result.getTestId(), studentId))
					.examType(test.getExamType() != null ? test.getExamType().name() : "")
					.timeSpent(result.getTimeSpent()).build();
		}).collect(Collectors.toList());

		// Calculate current rank (average rank across all tests)
		Integer currentRank = calculateAverageRank(results, studentId);

		// Build subject performance if needed
		Map<String, PerformanceAnalyticsDto.SubjectPerformance> subjectPerformance = calculateSubjectPerformance(
				results);

		return PerformanceAnalyticsDto.builder().studentId(studentId)
				.studentName(student.getName() != null ? student.getName() : "Student")
				.studentEmail(student.getEmail() != null ? student.getEmail() : "").periodType(periodType)
				.startDate(startDate).endDate(endDate).periodLabel(generatePeriodLabel(startDate, endDate, periodType))
				.totalTests(totalTests).totalMarksObtained(totalMarks).averageMarks(avgMarks).highestMarks(highestMarks)
				.lowestMarks(lowestMarks).improvementPercentage(improvementPercentage).breakdown(breakdown)
				.testHistory(testHistory).currentRank(currentRank)
				.totalParticipants(calculateTotalParticipants(results)).subjectPerformance(subjectPerformance).build();
	}

	private PerformanceAnalyticsDto buildEmptyPerformanceAnalytics(Long studentId, LocalDate startDate,
			LocalDate endDate, String periodType) {

		Users student = userRepository.findById(studentId).orElse(new Users());

		return PerformanceAnalyticsDto.builder().studentId(studentId)
				.studentName(student.getName() != null ? student.getName() : "Student")
				.studentEmail(student.getEmail() != null ? student.getEmail() : "").periodType(periodType)
				.startDate(startDate).endDate(endDate).periodLabel(generatePeriodLabel(startDate, endDate, periodType))
				.totalTests(0).totalMarksObtained(0.0).averageMarks(0.0).highestMarks(0.0).lowestMarks(0.0)
				.improvementPercentage(0.0).breakdown(new ArrayList<>()).testHistory(new ArrayList<>())
				.currentRank(null).totalParticipants(0).subjectPerformance(new HashMap<>()).build();
	}

	private String generatePeriodLabel(LocalDate startDate, LocalDate endDate, String periodType) {
		switch (periodType) {
		case "MONTHLY":
			return startDate.getMonth().toString() + " " + startDate.getYear();
		case "WEEKLY":
			return "Week of " + startDate.format(DateTimeFormatter.ofPattern("MMM dd"));
		case "DAILY":
			return startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
		default:
			return startDate + " to " + endDate;
		}
	}

	private Long findMostPopularTest(List<StudentTestResult> results) {
		return results.stream().collect(Collectors.groupingBy(StudentTestResult::getTestId, Collectors.counting()))
				.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
				.orElse(results.get(0).getTestId());
	}

	private List<PerformanceAnalyticsDto.RankEntry> getTopRankersForTest(Long testId, int topN) {
		List<StudentTestResult> allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(testId);

		return allResults.stream().limit(topN).map(result -> {
			Users student = userRepository.findById(result.getStudentId()).orElse(new Users());
			return PerformanceAnalyticsDto.RankEntry.builder().studentId(result.getStudentId())
					.studentName(student.getName() != null ? student.getName() : "Student")
					.studentEmail(result.getStudentEmail()).marksObtained(result.getTotalObtainedMarks())
					.correctAnswers(result.getNoOfCorrectAns() != null ? result.getNoOfCorrectAns().intValue() : 0)
					.timeSpent(result.getTimeSpent()).build();
		}).collect(Collectors.toList());
	}

	private Map<String, PerformanceAnalyticsDto.SubjectPerformance> calculateSubjectPerformance(
			List<StudentTestResult> results) {

		Map<String, PerformanceAnalyticsDto.SubjectPerformance> subjectPerformance = new HashMap<>();

		// This would require more complex logic to calculate subject-wise performance
		// For now, return empty map or implement based on your needs

		return subjectPerformance;
	}

	private Integer calculateAverageRank(List<StudentTestResult> results, Long studentId) {
		if (results.isEmpty())
			return null;

		int totalRank = 0;
		int rankedTests = 0;

		for (StudentTestResult result : results) {
			Integer rank = getRankForStudent(result.getTestId(), studentId);
			if (rank != null) {
				totalRank += rank;
				rankedTests++;
			}
		}

		return rankedTests > 0 ? totalRank / rankedTests : null;
	}

	private Integer calculateTotalParticipants(List<StudentTestResult> results) {
		Set<Long> testIds = results.stream().map(StudentTestResult::getTestId).collect(Collectors.toSet());

		int total = 0;
		for (Long testId : testIds) {
			List<StudentTestResult> testResults = testResultRepository.findByTestId(testId);
			if (testResults != null) {
				total += testResults.size();
			}
		}
		return total;
	}

// Updated method for specific test
	@Override
	public List<RankingResponseDto> getTopRanksForTest(Long testId, Integer topN) {
		try {
			log.info("Fetching top {} ranks for test ID: {}", topN, testId);

			// Get test details
			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new CustomException("Test not found with ID: " + testId, HttpStatus.NOT_FOUND));

			// Get all results for this test (from repository or manual filtering)
			List<StudentTestResult> allResults;

			// Try to use repository method if it exists
			try {
				// This will throw an error if method doesn't exist
				allResults = testResultRepository.findByTestIdOrderByTotalObtainedMarksDesc(testId);
			} catch (Exception e) {
				// Fallback: Get all results and sort manually
				log.warn("Repository method not found, using manual sorting");
				allResults = testResultRepository.findAll().stream()
						.filter(result -> result.getTestId() != null && result.getTestId().equals(testId))
						.sorted((r1, r2) -> Double.compare(r2.getTotalObtainedMarks(), r1.getTotalObtainedMarks()))
						.collect(Collectors.toList());
			}

			if (allResults.isEmpty()) {
				log.warn("No results found for test ID: {}", testId);
				return new ArrayList<>();
			}

			// Prepare top rank list
			List<RankingResponseDto> topRankList = new ArrayList<>();
			int rank = 1;

			for (StudentTestResult result : allResults) {
				if (topN != null && rank > topN) {
					break;
				}

				RankingResponseDto rankDto = createRankingResponseDto(result, rank, test);
				topRankList.add(rankDto);
				rank++;
			}

			log.info("Successfully fetched top {} ranks for test {}", topRankList.size(), testId);
			return topRankList;

		} catch (Exception e) {
			log.error("Error fetching top ranks for test: {}", e.getMessage(), e);
			throw new CustomException("Error fetching top ranks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private RankingResponseDto createRankingResponseDto(StudentTestResult result, int rank, Tests test) {
		try {
			// Get student details
			Users student = userRepository.findById(result.getStudentId()).orElse(new Users()); // Create empty user if
																								// not found

			// Count correct answers
			Integer correctAnswers = result.getNoOfCorrectAns() != null ? result.getNoOfCorrectAns().intValue() : 0;

			return RankingResponseDto.builder().testId(result.getTestId()).studentId(result.getStudentId())
					.studentEmail(result.getStudentEmail())
					.studentName(student.getName() != null ? student.getName() : "Unknown").rank(rank)
					.totalMarks(result.getTotalObtainedMarks()).correctAnswers(correctAnswers)
					.timeSpent(result.getTimeSpent() != null ? result.getTimeSpent() : "N/A")
					.examType(test.getExamType() != null ? test.getExamType().name() : "Unknown")
					.testName(test.getTestName()).build();

		} catch (Exception e) {
			log.error("Error creating RankingResponseDto: {}", e.getMessage());
			return RankingResponseDto.builder().testId(result.getTestId()).studentId(result.getStudentId())
					.studentEmail(result.getStudentEmail()).rank(rank).totalMarks(result.getTotalObtainedMarks())
					.build();
		}
	}

//1. Get Last Test Leaderboard for Institute
	@Override
	public StudentPerformanceDto getLastTestLeaderboardForInstitute(String instituteEmail, Integer topN,
			Boolean detailed) {
		try {
			log.info("Getting last test leaderboard for institute: {}, top: {}", instituteEmail, topN);

			// Find the latest test for this institute
			Tests latestTest = findLatestTestForInstitute(instituteEmail);

			if (latestTest == null) {
				log.warn("No tests found for institute: {}", instituteEmail);
				return createEmptyLeaderboardResponse(instituteEmail, "No tests available");
			}

			log.info("Latest test found: {} (ID: {})", latestTest.getTestName(), latestTest.getId());

			// Get all results for this test
			List<StudentTestResult> allResults = testResultRepository
					.findByTestIdAndInstituteEmailOrderByTotalObtainedMarksDesc(latestTest.getId(), instituteEmail);

			if (allResults.isEmpty()) {
				log.warn("No results found for test ID: {}", latestTest.getId());
				return createEmptyLeaderboardResponse(instituteEmail, "No students have taken the latest test yet");
			}

			// Create leaderboard entries
			List<StudentPerformanceDto.RankEntry> leaderboard = new ArrayList<>();
			int rank = 1;
			Double previousMarks = null;
			int sameRankCount = 0;

			for (StudentTestResult result : allResults) {
				if (topN != null && leaderboard.size() >= topN) {
					break;
				}

				// Get student details
				Users student = userRepository.findById(result.getStudentId()).orElse(new Users());

				// Create rank entry
				StudentPerformanceDto.RankEntry rankEntry = StudentPerformanceDto.RankEntry.builder()
						.studentId(result.getStudentId())
						.studentName(student.getName() != null ? student.getName() : "Student")
						.studentEmail(result.getStudentEmail()).marksObtained(result.getTotalObtainedMarks())
						.timeSpent(result.getTimeSpent())
						.noOfCorrectAnswers(
								result.getNoOfCorrectAns() != null ? result.getNoOfCorrectAns().intValue() : 0)
						.noOfIncorrectAnswers(
								result.getNoOfInCorrectAns() != null ? result.getNoOfInCorrectAns().intValue() : 0)
						.selectedLanguageSubjectId(result.getSelectedLanguageSubjectId()).build();

				// Handle ranking (with tie logic)
				if (previousMarks != null && result.getTotalObtainedMarks().equals(previousMarks)) {
					// Same marks as previous, same rank
					rankEntry.setRank(rank - 1);
					sameRankCount++;
				} else {
					// Different marks, new rank
					rankEntry.setRank(rank + sameRankCount);
					rank = rank + sameRankCount + 1;
					sameRankCount = 0;
					previousMarks = result.getTotalObtainedMarks();
				}

				// Add detailed information if requested
				if (detailed != null && detailed) {
					rankEntry.setTestId(result.getTestId());
					rankEntry.setTestName(result.getTestName());
					rankEntry.setTestAttemptedDate(result.getTestAttemptedDate());
					rankEntry.setRemark(result.getRemark());

					// Add selected language subject name if available
					if (result.getSelectedLanguageSubjectId() != null) {
						Subjects subject = subjectRepository.findById(result.getSelectedLanguageSubjectId())
								.orElse(null);
						if (subject != null) {
							rankEntry.setSelectedLanguageSubjectName(subject.getName());
						}
					}
				}

				leaderboard.add(rankEntry);
			}

			// Build the response
			return StudentPerformanceDto.builder().analysisType("INSTITUTE_LEADERBOARD").performanceType("LATEST_TEST")
					.instituteEmail(instituteEmail).testId(latestTest.getId()).testName(latestTest.getTestName())
					.examType(latestTest.getExamType()).leaderboard(leaderboard).totalParticipants(allResults.size())
					.totalMarks(calculateTotalMarks(latestTest)).averageMarks(calculateAverageMarks(allResults))
					.highestMarks(
							allResults.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).max().orElse(0.0))
					.lowestMarks(
							allResults.stream().mapToDouble(StudentTestResult::getTotalObtainedMarks).min().orElse(0.0))
					.testDate(allResults.get(0).getTestAttemptedDate()).build();

		} catch (Exception e) {
			log.error("Error fetching last test leaderboard for institute {}: {}", instituteEmail, e.getMessage(), e);
			throw new CustomException("Error fetching leaderboard: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	@Override
//	public List<TestNameDto> getTestListByStudentId(Long studentId) {
//		
//		
//	
//	}

//2. Get Overall Institute Leaderboard (across all tests)
	@Override
	public StudentPerformanceDto getOverallInstituteLeaderboard(String instituteEmail, Integer topN,
			LocalDate startDate, LocalDate endDate) {
		try {
			log.info("Getting overall leaderboard for institute: {}, date range: {} to {}", instituteEmail, startDate,
					endDate);

			// Get all test results for the institute
			List<StudentTestResult> instituteResults;
			if (startDate != null && endDate != null) {
				instituteResults = testResultRepository.findByInstituteEmailAndTestAttemptedDateBetween(instituteEmail,
						startDate, endDate);
			} else {
				instituteResults = testResultRepository.findByInstituteEmail(instituteEmail);
			}

			if (instituteResults.isEmpty()) {
				log.warn("No test results found for institute: {}", instituteEmail);
				return createEmptyLeaderboardResponse(instituteEmail, "No test results available");
			}

			// Group results by student and calculate aggregate statistics
			Map<Long, StudentAggregateStats> studentStatsMap = new HashMap<>();

			for (StudentTestResult result : instituteResults) {
				Long studentId = result.getStudentId();
				StudentAggregateStats stats = studentStatsMap.getOrDefault(studentId,
						new StudentAggregateStats(studentId, result.getStudentEmail()));

				stats.addTestResult(result);
				studentStatsMap.put(studentId, stats);
			}

			// Convert to list and sort by total marks (descending)
			List<StudentAggregateStats> sortedStats = studentStatsMap.values().stream()
					.sorted((s1, s2) -> Double.compare(s2.getTotalMarks(), s1.getTotalMarks()))
					.collect(Collectors.toList());

			// Create leaderboard entries
			List<StudentPerformanceDto.RankEntry> leaderboard = new ArrayList<>();
			int rank = 1;
			Double previousTotalMarks = null;
			int sameRankCount = 0;

			for (StudentAggregateStats stats : sortedStats) {
				if (topN != null && leaderboard.size() >= topN) {
					break;
				}

				// Get student details
				Users student = userRepository.findById(stats.getStudentId()).orElse(new Users());

				// Create rank entry
				StudentPerformanceDto.RankEntry rankEntry = StudentPerformanceDto.RankEntry.builder()
						.studentId(stats.getStudentId())
						.studentName(student.getName() != null ? student.getName() : "Student")
						.studentEmail(stats.getStudentEmail()).marksObtained(stats.getTotalMarks())
//						.totalTestsTaken(stats.getTestCount()).averageMarks(stats.getAverageMarks())
						.highestMarks(stats.getHighestMarks()).lowestMarks(stats.getLowestMarks())
						.totalCorrectAnswers(stats.getTotalCorrectAnswers())
						.totalIncorrectAnswers(stats.getTotalIncorrectAnswers()).accuracy(stats.getAccuracy()).build();

				// Handle ranking (with tie logic)
				if (previousTotalMarks != null && stats.getTotalMarks() == (previousTotalMarks)) {
					// Same total marks as previous, same rank
					rankEntry.setRank(rank - 1);
					sameRankCount++;
				} else {
					// Different total marks, new rank
					rankEntry.setRank(rank + sameRankCount);
					rank = rank + sameRankCount + 1;
					sameRankCount = 0;
					previousTotalMarks = stats.getTotalMarks();
				}

				leaderboard.add(rankEntry);
			}

			// Calculate institute-wide statistics
			double instituteAverageMarks = sortedStats.stream().mapToDouble(StudentAggregateStats::getAverageMarks)
					.average().orElse(0.0);

			// Build the response
			return StudentPerformanceDto.builder().analysisType("INSTITUTE_LEADERBOARD").performanceType("OVERALL")
					.instituteEmail(instituteEmail).leaderboard(leaderboard).totalParticipants(sortedStats.size())
//					.totalTestsTaken(instituteResults.size())
					.averageMarks(instituteAverageMarks)
					.highestMarks(
							sortedStats.stream().mapToDouble(StudentAggregateStats::getTotalMarks).max().orElse(0.0))
					.lowestMarks(
							sortedStats.stream().mapToDouble(StudentAggregateStats::getTotalMarks).min().orElse(0.0))
					.fromDate(startDate).toDate(endDate).build();

		} catch (Exception e) {
			log.error("Error fetching overall leaderboard for institute {}: {}", instituteEmail, e.getMessage(), e);
			throw new CustomException("Error fetching overall leaderboard: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Helper class for student aggregate statistics
	private static class StudentAggregateStats {
		private Long studentId;
		private String studentEmail;
		private double totalMarks = 0.0;
		private int testCount = 0;
		private double highestMarks = 0.0;
		private double lowestMarks = Double.MAX_VALUE;
		private int totalCorrectAnswers = 0;
		private int totalIncorrectAnswers = 0;
		private double totalTimeSpent = 0; // in seconds

		public StudentAggregateStats(Long studentId, String studentEmail) {
			this.studentId = studentId;
			this.studentEmail = studentEmail;
		}

		public void addTestResult(StudentTestResult result) {
			double marks = result.getTotalObtainedMarks() != null ? result.getTotalObtainedMarks() : 0.0;
			totalMarks += marks;
			testCount++;
			highestMarks = Math.max(highestMarks, marks);
			lowestMarks = Math.min(lowestMarks, marks);

			if (result.getNoOfCorrectAns() != null) {
				totalCorrectAnswers += result.getNoOfCorrectAns().intValue();
			}

			if (result.getNoOfInCorrectAns() != null) {
				totalIncorrectAnswers += result.getNoOfInCorrectAns().intValue();
			}

			// Add time if available
//	        if (result.getTimeSpent() != null) {
//	            totalTimeSpent += convertTimeToSeconds(result.getTimeSpent());
//	        }
		}

		public double getAverageMarks() {
			return testCount > 0 ? totalMarks / testCount : 0.0;
		}

		public double getAccuracy() {
			int totalAttempted = totalCorrectAnswers + totalIncorrectAnswers;
			return totalAttempted > 0 ? (totalCorrectAnswers * 100.0) / totalAttempted : 0.0;
		}

		public double getAverageTimePerTest() {
			return testCount > 0 ? totalTimeSpent / testCount : 0.0;
		}

		// Getters
		public Long getStudentId() {
			return studentId;
		}

		public String getStudentEmail() {
			return studentEmail;
		}

		public double getTotalMarks() {
			return totalMarks;
		}

		public int getTestCount() {
			return testCount;
		}

		public double getHighestMarks() {
			return highestMarks;
		}

		public double getLowestMarks() {
			return lowestMarks != Double.MAX_VALUE ? lowestMarks : 0.0;
		}

		public int getTotalCorrectAnswers() {
			return totalCorrectAnswers;
		}

		public int getTotalIncorrectAnswers() {
			return totalIncorrectAnswers;
		}

	}

	// Helper method to find latest test for an institute
	private Tests findLatestTestForInstitute(String instituteEmail) {
		try {
			// Method 1: Get latest test from test results
			List<StudentTestResult> testResults = testResultRepository
					.findByInstituteEmailOrderByTestAttemptedDateDesc(instituteEmail);

			if (!testResults.isEmpty()) {
				Long latestTestId = testResults.get(0).getTestId();
				return testRepository.findById(latestTestId).orElse(null);
			}

			// Method 2: Get latest test from test repository
			List<Tests> instituteTests = testRepository.findByInstitutesEmail(instituteEmail);

			if (!instituteTests.isEmpty()) {
				return instituteTests.stream().filter(test -> test.getCreatedAt() != null)
						.max((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())).orElse(instituteTests.get(0));
			}

			return null;

		} catch (Exception e) {
			log.error("Error finding latest test for institute {}: {}", instituteEmail, e.getMessage());
			return null;
		}
	}

	// Helper method to calculate average marks
	private double calculateAverageMarks(List<StudentTestResult> results) {
		return results.stream()
				.mapToDouble(result -> result.getTotalObtainedMarks() != null ? result.getTotalObtainedMarks() : 0.0)
				.average().orElse(0.0);
	}

	// Helper method to create empty leaderboard response
	private StudentPerformanceDto createEmptyLeaderboardResponse(String instituteEmail, String message) {
		return StudentPerformanceDto.builder().analysisType("INSTITUTE_LEADERBOARD").performanceType("OVERALL")
				.instituteEmail(instituteEmail).leaderboard(new ArrayList<>()).totalParticipants(0)
//	            .message(message)
				.build();
	}

//	private List<TestNameDto> getTestListByStudentId(Long studentId, ExamType examType) {
//
//		userRepository.findById(studentId)
//				.orElseThrow(() -> new CustomException("user not found", HttpStatus.NOT_FOUND));
//
//		List<StudentTestResult> testDetails = testResultRepository.findByStudentId(studentId);
//
//		List<StudentTestResult> examTypeTestDetailsList = null;
//
//		if (examType == ExamType.valueOf("EXAM_WISE")) {
//
//			examTypeTestDetailsList = testDetails.stream().filter(
//
//					testDetail -> testRepository.findById(testDetail.getId()).get().getExamType()
//							.equals(ExamType.valueOf("EXAM_WISE"))
//
//			).collect(Collectors.toList());
//
//		} else {
//
//			examTypeTestDetailsList = testDetails.stream().filter(
//
//					testDetail -> testRepository.findById(testDetail.getId()).get().getExamType()
//							.equals(ExamType.valueOf("SUBJECT_WISE"))
//
//			).collect(Collectors.toList());
//
//		}
//
//		List<TestNameDto> dtoo = examTypeTestDetailsList.stream().map((testDetail) -> {
//
//			TestNameDto dto = new TestNameDto();
//
//			dto.setTestId(testDetail.getTestId());
//			dto.setTestName(testDetail.getTestName());
//
//			return dto;
//		}).collect(Collectors.toList());
//
//		return dtoo;
//
//	}

	private List<TestNameDto> getTestListByStudentId(Long studentId, ExamType examType) {

		// Validate student exists
		userRepository.findById(studentId)
				.orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

		// Get all test results for the student
		List<StudentTestResult> testDetails = testResultRepository.findByStudentId(studentId);

		if (testDetails.isEmpty()) {
			return Collections.emptyList();
		}

		// Extract test IDs from test results
		List<Long> testIds = testDetails.stream().map(StudentTestResult::getTestId).distinct()
				.collect(Collectors.toList());

		// Fetch all tests at once (avoid N+1 problem)
		List<Tests> tests = testRepository.findAllById(testIds);

		// Create a map for quick lookup
		Map<Long, Tests> testMap = tests.stream().collect(Collectors.toMap(Tests::getId, test -> test));

		// Filter tests by exam type
		List<Tests> filteredTests = tests.stream().filter(test -> examType.equals(test.getExamType()))
				.collect(Collectors.toList());

		// Convert to DTO
		return filteredTests.stream().map(test -> new TestNameDto(test.getId(), test.getTestName()))
				.collect(Collectors.toList());
	}

	@Override
	public List<TestNameDto> getTestListByStudentIdExamType(Long studentId, ExamType examType) {

		List<TestNameDto> dtoo = getTestListByStudentId(studentId, examType);

		return dtoo;

	}

	@Override
	public StudentLeaderboardDto getStudentLeaderboardForLastAttemptedTest(Long studentId, String instituteEmail,
			Integer topN, Boolean includeStudent) {
		try {
			log.info("Getting student leaderboard for last attempted test - Student ID: {}, Institute: {}", studentId,
					instituteEmail);

			// 1. Find the student's last attempted test
			StudentTestResult studentLastResult = testResultRepository
					.findTopByStudentIdAndInstituteEmailOrderByTestAttemptedDateDesc(studentId, instituteEmail);

			if (studentLastResult == null) {
				throw new CustomException("Student has not attempted any tests yet", HttpStatus.NOT_FOUND);
			}

			Long lastTestId = studentLastResult.getTestId();
			Tests test = testRepository.findById(lastTestId)
					.orElseThrow(() -> new CustomException("Test not found", HttpStatus.NOT_FOUND));

			log.info("Student's last test: {} (ID: {}), Marks: {}", studentLastResult.getTestName(), lastTestId,
					studentLastResult.getTotalObtainedMarks());

			// 2. Get all results for this test from the same institute
			List<StudentTestResult> allResults = testResultRepository
					.findByTestIdAndInstituteEmailOrderByTotalObtainedMarksDesc(lastTestId, instituteEmail);

			if (allResults.isEmpty()) {
				throw new CustomException("No results found for this test", HttpStatus.NOT_FOUND);
			}

			// 3. Calculate ranks and create leaderboard
			List<StudentLeaderboardDto.StudentRankEntry> leaderboard = new ArrayList<>();
			Map<Long, Users> studentCache = new HashMap<>();
			int rank = 1;
			Double previousMarks = null;
			int sameRankCount = 0;
			Integer studentRank = null;
			StudentTestResult studentResultInList = null;

			// First pass: Calculate ranks and find student's position
			for (StudentTestResult result : allResults) {
				// Handle tie ranking
				if (previousMarks != null && result.getTotalObtainedMarks().equals(previousMarks)) {
					// Same marks as previous, same rank
					rank = rank - 1;
					sameRankCount++;
				} else {
					// Different marks, new rank
					rank = rank + sameRankCount;
					sameRankCount = 0;
					previousMarks = result.getTotalObtainedMarks();
				}

				// Check if this is the current student
				boolean isCurrentStudent = result.getStudentId().equals(studentId);
				if (isCurrentStudent) {
					studentRank = rank;
					studentResultInList = result;
				}
			}

			// Reset for second pass (building the leaderboard)
			rank = 1;
			previousMarks = null;
			sameRankCount = 0;
			int processedCount = 0;

			// Second pass: Build the leaderboard
			for (StudentTestResult result : allResults) {
				// Apply topN filter if specified
				if (topN != null && processedCount >= topN && !result.getStudentId().equals(studentId)) {
					// Skip if we've reached topN and this isn't the current student
					continue;
				}

				// Handle tie ranking
				if (previousMarks != null && result.getTotalObtainedMarks().equals(previousMarks)) {
					// Same marks as previous, same rank
					rank = rank - 1;
					sameRankCount++;
				} else {
					// Different marks, new rank
					rank = rank + sameRankCount;
					sameRankCount = 0;
					previousMarks = result.getTotalObtainedMarks();
				}

				boolean isCurrentStudent = result.getStudentId().equals(studentId);

				// Skip current student if includeStudent is false
				if (!Boolean.TRUE.equals(includeStudent) && isCurrentStudent) {
					continue;
				}

				// Get student details (with caching)
				Users student = studentCache.get(result.getStudentId());
				if (student == null) {
					student = userRepository.findById(result.getStudentId()).orElse(new Users());
					studentCache.put(result.getStudentId(), student);
				}

				// Get selected language subject name if available
				String selectedLanguageSubjectName = null;
				if (result.getSelectedLanguageSubjectId() != null) {
					Subjects subject = subjectRepository.findById(result.getSelectedLanguageSubjectId()).orElse(null);
					if (subject != null) {
						selectedLanguageSubjectName = subject.getName();
					}
				}

				// Create rank entry
				StudentLeaderboardDto.StudentRankEntry rankEntry = StudentLeaderboardDto.StudentRankEntry.builder()
						.rank(rank).studentId(result.getStudentId())
						.studentName(student.getName() != null ? student.getName() : "Student")
						.studentEmail(result.getStudentEmail()).marksObtained(result.getTotalObtainedMarks())
						.noOfCorrectAnswers(
								result.getNoOfCorrectAns() != null ? result.getNoOfCorrectAns().intValue() : 0)
						.noOfIncorrectAnswers(
								result.getNoOfInCorrectAns() != null ? result.getNoOfInCorrectAns().intValue() : 0)
						.timeSpent(result.getTimeSpent()).remark(result.getRemark())
						.selectedLanguageSubjectId(result.getSelectedLanguageSubjectId())
						.selectedLanguageSubjectName(selectedLanguageSubjectName)
						.attemptedTestLanguage(
								result.getAttemptedTestLanguage() != null ? result.getAttemptedTestLanguage().name()
										: null)
						.isCurrentStudent(isCurrentStudent).build();

				leaderboard.add(rankEntry);
				processedCount++;

				// Move to next rank if not a tie
				if (!result.getTotalObtainedMarks().equals(previousMarks)) {
					rank++;
				}
			}

			// If student wasn't found in the leaderboard (shouldn't happen but for safety)
			if (studentResultInList == null) {
				// Add current student separately if requested
				if (Boolean.TRUE.equals(includeStudent)) {
					Users student = userRepository.findById(studentId)
							.orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));

					String selectedLanguageSubjectName = null;
					if (studentLastResult.getSelectedLanguageSubjectId() != null) {
						Subjects subject = subjectRepository.findById(studentLastResult.getSelectedLanguageSubjectId())
								.orElse(null);
						if (subject != null) {
							selectedLanguageSubjectName = subject.getName();
						}
					}

					StudentLeaderboardDto.StudentRankEntry studentEntry = StudentLeaderboardDto.StudentRankEntry
							.builder().rank(studentRank).studentId(studentId).studentName(student.getName())
							.studentEmail(student.getEmail()).marksObtained(studentLastResult.getTotalObtainedMarks())
							.noOfCorrectAnswers(studentLastResult.getNoOfCorrectAns() != null
									? studentLastResult.getNoOfCorrectAns().intValue()
									: 0)
							.noOfIncorrectAnswers(studentLastResult.getNoOfInCorrectAns() != null
									? studentLastResult.getNoOfInCorrectAns().intValue()
									: 0)
							.timeSpent(studentLastResult.getTimeSpent()).remark(studentLastResult.getRemark())
							.selectedLanguageSubjectId(studentLastResult.getSelectedLanguageSubjectId())
							.selectedLanguageSubjectName(selectedLanguageSubjectName)
							.attemptedTestLanguage(studentLastResult.getAttemptedTestLanguage() != null
									? studentLastResult.getAttemptedTestLanguage().name()
									: null)
							.isCurrentStudent(true).build();

					// Insert student at correct position in leaderboard
					if (studentRank != null) {
						int insertIndex = Math.min(studentRank - 1, leaderboard.size());
						leaderboard.add(insertIndex, studentEntry);
					} else {
						leaderboard.add(studentEntry);
					}
				}
			}

			// 4. Get current student details
			Users currentStudent = userRepository.findById(studentId)
					.orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));

			// 5. Calculate statistics
			Double totalTestMarks = calculateTotalMarks(test, studentLastResult.getSelectedLanguageSubjectId());
			Double averageMarks = allResults.stream()
					.mapToDouble(r -> r.getTotalObtainedMarks() != null ? r.getTotalObtainedMarks() : 0.0).average()
					.orElse(0.0);
			Double highestMarks = allResults.stream()
					.mapToDouble(r -> r.getTotalObtainedMarks() != null ? r.getTotalObtainedMarks() : 0.0).max()
					.orElse(0.0);
			Double lowestMarks = allResults.stream()
					.mapToDouble(r -> r.getTotalObtainedMarks() != null ? r.getTotalObtainedMarks() : 0.0).min()
					.orElse(0.0);

			// 6. Build and return the response
			return StudentLeaderboardDto.builder().studentId(studentId).studentName(currentStudent.getName())
					.studentEmail(currentStudent.getEmail()).testId(lastTestId).testName(test.getTestName())
					.examType(test.getExamType() != null ? test.getExamType().name() : null)
					.studentMarks(studentLastResult.getTotalObtainedMarks()).studentRank(studentRank)
					.totalParticipants(allResults.size()).totalMarks(totalTestMarks).averageMarks(averageMarks)
					.highestMarks(highestMarks).lowestMarks(lowestMarks)
					.testDate(studentLastResult.getTestAttemptedDate()).timeSpent(studentLastResult.getTimeSpent())
					.leaderboard(leaderboard).instituteEmail(instituteEmail).build();

		} catch (CustomException e) {
			log.error("Custom exception in getStudentLeaderboardForLastAttemptedTest: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Error getting student leaderboard for last attempted test: {}", e.getMessage(), e);
			throw new CustomException("Error fetching student leaderboard: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
