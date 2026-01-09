package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.OptionDto;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.service.OptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/options")
@RequiredArgsConstructor
@Validated
public class OptionController {

    private final OptionService optionService;

    // Text-only option creation
    @PostMapping("/text")
    public ResponseEntity<?> createTextOption(@Valid @RequestBody OptionDto optionDto) {
        try {
            log.info("Creating text-only option for question ID: {}", optionDto.getQuestionId());
            OptionDto createdOption = optionService.createOptionWithText(optionDto);
            log.info("Text option created successfully with ID: {}", createdOption.getId());
            return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while creating text option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while creating text option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while creating text option: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating option: " + e.getMessage());
        }
    }

    // Image option creation with file upload
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createImageOption(
            @RequestPart("optionDto") @Valid OptionDto optionDto,
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("Creating image option for question ID: {}", optionDto.getQuestionId());
            OptionDto createdOption = optionService.createOptionWithImage(optionDto, file);
            log.info("Image option created successfully with ID: {}", createdOption.getId());
            return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while creating image option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while creating image option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while creating image option: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating option: " + e.getMessage());
        }
    }

    // Generic option creation that handles both text and image
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOption(
            @RequestPart("optionDto") @Valid OptionDto optionDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Creating option for question ID: {}", optionDto.getQuestionId());
            OptionDto createdOption = optionService.createOption(optionDto, file);
            log.info("Option created successfully with ID: {}", createdOption.getId());
            return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while creating option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while creating option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while creating option: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating option: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOptionById(@PathVariable Long id) {
        try {
            log.info("Fetching option with ID: {}", id);
            OptionDto optionDto = optionService.getOptionById(id);
            return ResponseEntity.ok(optionDto);
        } catch (ResourceNotFoundException e) {
            log.error("Option not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: Option not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error retrieving option with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving option: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOptions() {
        try {
            log.info("Fetching all options");
            List<OptionDto> options = optionService.getAllOptions();
            log.info("Retrieved {} options", options.size());
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            log.error("Error retrieving all options: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving options: " + e.getMessage());
        }
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            log.info("Fetching options for question ID: {}", questionId);
            List<OptionDto> options = optionService.getOptionsByQuestionId(questionId);
            log.info("Retrieved {} options for question ID: {}", options.size(), questionId);
            return ResponseEntity.ok(options);
        } catch (ResourceNotFoundException e) {
            log.error("Question not found with ID: {}", questionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving options for question ID {}: {}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving options: " + e.getMessage());
        }
    }

    // Update option with potential file upload
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateOption(
            @PathVariable Long id,
            @RequestPart("optionDto") @Valid OptionDto optionDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Updating option with ID: {}", id);
            OptionDto updatedOption = optionService.updateOption(id, optionDto, file);
            log.info("Option updated successfully with ID: {}", id);
            return ResponseEntity.ok(updatedOption);
        } catch (ResourceNotFoundException e) {
            log.error("Option not found for update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while updating option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating option with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating option: " + e.getMessage());
        }
    }

    // Text-only update (without file)
    @PutMapping("/{id}/text")
    public ResponseEntity<?> updateOptionText(
            @PathVariable Long id,
            @Valid @RequestBody OptionDto optionDto) {
        try {
            log.info("Updating option text with ID: {}", id);
            // Pass null for file to indicate text-only update
            OptionDto updatedOption = optionService.updateOption(id, optionDto, null);
            log.info("Option text updated successfully with ID: {}", id);
            return ResponseEntity.ok(updatedOption);
        } catch (ResourceNotFoundException e) {
            log.error("Option not found for text update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while updating option text: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating option text with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating option: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOption(@PathVariable Long id) {
        try {
            log.info("Deleting option with ID: {}", id);
            optionService.deleteOption(id);
            log.info("Option deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Option not found for deletion with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting option with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting option: " + e.getMessage());
        }
    }

    @GetMapping("/question/{questionId}/correct")
    public ResponseEntity<?> getCorrectOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            log.info("Fetching correct options for question ID: {}", questionId);
            List<OptionDto> options = optionService.getCorrectOptionsByQuestionId(questionId);
            log.info("Retrieved {} correct options for question ID: {}", options.size(), questionId);
            return ResponseEntity.ok(options);
        } catch (ResourceNotFoundException e) {
            log.error("Question not found with ID: {}", questionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving correct options for question ID {}: {}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving correct options: " + e.getMessage());
        }
    }

    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<?> deleteAllOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            log.info("Deleting all options for question ID: {}", questionId);
            optionService.deleteAllOptionsByQuestionId(questionId);
            log.info("All options deleted successfully for question ID: {}", questionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Question not found for bulk deletion with ID: {}", questionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting all options for question ID {}: {}", questionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting options: " + e.getMessage());
        }
    }
}