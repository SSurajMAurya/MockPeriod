package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.CloudinaryUploadResponse;
import com.mockperiod.main.dto.OptionDto;
import com.mockperiod.main.entities.Options;
import com.mockperiod.main.entities.Questions;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.OptionRepository;
import com.mockperiod.main.repository.QuestionRepository;
import com.mockperiod.main.service.OptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public OptionDto createOption(OptionDto optionDto, MultipartFile file) {
        log.info("Creating option for question ID: {}", optionDto.getQuestionId());
        
        validateOptionInput(optionDto, file);
        
        Questions question = getQuestionById(optionDto.getQuestionId());
        Options option = buildOptionEntity(optionDto, question);
        
        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            validateFile(file);
            String imageUrl = uploadOptionImage(file);
            option.setOptionImageUrl(imageUrl);
            option.setOptionText(null); // Clear text when image is provided
        }
        
        Options savedOption = optionRepository.save(option);
        log.info("Option created successfully with ID: {}", savedOption.getId());
        
        return mapToDto(savedOption);
    }

    @Override
    @Transactional
    public OptionDto createOptionWithText(OptionDto optionDto) {
        log.info("Creating text-only option for question ID: {}", optionDto.getQuestionId());
        
        if (optionDto.getOptionText() == null || optionDto.getOptionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Option text must be provided for text-only options");
        }
        
        Questions question = getQuestionById(optionDto.getQuestionId());
        Options option = buildOptionEntity(optionDto, question);
        option.setOptionImageUrl(null); // Ensure no image URL for text-only option
        
        Options savedOption = optionRepository.save(option);
        return mapToDto(savedOption);
    }

    @Transactional
    public OptionDto createOptionWithImage(OptionDto optionDto, MultipartFile file) {
        log.info("Creating image option for question ID: {}", optionDto.getQuestionId());
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must be provided for image options");
        }
        
        validateFile(file);
        Questions question = getQuestionById(optionDto.getQuestionId());
        Options option = buildOptionEntity(optionDto, question);
        
        String imageUrl = uploadOptionImage(file);
        option.setOptionImageUrl(imageUrl);
        option.setOptionText(null); // Clear text for image options
        
        Options savedOption = optionRepository.save(option);
        return mapToDto(savedOption);
    }

  
    @Transactional
    public OptionDto updateOption(Long id, OptionDto optionDto, MultipartFile file) {
        log.info("Updating option with ID: {}", id);
        
        Options existingOption = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + id));

        // Handle file update/removal
        if (file != null && !file.isEmpty()) {
            // Delete old image if exists
            deleteOptionImage(existingOption.getOptionImageUrl());
            
            // Upload new image
            validateFile(file);
            String newImageUrl = uploadOptionImage(file);
            existingOption.setOptionImageUrl(newImageUrl);
            existingOption.setOptionText(null); // Clear text when image is provided
        } else if (optionDto.getOptionText() != null && !optionDto.getOptionText().trim().isEmpty()) {
            // If text is provided and no new file, remove existing image
            deleteOptionImage(existingOption.getOptionImageUrl());
            existingOption.setOptionImageUrl(null);
            existingOption.setOptionText(optionDto.getOptionText().trim());
        }

        // Update other fields
        if (optionDto.getOptionNumber() != null) {
            existingOption.setOptionNumber(optionDto.getOptionNumber());
        }
        if (optionDto.getIsCorrect() != null) {
            existingOption.setIsCorrect(optionDto.getIsCorrect());
        }

        Options updatedOption = optionRepository.save(existingOption);
        return mapToDto(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOption(Long id) {
        log.info("Deleting option with ID: {}", id);
        
        Options option = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + id));
        
        // Delete image from Cloudinary if exists
        deleteOptionImage(option.getOptionImageUrl());
        
        optionRepository.delete(option);
        log.info("Option deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OptionDto getOptionById(Long id) {
        return optionRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionDto> getAllOptions() {
        return optionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionDto> getOptionsByQuestionId(Long questionId) {
        Questions question = getQuestionById(questionId);
        return optionRepository.findByQuestion(question)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionDto> getCorrectOptionsByQuestionId(Long questionId) {
        Questions question = getQuestionById(questionId);
        return optionRepository.findByQuestionAndIsCorrect(question, true)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllOptionsByQuestionId(Long questionId) {
        log.info("Deleting all options for question ID: {}", questionId);
        
        Questions question = getQuestionById(questionId);
        List<Options> options = optionRepository.findByQuestion(question);
        
        // Delete all images from Cloudinary
        options.forEach(option -> deleteOptionImage(option.getOptionImageUrl()));
        
        optionRepository.deleteAll(options);
        log.info("Deleted {} options for question ID: {}", options.size(), questionId);
    }

    // Helper methods
    private void validateOptionInput(OptionDto optionDto, MultipartFile file) {
        boolean hasText = optionDto.getOptionText() != null && !optionDto.getOptionText().trim().isEmpty();
        boolean hasFile = file != null && !file.isEmpty();
        
        if (!hasText && !hasFile) {
            throw new IllegalArgumentException("Either option text or file must be provided");
        }
        
        if (optionDto.getQuestionId() == null) {
            throw new IllegalArgumentException("Question ID must be provided");
        }
    }

    private void validateFile(MultipartFile file) {
        if (!cloudinaryService.isValidFileSize(file)) {
            throw new IllegalArgumentException("File size exceeds maximum limit (10MB)");
        }
        // Add file type validation if needed
    }

    private Questions getQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
    }

    private String uploadOptionImage(MultipartFile file) {
        try {
            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadFile(file, "options");
            return uploadResponse.getSecureUrl();
        } catch (Exception e) {
            log.error("Failed to upload option image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload option image: " + e.getMessage(), e);
        }
    }

    private void deleteOptionImage(String imageUrl) {
        if (imageUrl != null) {
            try {
                // Extract public ID from URL and delete
                // You need to implement this in CloudinaryService
                cloudinaryService.deleteFile(imageUrl);
            } catch (Exception e) {
                log.warn("Failed to delete option image from Cloudinary: {}", e.getMessage());
                // Don't throw exception, just log warning
            }
        } 
    }

    private Options buildOptionEntity(OptionDto optionDto, Questions question) {
        return Options.builder()
                .optionText(optionDto.getOptionText() != null ? optionDto.getOptionText().trim() : null)
                .question(question)
                .optionNumber(optionDto.getOptionNumber())
                .isCorrect(optionDto.getIsCorrect() != null ? optionDto.getIsCorrect() : false)
                .build();
    }

    private OptionDto mapToDto(Options option) {
        return OptionDto.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .optionImageUrl(option.getOptionImageUrl())
                .questionId(option.getQuestion() != null ? option.getQuestion().getId() : null)
                .optionNumber(option.getOptionNumber())
                .isCorrect(option.getIsCorrect())
                .build();
    }
}