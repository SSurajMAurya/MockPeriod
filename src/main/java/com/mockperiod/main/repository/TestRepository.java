package com.mockperiod.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.dto.TestDto;
import com.mockperiod.main.entities.Exam;
import com.mockperiod.main.entities.ExamType;
import com.mockperiod.main.entities.Language;
import com.mockperiod.main.entities.Questions;
import com.mockperiod.main.entities.StudentTestResult;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.entities.Tests;

public interface TestRepository extends JpaRepository<Tests, Long> {
	Optional<Tests> findByTestNameAndExam(String testName, Exam exam);

	List<Tests> findByExamId(Long examId);

	// Custom query to fetch test with questions and options - FIXED VERSION

	boolean existsByTestNameAndExam(String testName, Exam exam);

//	@Query("SELECT DISTINCT t FROM Tests t " + "LEFT JOIN FETCH t.questions q " + "LEFT JOIN FETCH q.options o "
//			+ "WHERE t.id = :testId")
//	Optional<Tests> findByIdWithQuestionsAndOptions(@Param("testId") Long testId);

//	@Query("SELECT DISTINCT t FROM Tests t " + "LEFT JOIN FETCH t.questions q " + "LEFT JOIN FETCH q.options o "
//			+ "WHERE t.id = :testId")
//	Optional<Tests> findByIdWithQuestionsAndOptions(@Param("testId") Long testId);

//	@Query("SELECT DISTINCT t FROM Tests t " + "LEFT JOIN FETCH t.questions q " + "LEFT JOIN FETCH q.options o "
//			+ "WHERE t.id = :testId AND q.language = :language")
//	Optional<Tests> findByIdWithQuestionsAndOptionsByLanguage(@Param("testId") Long testId,
//			@Param("language") Language language);
	
	
	@Query("SELECT DISTINCT t FROM Tests t " + "LEFT JOIN FETCH t.questions q " + "LEFT JOIN FETCH q.options o "
			+ "WHERE t.id = :testId AND q.language = :language")
	Tests findByIdWithQuestionsAndOptionsByLanguage(@Param("testId") Long testId,
			@Param("language") Language language);
	
	
	

	@Query("SELECT DISTINCT t FROM Tests t " + "LEFT JOIN FETCH t.questions q " + "LEFT JOIN FETCH q.options")
	List<Tests> findAllWithQuestionsAndOptions();

	@Query("SELECT t FROM Tests t JOIN t.institutes i WHERE i.id = :instituteId")
	List<Tests> findByInstituteId(@Param("instituteId") Long instituteId);

	@Query("SELECT t FROM Tests t JOIN t.institutes i WHERE i.id = :instituteId AND t.examType = :examType")
	List<Tests> findByInstituteIdAndExamType(@Param("instituteId") Long instituteId,
			@Param("examType") ExamType examType);

	Optional<Tests> findByTestNameAndSubject(String testName, Subjects subject);

	boolean existsByTestNameAndSubject(String testName, Subjects subject);

	// Find tests by exam type
	List<Tests> findByExamType(ExamType examType);

	List<Tests> findBySubjectId(Long subjectId);

	List<Tests> findByExamIdAndInstitutesId(Long examId, Long instituteId);

	List<Tests> findByInstitutesId(Long instituteId);

	List<Tests> findByInstitutesEmail(String instituteEmail);

	List<Tests> findBySubjectIdAndInstitutesEmail(Long subjectId, String instituteEmail);
//    List<Tests> findBySubjectId(Long subjectId);

	List<Tests> findByExamIdAndInstitutesEmail(Long examId, String instituteEmail);

//    List<Tests> findByExamIdAndInstitutesId(Long examId, Long id);
//    
	List<Tests> findBySubjectIdAndInstitutesId(Long subjectId, Long id);

//        List<Subjects> findBySubjects(Tests)

	@Query("SELECT t FROM Tests t JOIN t.institutes i WHERE i.email = :instituteEmail")
	List<Tests> findByInstituteEmail(@Param("instituteEmail") String instituteEmail);
	
//	
//	@Query("SELECT q FROM Questions q " + "LEFT JOIN FETCH q.options " + "WHERE q.test.id = :testId "
//			+ "AND q.language = :language")
//	List<Questions> findByIdWithQuestionsAndOptionsByLanguage(@Param("testId") Long testId, @Param("language") Language language)
//	;
	
	
	 // To get test with all questions (for fallback)
    @Query("SELECT DISTINCT t FROM Tests t " + 
           "LEFT JOIN FETCH t.questions q " + 
           "LEFT JOIN FETCH q.options " +
           "WHERE t.id = :testId")
    Optional<Tests> findByIdWithQuestionsAndOptions(@Param("testId") Long testId);
    
    
}



