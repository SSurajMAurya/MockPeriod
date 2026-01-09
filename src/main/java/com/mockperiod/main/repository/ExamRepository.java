package com.mockperiod.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.mockperiod.main.entities.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {
	
	 Optional<Exam> findByExamName(String name);
	 
//	 Optional<Exam> findByExamName(String examName);
	 Optional<Exam> findByExamNameAndIdNot(String examName, Long id);

}
