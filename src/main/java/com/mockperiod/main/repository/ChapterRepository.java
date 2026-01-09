package com.mockperiod.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.Chapter;
import com.mockperiod.main.entities.Exam;
import com.mockperiod.main.entities.Subjects;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
	
	List<Chapter> findBySubject(Subjects subjects);
	
	Optional<Chapter> findByIdAndSubject(Long id ,Subjects subjects );

}
