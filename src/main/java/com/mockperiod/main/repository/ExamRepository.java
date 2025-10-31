package com.mockperiod.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.Domains;

public interface ExamRepository extends JpaRepository<Domains, Long> {

}
