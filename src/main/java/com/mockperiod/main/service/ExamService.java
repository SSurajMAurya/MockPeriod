package com.mockperiod.main.service;

import com.mockperiod.main.dto.ExamDto;
import com.mockperiod.main.dto.ExamForFEDto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ExamService {
	ExamDto createExam(ExamDto examDto);

	ExamDto getExamById(Long id);

	List<ExamDto> getAllExams();

	ExamDto updateExam(Long id, ExamDto examDto);

	void deleteExam(Long id);

	ExamDto addSubjectsToExam(Long examId, List<Long> subjectIds);

	ExamDto removeSubjectsFromExam(Long examId, List<Long> subjectIds);

	void deleteExamImage(Long examId);
	
	ExamDto updateExamImage(Long examId, MultipartFile imageFile);
	
	List<ExamForFEDto> getAllExamFE();

}