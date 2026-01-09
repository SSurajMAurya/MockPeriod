package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.SubjectDto;
import com.mockperiod.main.dto.SubjectFEDto;
import com.mockperiod.main.entities.Exam;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.ExamRepository;
import com.mockperiod.main.repository.SubjectRepository;
import com.mockperiod.main.service.SubjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;

    @Override
    @Transactional
    public SubjectDto createSubject(SubjectDto subjectDto) {
        try {
            // Check if subject already exists
            if (subjectRepository.findByName(subjectDto.getName()).isPresent()) {
                throw new CustomException("Subject already exists", HttpStatus.CONFLICT);
            }

            Subjects subject = Subjects.builder()
                    .name(subjectDto.getName())
                    .build();

            Subjects savedSubject = subjectRepository.save(subject);
            System.out.println("Subject saved with ID: " + savedSubject.getId());

            return mapToDto(savedSubject);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Error creating subject: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SubjectDto getSubjectById(Long id) {
        try {
            Subjects subject = subjectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
            return mapToDto(subject);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Error retrieving subject: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SubjectDto> getAllSubjects() {
        try {
            List<Subjects> subjects = subjectRepository.findAll();
            return subjects.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomException("Error retrieving subjects: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public SubjectDto updateSubject(Long id, SubjectDto subjectDto) {
        try {
            Subjects existingSubject = subjectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

            System.out.println("Updating subject ID: " + id + " with data: " + subjectDto);

            // Update only non-null fields (partial update)
            if (subjectDto.getName() != null && !subjectDto.getName().trim().isEmpty()) {
                // Check if new name already exists (excluding current subject)
                if (subjectRepository.findByNameAndIdNot(subjectDto.getName(), id).isPresent()) {
                    throw new CustomException("Subject name already exists", HttpStatus.CONFLICT);
                }
                existingSubject.setName(subjectDto.getName());
            }

            Subjects updatedSubject = subjectRepository.save(existingSubject);
            return mapToDto(updatedSubject);

        } catch (CustomException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Error updating subject: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        try {
            Subjects subject = subjectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

            subjectRepository.delete(subject);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Error deleting subject: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to convert Entity to DTO
    private SubjectDto mapToDto(Subjects subject) {
        try {
            return SubjectDto.builder()
                    .id(subject.getId())
                    .name(subject.getName())
                    .build();
        } catch (Exception e) {
            throw new CustomException("Error mapping subject to DTO: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Override
	public List<SubjectDto> getSubjectsByExamId(Long examId) {
		try {
           
		Exam exam =	examRepository.findById(examId).orElseThrow(() -> new CustomException("Exam not found", HttpStatus.NOT_FOUND));

		List<Subjects> subjects = subjectRepository.findByExam(exam);
		
		if(subjects == null) {
			throw new CustomException("No subject found for exam "+exam.getExamName() , HttpStatus.NOT_FOUND);
		}
		
		return subjects.stream().map((subject) ->{
			
			SubjectDto subjectDto = new SubjectDto();
			subjectDto.setName(subject.getName());
			subjectDto.setId(subject.getId());
			
			return subjectDto;
		}).collect(Collectors.toList());
		
        } catch (CustomException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Error updating subject: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@Override
	public List<SubjectFEDto> getAllSubjectByChapter() {
		try {
		List<Subjects> subjects =	subjectRepository.findAllSubjectsWithChapters();
		
	List<SubjectFEDto> dtos =	subjects.stream().map((subject) ->{
			SubjectFEDto dto = new SubjectFEDto();
			dto.setId(subject.getId());
			dto.setSubjectName(subject.getName());
			return dto;
		}).collect(Collectors.toList());
		
		return dtos;
		
		} catch (Exception e) {
			throw new CustomException("Error fetching the subjects" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
}