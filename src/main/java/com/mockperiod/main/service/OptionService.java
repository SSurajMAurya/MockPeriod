package com.mockperiod.main.service;

import com.mockperiod.main.dto.OptionDto;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface OptionService {
//	OptionDto createOption(OptionDto optionDto, MultipartFile file);
//
//	OptionDto createOptionWithText(OptionDto optionDto);
//
//	OptionDto createOptionWithFile(OptionDto optionDto);
//
//	OptionDto createOptionWithFile(OptionDto optionDto, MultipartFile file);
//
//	OptionDto getOptionById(Long id);
//
//	List<OptionDto> getAllOptions();
//
//	OptionDto updateOption(Long id, OptionDto optionDto);
//
//	OptionDto updateOptionWithFile(Long id, OptionDto optionDto, MultipartFile file);
//
//	void deleteOption(Long id);
//
//	List<OptionDto> getOptionsByQuestionId(Long questionId);
//
//	List<OptionDto> getCorrectOptionsByQuestionId(Long questionId);
//
//	void deleteAllOptionsByQuestionId(Long questionId);

	OptionDto createOption(OptionDto optionDto, MultipartFile file);

	/**
	 * Create a text-only option
	 */
	OptionDto createOptionWithText(OptionDto optionDto);

	/**
	 * Create an option with image file
	 */
	OptionDto createOptionWithImage(OptionDto optionDto, MultipartFile file);

	/**
	 * Get option by ID
	 */
	OptionDto getOptionById(Long id);

	/**
	 * Get all options
	 */
	List<OptionDto> getAllOptions();

	/**
	 * Update an option
	 */
	OptionDto updateOption(Long id, OptionDto optionDto, MultipartFile file);

	/**
	 * Delete an option
	 */
	void deleteOption(Long id);

	/**
	 * Get all options for a specific question
	 */
	List<OptionDto> getOptionsByQuestionId(Long questionId);

	/**
	 * Get correct options for a specific question
	 */
	List<OptionDto> getCorrectOptionsByQuestionId(Long questionId);

	/**
	 * Delete all options for a specific question
	 */
	void deleteAllOptionsByQuestionId(Long questionId);
}