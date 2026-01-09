package com.mockperiod.main.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.StudentTestResult;

public interface StudentTestResultRepository extends JpaRepository<StudentTestResult, Long> {

	List<StudentTestResult> findByInstituteEmail(String instituteEmail);

	List<StudentTestResult> findByTestAttemptedDateAndInstituteEmail(LocalDate date, String instituteEmail);

	List<StudentTestResult> findByStudentIdAndInstituteEmail(Long studentId, String instituteEmail);

	List<StudentTestResult> findByStudentId(Long studentId);

	StudentTestResult findByStudentIdAndTestId(Long studentId, Long testId);

	List<StudentTestResult> findByTestIdInAndInstituteEmail(List<Long> testIds, String instituteEmail);

	List<StudentTestResult> findByStudentIdAndTestIdIn(Long studentId, List<Long> testIds);

	List<StudentTestResult> findByTestIdAndInstituteEmail(Long testId, String instituteEmail);

	List<StudentTestResult> findByTestIdOrderByTotalObtainedMarksDesc(Long testId);

	StudentTestResult findTopByStudentIdAndInstituteEmailOrderByTestAttemptedDateDesc(Long studentId,
			String instituteEmail);

	List<StudentTestResult> findByTestId(Long testId);

	// Additional useful methods
	List<StudentTestResult> findByStudentIdAndTestAttemptedDateBetween(Long studentId, LocalDate startDate,
			LocalDate endDate);

	List<StudentTestResult> findByInstituteEmailAndTestAttemptedDateBetween(String instituteEmail, LocalDate startDate,
			LocalDate endDate);

	Long countByTestIdAndInstituteEmail(Long testId, String instituteEmail);

	boolean existsByStudentIdAndTestId(Long studentId, Long testId);

	// Find results by institute email, ordered by test date descending
	@Query("SELECT str FROM StudentTestResult str WHERE str.instituteEmail = :instituteEmail ORDER BY str.testAttemptedDate DESC")
	List<StudentTestResult> findByInstituteEmailOrderByTestAttemptedDateDesc(
			@Param("instituteEmail") String instituteEmail);

	// Count results by test ID
	Integer countByTestId(Long testId);
	
	
	List<StudentTestResult> findByTestIdAndInstituteEmailOrderByTotalObtainedMarksDesc(Long id , String instituteEmail);
	
//	List<StudentTestResult> findByStudentId(Long studentId);
	
	 Long countByStudentId(Long studentId);
	 
	 boolean  existsByStudentId(Long studentId);

}
