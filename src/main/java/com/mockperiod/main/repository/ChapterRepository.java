package com.mockperiod.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

}
