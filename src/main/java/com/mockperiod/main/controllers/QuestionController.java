package com.mockperiod.main.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockperiod.main.dto.QuestionDto;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;

    // Create question with optional file
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createQuestion(
            @RequestPart("questionDto") @Valid String questionDtoJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            // Manually parse the JSON
            ObjectMapper objectMapper = new ObjectMapper();
            QuestionDto questionDto = objectMapper.readValue(questionDtoJson, QuestionDto.class);
            
            log.info("Creating question - Text: {}, TestId: {}, File: {}", 
                questionDto.getQuestionText(), 
                questionDto.getTestId(),
                file != null ? file.getOriginalFilename() : "null");
            
            QuestionDto createdQuestion = questionService.createQuestion(questionDto, file);
            return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid JSON format: " + e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Resource not found: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating question: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating question: " + e.getMessage()));
        }
    }

    // Create question with text only
    @PostMapping("/text")
    public ResponseEntity<?> createQuestionWithText(@Valid @RequestBody QuestionDto questionDto) {
        try {
            QuestionDto createdQuestion = questionService.createQuestionWithText(questionDto);
            return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Resource not found: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating question: " + e.getMessage()));
        }
    }

    // Create question with file only
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createQuestionWithFile(
            @RequestPart @Valid QuestionDto questionDto,
            @RequestPart MultipartFile file) {
        try {
            QuestionDto createdQuestion = questionService.createQuestionWithFile(questionDto, file);
            return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Resource not found: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating question with file: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        try {
            QuestionDto questionDto = questionService.getQuestionById(id);
            return ResponseEntity.ok(questionDto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Question not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving question: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        try {
            List<QuestionDto> questions = questionService.getAllQuestions();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving questions: " + e.getMessage()));
        }
    }

    // Update question with optional file
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id,
            @RequestPart @Valid QuestionDto questionDto,
            @RequestPart(required = false) MultipartFile file) {
        try {
            QuestionDto updatedQuestion = questionService.updateQuestion(id, questionDto, file);
            return ResponseEntity.ok(updatedQuestion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Question not found with id: " + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error updating question: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Question not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error deleting question: " + e.getMessage()));
        }
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<?> getQuestionsByTestId(@PathVariable Long testId) {
        try {
            List<QuestionDto> questions = questionService.getQuestionsByTestId(testId);
            return ResponseEntity.ok(questions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Test not found with id: " + testId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving questions by test: " + e.getMessage()));
        }
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<?> getQuestionsBySubjectId(@PathVariable Long subjectId) {
        try {
            List<QuestionDto> questions = questionService.getQuestionsBySubjectId(subjectId);
            return ResponseEntity.ok(questions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Subject not found with id: " + subjectId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving questions by subject: " + e.getMessage()));
        }
    }

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<?> getQuestionsByChapterId(@PathVariable Long chapterId) {
        try {
            List<QuestionDto> questions = questionService.getQuestionsByChapterId(chapterId);
            return ResponseEntity.ok(questions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Chapter not found with id: " + chapterId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving questions by chapter: " + e.getMessage()));
        }
    }

    // Error Response DTO
    public static class ErrorResponse {
        private String message;
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}