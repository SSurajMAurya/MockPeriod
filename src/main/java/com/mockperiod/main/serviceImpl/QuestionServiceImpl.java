package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.CloudinaryUploadResponse;
import com.mockperiod.main.dto.QuestionDto;
import com.mockperiod.main.entities.Questions;
import com.mockperiod.main.entities.Tests;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.entities.Chapter;
import com.mockperiod.main.repository.QuestionRepository;
import com.mockperiod.main.repository.TestRepository;
import com.mockperiod.main.repository.SubjectRepository;
import com.mockperiod.main.repository.ChapterRepository;
import com.mockperiod.main.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto, MultipartFile file) {
        log.info("Creating question for test ID: {}", questionDto.getTestId());
        
        validateQuestionInput(questionDto, file);
        
        if (file != null && !file.isEmpty()) {
            return createQuestionWithFile(questionDto, file);
        } else {
            return createQuestionWithText(questionDto);
        }
    }

    @Override
    @Transactional
    public QuestionDto createQuestionWithText(QuestionDto questionDto) {
        log.info("Creating text-only question for test ID: {}", questionDto.getTestId());
        
        if (questionDto.getQuestionText() == null || questionDto.getQuestionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Question text must be provided for text-only questions");
        }
        
        Questions question = buildQuestionEntity(questionDto);
        Questions savedQuestion = questionRepository.save(question);
        log.info("Text question created successfully with ID: {}", savedQuestion.getId());
        
        return mapToDto(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDto createQuestionWithFile(QuestionDto questionDto, MultipartFile file) {
        log.info("Creating image question for test ID: {}", questionDto.getTestId());
        
        validateFile(file);
        
        try {
            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadFile(file, "questions");
            
            Questions question = buildQuestionEntity(questionDto);
            question.setQuestionImageUrl(uploadResponse.getSecureUrl());
            
            Questions savedQuestion = questionRepository.save(question);
            log.info("Image question created successfully with ID: {}", savedQuestion.getId());
            
            return mapToDto(savedQuestion);
            
        } catch (Exception e) {
            log.error("Failed to create question with image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload question file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto, MultipartFile file) {
        log.info("Updating question with ID: {}", id);
        
        Questions existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        // Handle file update
        if (file != null && !file.isEmpty()) {
            validateFile(file);
            
            // Delete old image if exists
            deleteQuestionImage(existingQuestion.getQuestionImageUrl());
            
            // Upload new image
            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadFile(file, "questions");
            existingQuestion.setQuestionImageUrl(uploadResponse.getSecureUrl());
            existingQuestion.setQuestionText(null); // Clear text when image is provided
            
        } else if (questionDto.getQuestionText() != null && !questionDto.getQuestionText().trim().isEmpty()) {
            // If text is provided and no new file, remove existing image
            deleteQuestionImage(existingQuestion.getQuestionImageUrl());
            existingQuestion.setQuestionImageUrl(null);
            existingQuestion.setQuestionText(questionDto.getQuestionText().trim());
        }

        // Update other fields
        updateQuestionFields(existingQuestion, questionDto);
        existingQuestion.setUpdatedAt(LocalDateTime.now());

        Questions updatedQuestion = questionRepository.save(existingQuestion);
        log.info("Question updated successfully with ID: {}", id);
        
        return mapToDto(updatedQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(Long id) {
        return questionRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Deleting question with ID: {}", id);
        
        Questions question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        
        // Delete image from Cloudinary if exists
        deleteQuestionImage(question.getQuestionImageUrl());
        
        questionRepository.delete(question);
        log.info("Question deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsByTestId(Long testId) {
        Tests test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));
        
        return questionRepository.findByTest(test)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsBySubjectId(Long subjectId) {
        Subjects subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
        
        return questionRepository.findBySubject(subject)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsByChapterId(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
        
        return questionRepository.findByChapter(chapter)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateQuestionInput(QuestionDto questionDto, MultipartFile file) {
        boolean hasText = questionDto.getQuestionText() != null && !questionDto.getQuestionText().trim().isEmpty();
        boolean hasFile = file != null && !file.isEmpty();
        
        if (!hasText && !hasFile) {
            throw new IllegalArgumentException("Either question text or file must be provided");
        }
        
        if (questionDto.getTestId() == null) {
            throw new IllegalArgumentException("Test ID must be provided");
        }
    }

    private void validateFile(MultipartFile file) {
        if (!cloudinaryService.isValidFileSize(file)) {
            throw new IllegalArgumentException("File size exceeds maximum limit (10MB)");
        }
    }

    private Questions buildQuestionEntity(QuestionDto questionDto) {
        Tests test = testRepository.findById(questionDto.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + questionDto.getTestId()));

        Questions question = new Questions();
        question.setQuestionText(questionDto.getQuestionText());
        question.setTest(test);
        question.setQuestionNumber(questionDto.getQuestionNumber());
        question.setMarks(questionDto.getMarks());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        // Set subject if provided
        if (questionDto.getSubjectId() != null) {
            Subjects subject = subjectRepository.findById(questionDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + questionDto.getSubjectId()));
            question.setSubject(subject);
        }

        // Set chapter if provided
        if (questionDto.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(questionDto.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + questionDto.getChapterId()));
            question.setChapter(chapter);
        }

        return question;
    }

    private void updateQuestionFields(Questions question, QuestionDto questionDto) {
        if (questionDto.getQuestionNumber() != null) {
            question.setQuestionNumber(questionDto.getQuestionNumber());
        }
        if (questionDto.getMarks() != null) {
            question.setMarks(questionDto.getMarks());
        }

        // Update test if provided
        if (questionDto.getTestId() != null) {
            Tests test = testRepository.findById(questionDto.getTestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + questionDto.getTestId()));
            question.setTest(test);
        }

        // Update subject if provided
        if (questionDto.getSubjectId() != null) {
            Subjects subject = subjectRepository.findById(questionDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + questionDto.getSubjectId()));
            question.setSubject(subject);
        }

        // Update chapter if provided
        if (questionDto.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(questionDto.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + questionDto.getChapterId()));
            question.setChapter(chapter);
        }
    }

    private void deleteQuestionImage(String imageUrl) {
        if (imageUrl != null) {
            try {
                cloudinaryService.deleteFile(imageUrl);
            } catch (Exception e) {
                log.warn("Failed to delete question image from Cloudinary: {}", e.getMessage());
            }
        }
    }

    private QuestionDto mapToDto(Questions question) {
        return QuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionImageUrl(question.getQuestionImageUrl())
                .testId(question.getTest() != null ? question.getTest().getId() : null)
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .chapterId(question.getChapter() != null ? question.getChapter().getId() : null)
                .questionNumber(question.getQuestionNumber())
                .marks(question.getMarks())
                .build();
    }
}