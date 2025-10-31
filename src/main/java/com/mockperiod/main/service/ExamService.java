package com.mockperiod.main.service;

import java.util.List;

import com.mockperiod.main.dto.CreateExamRequest;
import com.mockperiod.main.dto.ExamResponseDTO;
import com.mockperiod.main.dto.UpdateExamRequest;

public interface ExamService {

	ExamResponseDTO createExam(CreateExamRequest request);

	List<ExamResponseDTO> getAllExams();

	ExamResponseDTO updateExam(Long examId, UpdateExamRequest request);

	void deleteExam(Long examId);

	List<ExamResponseDTO> searchExams(String keyword);

}
