package com.mockperiod.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.Exam;
import com.mockperiod.main.entities.Subjects;

public interface SubjectRepository extends JpaRepository<Subjects, Long>{
	
	Optional<Subjects> findByName(String name);
	
	Subjects findByNameAndExamId(String name, Long examId);
	 Optional<Subjects> findByNameAndIdNot(String name, Long id);
	
	List<Subjects> findByExam(Exam exam);
	 
//	  @Query("SELECT s FROM Subjects s JOIN s.exams e WHERE e = :exam")
//	    List<Subjects> findByExam(@Param("exam") Exam exam);
	
	boolean existsByNameAndExam(String name, Exam exam);
	
	
	Optional<Subjects> findByNameAndExam(String name, Exam exam);
	
	  @Query("SELECT s FROM Subjects s WHERE SIZE(s.chapters) > 0")
	    List<Subjects> findAllSubjectsWithChapters();

}
