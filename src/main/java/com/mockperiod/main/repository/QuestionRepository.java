package com.mockperiod.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.Questions;


public interface QuestionRepository extends JpaRepository<Questions, Long> {

}
