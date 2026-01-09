package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.SubjectDto;
import com.mockperiod.main.dto.SubjectFEDto;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDto subjectDto) {
        try {
            SubjectDto createdSubject = subjectService.createSubject(subjectDto);
            return new ResponseEntity<>(createdSubject, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error creating subject: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id) {
        try {
            SubjectDto subjectDto = subjectService.getSubjectById(id);
            return ResponseEntity.ok(subjectDto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Subject not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving subject: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSubjects() {
        try {
            List<SubjectDto> subjects = subjectService.getAllSubjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving subjects: " + e.getMessage()));
        }
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<?> getSubjectsByExamId(@PathVariable Long examId) {
        try {
            List<SubjectDto> subjects = subjectService.getSubjectsByExamId(examId);
            return ResponseEntity.ok(subjects);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Exam not found with id: " + examId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving subjects by exam: " + e.getMessage()));
        }
    }

    
    @GetMapping("/allSubjects")
    public ResponseEntity<List<SubjectFEDto>> getSubjects() {
        try {
            List<SubjectFEDto> subjects = subjectService.getAllSubjectByChapter();
            return ResponseEntity.ok(subjects);
        }  catch (Exception e) {
           throw new CustomException("Error fetching subjects", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(
            @PathVariable Long id, 
            @Valid @RequestBody SubjectDto subjectDto) {
        try {
            SubjectDto updatedSubject = subjectService.updateSubject(id, subjectDto);
            return ResponseEntity.ok(updatedSubject);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Subject not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error updating subject: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        try {
            subjectService.deleteSubject(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Subject not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error deleting subject: " + e.getMessage()));
        }
    }
    
    
//    @PutMapping("/{subjectId}/{examId}")
//    public ResponseEntity<?> addSubjectToExam(@PathVariable Long subjectId, @PathVariable Long examId) {
//        try {
//            SubjectDto response = subjectService.addSubjectToExam(subjectId, examId);
//            return ResponseEntity.ok().body(response);
//        } catch (ResourceNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ErrorResponse("Resource not found: " + e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Error adding subject to exam: " + e.getMessage()));
//        }
//    }
    

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