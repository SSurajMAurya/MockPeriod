package com.mockperiod.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

//import org.hibernate.query.Page;

import com.mockperiod.main.dto.CreateQuestionRequest;
import com.mockperiod.main.dto.QuestionFilter;
import com.mockperiod.main.dto.QuestionResponseDTO;
import com.mockperiod.main.dto.UpdateQuestionRequest;

public interface QuestionService {
    
    QuestionResponseDTO createQuestion(CreateQuestionRequest request);
    
    QuestionResponseDTO getQuestionById(Long questionId);
    
    Page<QuestionResponseDTO> getQuestionsByFilter(QuestionFilter filter, Pageable pageable);
    
    QuestionResponseDTO updateQuestion(Long questionId, UpdateQuestionRequest request);
    
    void deleteQuestion(Long questionId);
    
    QuestionResponseDTO uploadQuestionImage(Long questionId, MultipartFile image);
    
    QuestionResponseDTO uploadOptionImage(Long questionId, Integer optionNumber, MultipartFile image);
    
    ValidationResult validateQuestion(CreateQuestionRequest request);
}