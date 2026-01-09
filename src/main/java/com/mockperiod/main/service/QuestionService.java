package com.mockperiod.main.service;

import com.mockperiod.main.dto.QuestionDto;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface QuestionService {
	QuestionDto createQuestion(QuestionDto questionDto, MultipartFile file);

	QuestionDto createQuestionWithText(QuestionDto questionDto);

	QuestionDto createQuestionWithFile(QuestionDto questionDto, MultipartFile file);

	QuestionDto getQuestionById(Long id);

	List<QuestionDto> getAllQuestions();

	QuestionDto updateQuestion(Long id, QuestionDto questionDto, MultipartFile file);

	void deleteQuestion(Long id);

	List<QuestionDto> getQuestionsByTestId(Long testId);

	List<QuestionDto> getQuestionsBySubjectId(Long subjectId);

	List<QuestionDto> getQuestionsByChapterId(Long chapterId);
}