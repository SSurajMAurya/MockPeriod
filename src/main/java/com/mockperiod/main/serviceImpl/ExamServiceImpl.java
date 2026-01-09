
package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.ExamDto;
import com.mockperiod.main.dto.ExamForFEDto;
import com.mockperiod.main.dto.CloudinaryUploadResponse;
import com.mockperiod.main.entities.Exam;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.ExamRepository;
import com.mockperiod.main.repository.SubjectRepository;
import com.mockperiod.main.service.ExamService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ExamDto createExam(ExamDto examDto) {
        try {
            log.info("Creating exam with name: {}", examDto.getExamName());

            // Check if exam already exists with the same name
            if (examRepository.findByExamName(examDto.getExamName()).isPresent()) {
                throw new CustomException("Exam already exists with exam Name " + examDto.getExamName(), HttpStatus.CONFLICT);
            }

            Exam exam = new Exam();
            exam.setExamName(examDto.getExamName());
            exam.setDescription(examDto.getDescription());

            // Handle image upload if present
            if (examDto.getExamImage() != null && !examDto.getExamImage().isEmpty()) {
                String imageUrl = uploadExamImage(examDto.getExamImage());
                exam.setExamImageUrl(imageUrl);
            }

            // Save the exam first
            Exam savedExam = examRepository.save(exam);
            log.info("Exam saved with ID: {}", savedExam.getId());

            // Then handle subjects relationship if subjectIds are provided
            if (examDto.getSubjectIds() != null && !examDto.getSubjectIds().isEmpty()) {
                log.info("Processing subject IDs: {}", examDto.getSubjectIds());

                List<Subjects> subjects = subjectRepository.findAllById(examDto.getSubjectIds());
                log.info("Found subjects: {}", subjects.size());

                // Check if all subjects were found
                if (subjects.size() != examDto.getSubjectIds().size()) {
                    List<Long> foundIds = subjects.stream().map(Subjects::getId).collect(Collectors.toList());
                    List<Long> missingIds = examDto.getSubjectIds().stream()
                            .filter(id -> !foundIds.contains(id))
                            .collect(Collectors.toList());
                    throw new ResourceNotFoundException("Subjects not found with IDs: " + missingIds);
                }

                // Set subjects to exam
                savedExam.setSubjects(subjects);
                savedExam = examRepository.save(savedExam);
            }

            return mapToDto(savedExam);

        } catch (CustomException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating exam: {}", e.getMessage(), e);
            throw new CustomException("Error creating exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ExamDto getExamById(Long id) {
        try {
            Exam exam = examRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
            return mapToDto(exam);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving exam: {}", e.getMessage(), e);
            throw new CustomException("Error retrieving exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ExamDto> getAllExams() {
        try {
            List<Exam> exams = examRepository.findAll();
            return exams.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving exams: {}", e.getMessage(), e);
            throw new CustomException("Error retrieving exams: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ExamDto updateExam(Long id, ExamDto examDto) {
        try {
            Exam existingExam = examRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

            log.info("Updating exam ID: {} with data: {}", id, examDto);

            // Update only non-null fields (partial update)
            if (examDto.getExamName() != null && !examDto.getExamName().trim().isEmpty()) {
                // Check if new name already exists (excluding current exam)
                if (examRepository.findByExamNameAndIdNot(examDto.getExamName(), id).isPresent()) {
                    throw new CustomException("Exam name already exists", HttpStatus.CONFLICT);
                }
                existingExam.setExamName(examDto.getExamName());
            }
            
            if (examDto.getDescription() != null) {
                existingExam.setDescription(examDto.getDescription());
            }

            // Handle image update if new image is provided
            if (examDto.getExamImage() != null && !examDto.getExamImage().isEmpty()) {
                // Delete old image if exists
                if (existingExam.getExamImageUrl() != null) {
                    cloudinaryService.deleteFile(existingExam.getExamImageUrl());
                }
                // Upload new image
                String newImageUrl = uploadExamImage(examDto.getExamImage());
                existingExam.setExamImageUrl(newImageUrl);
            }

            // Update subjects only if subjectIds is provided (not null)
            if (examDto.getSubjectIds() != null) {
                log.info("Updating subjects relationship: {}", examDto.getSubjectIds());

                if (!examDto.getSubjectIds().isEmpty()) {
                    List<Subjects> subjects = subjectRepository.findAllById(examDto.getSubjectIds());

                    // Check if all subjects were found
                    if (subjects.size() != examDto.getSubjectIds().size()) {
                        List<Long> foundIds = subjects.stream().map(Subjects::getId).collect(Collectors.toList());
                        List<Long> missingIds = examDto.getSubjectIds().stream()
                                .filter(subjectId -> !foundIds.contains(subjectId))
                                .collect(Collectors.toList());
                        throw new ResourceNotFoundException("Subjects not found with IDs: " + missingIds);
                    }

                    // Update subjects list
                    existingExam.setSubjects(subjects);
                } else {
                    // Clear subjects if empty list provided
                    existingExam.setSubjects(new ArrayList<>());
                }
            }

            Exam updatedExam = examRepository.save(existingExam);
            return mapToDto(updatedExam);

        } catch (CustomException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating exam: {}", e.getMessage(), e);
            throw new CustomException("Error updating exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        try {
            Exam exam = examRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

            // Delete image from Cloudinary if exists
            if (exam.getExamImageUrl() != null) {
                cloudinaryService.deleteFile(exam.getExamImageUrl());
            }

            examRepository.delete(exam);
            log.info("Exam deleted successfully with ID: {}", id);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting exam: {}", e.getMessage(), e);
            throw new CustomException("Error deleting exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ExamDto addSubjectsToExam(Long examId, List<Long> subjectIds) {
        try {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

            List<Subjects> subjectsToAdd = subjectRepository.findAllById(subjectIds);
            // Check if all subjects were found
            if (subjectsToAdd.size() != subjectIds.size()) {
                List<Long> foundIds = subjectsToAdd.stream().map(Subjects::getId).collect(Collectors.toList());
                List<Long> missingIds = subjectIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new ResourceNotFoundException("Subjects not found with IDs: " + missingIds);
            }

            // Get current subjects and add new ones (avoid duplicates)
            List<Subjects> currentSubjects = exam.getSubjects();
            for (Subjects subject : subjectsToAdd) {
                if (!currentSubjects.contains(subject)) {
                    currentSubjects.add(subject);
                }
            }

            exam.setSubjects(currentSubjects);
            Exam updatedExam = examRepository.save(exam);
            return mapToDto(updatedExam);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding subjects to exam: {}", e.getMessage(), e);
            throw new CustomException("Error adding subjects to exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ExamDto removeSubjectsFromExam(Long examId, List<Long> subjectIds) {
        try {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

            List<Subjects> subjectsToRemove = subjectRepository.findAllById(subjectIds);
            
            // Remove the subjects from exam
            List<Subjects> currentSubjects = exam.getSubjects();
            currentSubjects.removeAll(subjectsToRemove);
            exam.setSubjects(currentSubjects);
            
            Exam updatedExam = examRepository.save(exam);
            return mapToDto(updatedExam);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing subjects from exam: {}", e.getMessage(), e);
            throw new CustomException("Error removing subjects from exam: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ExamDto updateExamImage(Long examId, MultipartFile imageFile) {
        try {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

            // Validate file
            if (!cloudinaryService.isValidFileSize(imageFile)) {
                throw new CustomException("File size too large. Maximum size is 10MB", HttpStatus.BAD_REQUEST);
            }

            // Delete old image if exists
            if (exam.getExamImageUrl() != null) {
                cloudinaryService.deleteFile(exam.getExamImageUrl());
            }

            // Upload new image
            String imageUrl = uploadExamImage(imageFile);
            exam.setExamImageUrl(imageUrl);

            Exam updatedExam = examRepository.save(exam);
            return mapToDto(updatedExam);

        } catch (ResourceNotFoundException | CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating exam image: {}", e.getMessage(), e);
            throw new CustomException("Error updating exam image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteExamImage(Long examId) {
        try {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

            if (exam.getExamImageUrl() != null) {
                cloudinaryService.deleteFile(exam.getExamImageUrl());
                exam.setExamImageUrl(null);
                examRepository.save(exam);
                log.info("Exam image deleted successfully for exam ID: {}", examId);
            }

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting exam image: {}", e.getMessage(), e);
            throw new CustomException("Error deleting exam image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to upload exam image
    private String uploadExamImage(MultipartFile imageFile) {
        try {
            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadFile(imageFile, "exams");
            return uploadResponse.getSecureUrl() != null ? uploadResponse.getSecureUrl() : uploadResponse.getUrl();
        } catch (Exception e) {
            log.error("Failed to upload exam image: {}", e.getMessage(), e);
            throw new CustomException("Failed to upload exam image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to convert Entity to DTO
    private ExamDto mapToDto(Exam exam) {
        try {
            List<Long> subjectIds = exam.getSubjects() != null
                    ? exam.getSubjects().stream()
                            .map(Subjects::getId)
                            .collect(Collectors.toList())
                    : new ArrayList<>();
            
            List<String> subNames = exam.getSubjects() != null
                    ? exam.getSubjects().stream()
                            .map(Subjects::getName)

                            .collect(Collectors.toList())
                    : new ArrayList<>();

            log.debug("Mapping exam ID: {} with subject IDs: {}", exam.getId(), subjectIds);

            return ExamDto.builder()
                    .id(exam.getId())
                    .examName(exam.getExamName())
                    .description(exam.getDescription())
                    .subjectIds(subjectIds)
                    .examImageUrl(exam.getExamImageUrl())
                    .subjectNames(subNames)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping exam to DTO: {}", e.getMessage(), e);
            throw new CustomException("Error mapping exam to DTO: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Override
	public List<ExamForFEDto> getAllExamFE() {
		try {
			
		List<Exam> exams =	examRepository.findAll();
		
		return exams.stream().map((exam) ->{ 
			ExamForFEDto dto = new ExamForFEDto();
			dto.setId(exam.getId());
			dto.setExamName(exam.getExamName());
			dto.setExamImageUrl(exam.getExamImageUrl());
			
			return dto;
		}).collect(Collectors.toList());
		
		} catch (Exception e) {
			throw new RuntimeException("Error getting Exam list");
		}
	}
}