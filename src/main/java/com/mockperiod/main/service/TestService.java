package com.mockperiod.main.service;

import com.mockperiod.main.dto.TestDto;
import com.mockperiod.main.dto.TestStatusResponse;
import com.mockperiod.main.dto.QuestionDto;
import com.mockperiod.main.dto.StudentListWithTestDto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface TestService {

    TestDto createOrUpdateTest(TestDto testDto);

    TestDto getTestById(Long id);

//    TestDto getTestWithQuestionsById(Long id);
    
    TestDto getTestWithQuestionsByIdAndLanguage(Long id , String language , String subjectName); 

    List<TestDto> getAllTests();

    List<TestDto> getAllTestsWithQuestions();

    TestDto updateTest(Long id, TestDto testDto);

    void deleteTest(Long id);

    List<TestDto> getTestsByExamId(Long examId);

    List<TestDto> getTestsByExamIdWithQuestions(Long examId);

    TestDto processExcelFile(MultipartFile excelFile, TestDto testDto);

    boolean testExistsByNameAndExam(String testName, Long examId);

    TestDto addQuestionsToTest(Long testId, List<QuestionDto> questions);

    List<QuestionDto> getQuestionsByTestId(Long testId);

    List<TestDto> getTestsByInstituteId(Long instituteId, String examType);

    List<TestDto> getTestsByInstituteIdWithQuestions(Long instituteId);
    
    TestStatusResponse isTestCurrentlyOpen(Long testId, Long instituteId, Long studentId);
    
    // New methods for exam-wise and subject-wise tests
    List<TestDto> getTestsByExamType(String examType);
    
    List<TestDto> getSubjectWiseTestsBySubject(Long subjectId);
    
    List<TestDto> getExamWiseTestsByExam(Long examId);
    
    // New method to check if test exists by name and subject (for subject-wise tests)
    boolean testExistsByNameAndSubject(String testName, Long subjectId);
    
    List<TestDto> getAllTestByExamType(String examType);
    
    void deleteQuestionFromTest(Long testId, Long questionId);
    
     QuestionDto updateQuestionFromTest(Long testId, Long questionId, QuestionDto questionDto);
     
     
     List<TestDto> getTestsByExamAndInstitute(Long examId, Long instituteId);
     
//     List<TestDto> getTestsByExamId(Long examId);
     List<TestDto> getTestsByInstituteId(Long instituteId);
     
     List<TestDto> getTestsBySubjectAndInstitute(Long subjectId, Long instituteId);
     
     List<StudentListWithTestDto> getAllStudentForInstituteWithTestAttempted(Long instituteId);
     
}