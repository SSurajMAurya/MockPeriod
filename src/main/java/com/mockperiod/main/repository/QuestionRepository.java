package com.mockperiod.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.Chapter;
import com.mockperiod.main.entities.Language;
import com.mockperiod.main.entities.Questions;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.entities.Tests;

public interface QuestionRepository extends JpaRepository<Questions, Long> {

	List<Questions> findByTest(Tests test);

	List<Questions> findBySubject(Subjects subjects);

	List<Questions> findByChapter(Chapter chapter);

//	@Query("SELECT q FROM Questions q JOIN FETCH q.options WHERE q.test = :test")
//	List<Questions> findByTestWithOptions(@Param("test") Tests test);

	@Query("SELECT DISTINCT q FROM Questions q LEFT JOIN FETCH q.options WHERE q.test = :test")
	List<Questions> findByTestWithOptions(@Param("test") Tests test);

	// Method 2: Use EntityGraph (Recommended)
//	    @EntityGraph(attributePaths = {"options"})
//	    List<Questions> findByTest(Tests test);

	// Method 3: Use test ID instead of entity
	@EntityGraph(attributePaths = { "options" })
	List<Questions> findByTestId(Long testId);

	@Query(value = "SELECT q.*, o.* FROM questions q " + "LEFT JOIN question_options o ON q.id = o.question_id "
			+ "WHERE q.test_id = :testId", nativeQuery = true)
	List<Object[]> findQuestionsWithOptionsByTestId(@Param("testId") Long testId);

	@Query("SELECT q FROM Questions q WHERE q.test.id = :testId AND q.subject.id = :subjectId")
	List<Questions> findByTestAndSubject(@Param("testId") Long testId, @Param("subjectId") Long subjectId);

	@Query("SELECT COUNT(q) FROM Questions q WHERE q.test.id = :testId")
	Long countByTestId(@Param("testId") Long testId);

	@Query("SELECT COUNT(q) FROM Questions q WHERE q.test.id = :testId AND q.subject.id = :subjectId")
	Long countByTestIdAndSubjectId(@Param("testId") Long testId, @Param("subjectId") Long subjectId);

	// to find multiple test by IDs and test language
	@Query("SELECT q FROM Questions q " + "JOIN q.test t " + "WHERE t.id = :testId "
			+ "AND :language MEMBER OF t.language")
	List<Questions> findByTestIdAndTestLanguage(@Param("testId") Long testId, @Param("language") Language language);

	 @Query("SELECT q FROM Questions q " +
	           "LEFT JOIN FETCH q.options " +
	           "WHERE q.test.id = :testId " +
	           "AND q.language = :language " +
	           "ORDER BY q.questionNumber ASC")
	    List<Questions> findByTestIdAndQuestionLanguage(
	            @Param("testId") Long testId, 
	            @Param("language") Language language);
	    
	    // Alternative with all relationships fetched
	    @Query("SELECT DISTINCT q FROM Questions q " +
	           "LEFT JOIN FETCH q.options " +
	           "LEFT JOIN FETCH q.subject " +
	           "LEFT JOIN FETCH q.chapter " +
	           "WHERE q.test.id = :testId " +
	           "AND q.language = :language " +
	           "ORDER BY q.questionNumber ASC")
	    List<Questions> findByTestIdAndQuestionLanguageWithDetails(
	            @Param("testId") Long testId, 
	            @Param("language") Language language);

	    List<Questions> findByTestIdAndLanguage(Long testId, Language language);
	    
}
