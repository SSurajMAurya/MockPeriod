//package com.mockperiod.main.controllers;
//
//import com.mockperiod.main.dto.ExamDto;
//
//import com.mockperiod.main.service.ExamService;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/exams")
//@RequiredArgsConstructor
//public class ExamController {
//
//	private final ExamService examService;
//
//	@PostMapping
//	public ResponseEntity<ExamDto> createExam(@Valid @RequestBody ExamDto examDto) {
//		ExamDto createdExam = examService.createExam(examDto);
//		return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
//	}
//
//	@GetMapping("/{id}")
//	public ResponseEntity<ExamDto> getExamById(@PathVariable Long id) {
//		ExamDto examDto = examService.getExamById(id);
//		return ResponseEntity.ok(examDto);
//	}
//
//	@GetMapping 
//	public ResponseEntity<List<ExamDto>> getAllExams() {
//		List<ExamDto> exams = examService.getAllExams();
//		return ResponseEntity.ok(exams);
//	}
//
//	@PutMapping("/{id}")
//	public ResponseEntity<ExamDto> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDto examDto) {
//		ExamDto updatedExam = examService.updateExam(id, examDto);
//		return ResponseEntity.ok(updatedExam);
//	}
//
////    @PatchMapping("/{id}")
////    public ResponseEntity<ExamDto> updateExam(
////            @PathVariable Long id, 
////            @Valid @RequestBody ExamDto examDto) {
////        ExamDto updatedExam = examService.updateExam(id, examDto);
////        return ResponseEntity.ok(updatedExam);
////    }
//
//	@DeleteMapping("/{id}")
//	public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
//		examService.deleteExam(id);
//		return ResponseEntity.noContent().build();
//	}
//
//	// Add subjects to exam
//	@PostMapping("/{examId}/subjects")
//	public ResponseEntity<ExamDto> addSubjectsToExam(@PathVariable Long examId, @RequestBody List<Long> subjectIds) {
//		ExamDto updatedExam = examService.addSubjectsToExam(examId, subjectIds);
//		return ResponseEntity.ok(updatedExam);
//	}
//
//	// Remove subjects from exam
//	@DeleteMapping("/{examId}/subjects")
//	public ResponseEntity<ExamDto> removeSubjectsFromExam(@PathVariable Long examId,
//			@RequestBody List<Long> subjectIds) {
//		ExamDto updatedExam = examService.removeSubjectsFromExam(examId, subjectIds);
//		return ResponseEntity.ok(updatedExam);
//	}
//}



package com.mockperiod.main.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockperiod.main.dto.ExamDto;
import com.mockperiod.main.dto.ExamForFEDto;
import com.mockperiod.main.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamDto> createExam(
            @RequestPart("examData") String examDataJson,
            @RequestPart(value = "examImage", required = false) MultipartFile examImage) {
        
        try {
            log.info("Creating exam with image: {}", examImage != null ? examImage.getOriginalFilename() : "null");
            
            // Convert JSON string to ExamDto
            ExamDto examDto = objectMapper.readValue(examDataJson, ExamDto.class);
            
            // Set the image file to DTO if provided
            if (examImage != null && !examImage.isEmpty()) {
                examDto.setExamImage(examImage);
            }
            
            ExamDto createdExam = examService.createExam(examDto);
            return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
            
        } catch (Exception e) {
            log.error("Error creating exam: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing exam data", e);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamDto> updateExam(
            @PathVariable Long id,
            @RequestPart("examData") String examDataJson,
            @RequestPart(value = "examImage", required = false) MultipartFile examImage) {
        
        try {
            log.info("Updating exam with ID: {}", id);
            
            // Convert JSON string to ExamDto
            ExamDto examDto = objectMapper.readValue(examDataJson, ExamDto.class);
            
            // Set the image file to DTO if provided
            if (examImage != null && !examImage.isEmpty()) {
                examDto.setExamImage(examImage);
            }
            
            ExamDto updatedExam = examService.updateExam(id, examDto);
            return ResponseEntity.ok(updatedExam);
            
        } catch (Exception e) {
            log.error("Error updating exam: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing exam data", e);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ExamDto> getExamById(@PathVariable Long id) {
        log.info("Fetching exam with ID: {}", id);
        ExamDto examDto = examService.getExamById(id);
        return ResponseEntity.ok(examDto);
    }

    @GetMapping 
    public ResponseEntity<List<ExamDto>> getAllExams() {
        log.info("Fetching all exams");
        List<ExamDto> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

//    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ExamDto> updateExam(
//            @PathVariable Long id,
//            @Valid @RequestPart("examData") ExamDto examDto,
//            @RequestPart(value = "examImage", required = false) MultipartFile examImage) {
//        
//        log.info("Updating exam with ID: {}", id);
//        
//        // Set the image file to DTO if provided
//        if (examImage != null && !examImage.isEmpty()) {
//            examDto.setExamImage(examImage);
//        }
//        
//        ExamDto updatedExam = examService.updateExam(id, examDto);
//        return ResponseEntity.ok(updatedExam);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        log.info("Deleting exam with ID: {}", id);
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    // Add subjects to exam
    @PostMapping("/{examId}/subjects")
    public ResponseEntity<ExamDto> addSubjectsToExam(@PathVariable Long examId, @RequestBody List<Long> subjectIds) {
        log.info("Adding subjects to exam ID: {}", examId);
        ExamDto updatedExam = examService.addSubjectsToExam(examId, subjectIds);
        return ResponseEntity.ok(updatedExam);
    }

    // Remove subjects from exam
    @DeleteMapping("/{examId}/subjects")
    public ResponseEntity<ExamDto> removeSubjectsFromExam(@PathVariable Long examId,
            @RequestBody List<Long> subjectIds) {
        log.info("Removing subjects from exam ID: {}", examId);
        ExamDto updatedExam = examService.removeSubjectsFromExam(examId, subjectIds);
        return ResponseEntity.ok(updatedExam);
    }

    // Update exam image only
    @PutMapping(value = "/{examId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamDto> updateExamImage(
            @PathVariable Long examId,
            @RequestPart("examImage") MultipartFile examImage) {
        
        log.info("Updating image for exam ID: {}", examId);
        ExamDto updatedExam = examService.updateExamImage(examId, examImage);
        return ResponseEntity.ok(updatedExam);
    }

    // Delete exam image only
    @DeleteMapping("/{examId}/image")
    public ResponseEntity<Void> deleteExamImage(@PathVariable Long examId) {
        log.info("Deleting image for exam ID: {}", examId);
        examService.deleteExamImage(examId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/allExam")
    public ResponseEntity<List<ExamForFEDto>> getAllExam() {
        log.info("fetching exam list");
         List<ExamForFEDto> examList =  examService.getAllExamFE();
        return ResponseEntity.ok(examList);
    }
    
    
    
    
}