package com.mockperiod.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.Options;
import com.mockperiod.main.entities.Questions;

public interface OptionRepository extends JpaRepository<Options, Long> {
	
//	List<Options> findByQuestion(Questions questions);
	
	List<Options> findByQuestion(Questions question);
    List<Options> findByQuestionAndIsCorrect(Questions question, Boolean isCorrect);
    int deleteByQuestion(Questions question);
    
    @Query("SELECT o FROM Options o WHERE o.question.id IN :questionIds")
    List<Options> findByQuestionIds(@Param("questionIds") List<Long> questionIds);

}
