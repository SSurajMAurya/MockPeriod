package com.mockperiod.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.Tests;

public interface TestRepository extends JpaRepository<Tests, Long> {

}
