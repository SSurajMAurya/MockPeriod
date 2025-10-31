package com.mockperiod.main.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mockperiod.main.dto.CreateQuestionRequest;
import com.mockperiod.main.dto.CreateTestRequest;

import com.mockperiod.main.dto.TestFilter;
import com.mockperiod.main.dto.TestResponseDto;
import com.mockperiod.main.dto.TestSummaryDTO;
import com.mockperiod.main.dto.UpdateTestDto;

public interface TestService {
	
	

		TestResponseDto createTest(CreateTestRequest request);

		TestResponseDto getTestById(Long testId);

		Page<TestResponseDto> getAllTests(TestFilter filter, Pageable pageable);

		TestResponseDto updateTest(Long testId, UpdateTestDto request);
		
		

		void deleteTest(Long testId);

		TestResponseDto addQuestionToTest(Long testId, CreateQuestionRequest request);

//		List<QuestionListDTO> getTestQuestions(Long testId);
		
//		List<com.mockperiod.main.dto.QuestionResponseDto>  getTestQuestions(Long testId);

		TestSummaryDTO getTestSummary(Long testId);
	}


