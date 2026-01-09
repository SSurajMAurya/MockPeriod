package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.TestDto;
import com.mockperiod.main.dto.TestInstituteTimeDto;
import com.mockperiod.main.dto.TestStatusResponse;
import com.mockperiod.main.dto.QuestionDto;
import com.mockperiod.main.dto.StudentListWithTestDto;
import com.mockperiod.main.dto.CloudinaryUploadResponse;
import com.mockperiod.main.dto.NotificationDto;
import com.mockperiod.main.dto.OptionDto;
import com.mockperiod.main.entities.*;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.*;
import com.mockperiod.main.service.NotificationService;
import com.mockperiod.main.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

	private final UserRepository userRepository;
	private final TestRepository testRepository;
	private final ExamRepository examRepository;
	private final SubjectRepository subjectRepository;
	private final ChapterRepository chapterRepository;
	private final CloudinaryService cloudinaryService;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;
	private final WebSocketService webSocketService;
	private final TestInstituteTimeRepository testInstituteTimeRepository;
	private final StudentTestResultRepository testResultRepository;
	private final NotificationService notificationService;
	private final NotificationRepository notificationRepository;
//	private final StudentTestResultRepository testResultRepository2

	@Override
	@Transactional
	public TestDto createOrUpdateTest(TestDto testDto) {
		log.info("Starting createOrUpdateTest for test: {}", testDto.getTestName());

		List<Users> instituteList = null;

		if (testDto.getInstituteIds().get(0) == 0) {
			instituteList = userRepository.findByRole(Role.valueOf("ADMIN"));
		} else {
			instituteList = userRepository.findAllById(testDto.getInstituteIds());
		}

		// Validate institute list
		if (instituteList.isEmpty()) {
			throw new ResourceNotFoundException("No institute found with provided IDs: " + testDto.getInstituteIds());
		}
		log.info("Found {} institutes", instituteList.size());

		// Determine test type and validate accordingly
		ExamType examType = determineExamType(testDto);
		testDto.setExamType(examType.name());
		log.info("Test type determined as: {}", examType);

		Tests test;
		if (examType == ExamType.EXAM_WISE) {
			test = createOrUpdateExamWiseTest(testDto, instituteList);
		} else {
			test = createOrUpdateSubjectWiseTest(testDto, instituteList);
		}

		log.info("Test saved with ID: {}", test.getId());

		// Process single question upload if provided
		List<QuestionDto> allQuestions = new ArrayList<>();
		if (hasQuestionData(testDto)) {
			log.info("Processing question data for test ID: {}", test.getId());
			QuestionDto savedQuestionDto = processSingleQuestion(testDto, test);
			allQuestions.add(savedQuestionDto);
			log.info("Question processed and saved for test ID: {}", test.getId());
		} else {
			log.info("No question data provided for test ID: {}", test.getId());
			// Get existing questions if any
			allQuestions = getAllQuestionsForTest(test.getId());
		}

		// Send notification
		try {
			List<Long> instituteIds = instituteList.stream().map(Users::getId).collect(Collectors.toList());

			Notifications dto = new Notifications();
			dto.setUserIds(instituteIds);;
			dto.setMessage("New test has been created with test name --> "+ test.getTestName());
			dto.setType(NotificationType.TEST_CREATION);			
//			notificationService.sendNotificationsToUsersAsync(dto);
			notificationRepository.save(dto);
			
			
		} catch (Exception e) {
			log.warn("Failed to send notification, but test was saved: {}", e.getMessage());
		}

		TestDto result = mapToTestDto(test, allQuestions, null);
		log.info("Successfully created/updated test: {} with ID: {}", test.getTestName(), test.getId());
		return result;
	}

	private ExamType determineExamType(TestDto testDto) {
		if (testDto.getExamId() != null) {
			return ExamType.EXAM_WISE;
		} else if (testDto.getSubjectId() != null) {
			return ExamType.SUBJECT_WISE;
		} else {
			throw new IllegalArgumentException("Either examId or subjectId must be provided");
		}
	}

	private Tests createOrUpdateExamWiseTest(TestDto testDto, List<Users> instituteList) {
		log.info("Creating/updating exam-wise test: {}", testDto.getTestName());

		Exam exam = examRepository.findById(testDto.getExamId())
				.orElseThrow(() -> new ResourceNotFoundException("Exam not found with exam id " + testDto.getExamId()));

		Tests test = testRepository.findByTestNameAndExam(testDto.getTestName(), exam).orElse(null);

		if (test == null) {
			log.info("Creating new exam-wise test");
			test = new Tests();
			test.setTestName(testDto.getTestName());
			test.setExam(exam);
			test.setExamType(ExamType.EXAM_WISE);

			// Initialize sets/lists
			test.setSubjects(new HashSet<>());
			test.setLanguage(new ArrayList<>());
		} else {
			log.info("Updating existing test with ID: {}", test.getId());
		}

		// Update common fields with subject as null (exam-wise tests get subjects from
		// DTO)
		updateCommonTestFields(test, testDto, instituteList, null);
		Tests savedTest = testRepository.save(test);
		log.info("Exam-wise test saved with ID: {}", savedTest.getId());
		return savedTest;
	}

	private Tests createOrUpdateSubjectWiseTest(TestDto testDto, List<Users> instituteList) {
		log.info("Creating/updating subject-wise test: {}", testDto.getTestName());

		Subjects subject = subjectRepository.findById(testDto.getSubjectId()).orElseThrow(
				() -> new ResourceNotFoundException("Subject not found with id " + testDto.getSubjectId()));

		Tests test = testRepository.findByTestNameAndSubject(testDto.getTestName(), subject).orElse(null);

		if (test == null) {
			log.info("Creating new subject-wise test");
			test = new Tests();
			test.setTestName(testDto.getTestName());
			test.setSubject(subject);
			test.setExamType(ExamType.SUBJECT_WISE);

			// Initialize sets/lists
			test.setSubjects(new HashSet<>());
			test.setLanguage(new ArrayList<>());

			// Add the primary subject to subjects set
			test.getSubjects().add(subject);

			if (testDto.getChapterIds() != null && !testDto.getChapterIds().isEmpty()) {
				List<Chapter> chapters = chapterRepository.findAllById(testDto.getChapterIds());
				test.setChapters(chapters);
				log.info("Set {} chapters for test", chapters.size());
			}
		} else {
			log.info("Updating existing subject-wise test with ID: {}", test.getId());
		}

		// Update common fields (subject will be added via updateCommonTestFields)
		updateCommonTestFields(test, testDto, instituteList, subject);
		Tests savedTest = testRepository.save(test);
		log.info("Subject-wise test saved with ID: {}", savedTest.getId());
		return savedTest;
	}

	private void updateCommonTestFields(Tests test, TestDto testDto, List<Users> instituteList, Subjects subject) {
		test.setCorrectMark(testDto.getCorrectMark() != null ? testDto.getCorrectMark() : 1.0);
		test.setDurationMinutes(testDto.getDurationMinutes() != null ? testDto.getDurationMinutes() : 60);
		test.setInstitutes(instituteList);

		// Handle languages - ADD new languages to existing ones
		if (testDto.getLanguage() != null && !testDto.getLanguage().isEmpty()) {
			// Initialize language list if null
			if (test.getLanguage() == null) {
				test.setLanguage(new ArrayList<>());
			}

			// Add new languages that aren't already in the list
			for (Language newLanguage : testDto.getLanguage()) {
				if (!test.getLanguage().contains(newLanguage)) {
					test.getLanguage().add(newLanguage);
				}
			}
			log.info("Added languages: {} to test ID: {}", testDto.getLanguage(), test.getId());
		}

		test.setNegativeMark(testDto.getNegativeMark() != null ? testDto.getNegativeMark() : 0.0);

		// Handle subjects - ADD new subjects to existing ones
		if (subject != null) {
			if (test.getSubjects() == null) {
				test.setSubjects(new HashSet<>());
			}
			test.getSubjects().add(subject);
			log.info("Added subject: {} to test ID: {}", subject.getName(), test.getId());
		}

		// Add subjects from subjectsIds if provided
		if (testDto.getSubjectsIds() != null && !testDto.getSubjectsIds().isEmpty()) {
			if (test.getSubjects() == null) {
				test.setSubjects(new HashSet<>());
			}

			Set<Subjects> additionalSubjects = new HashSet<>(subjectRepository.findAllById(testDto.getSubjectsIds()));

			// Get current subject IDs for comparison
			Set<Long> existingSubjectIds = test.getSubjects().stream().map(Subjects::getId).collect(Collectors.toSet());

			// Only add subjects that aren't already associated
			for (Subjects newSubject : additionalSubjects) {
				if (!existingSubjectIds.contains(newSubject.getId())) {
					test.getSubjects().add(newSubject);
					log.info("Added new subject from DTO: {} to test ID: {}", newSubject.getName(), test.getId());
				}
			}
		}

		if (test.getId() == null) {
			test.setCreatedAt(LocalDateTime.now());
			log.info("Set created date for new test");
		} else {
			test.setUpdatedAt(LocalDateTime.now());
			log.info("Set updated date for existing test");
		}
	}

	private boolean hasQuestionData(TestDto testDto) {
		boolean hasData = (testDto.getQuestion() != null && !testDto.getQuestion().isEmpty())
				|| (testDto.getQuestionDto() != null)
				|| (testDto.getOptionFilesMap() != null && !testDto.getOptionFilesMap().isEmpty());

		log.info("Question data check - Question file: {}, QuestionDto: {}, OptionFilesMap: {}, Result: {}",
				testDto.getQuestion() != null ? "present" : "null",
				testDto.getQuestionDto() != null ? "present" : "null",
				testDto.getOptionFilesMap() != null ? testDto.getOptionFilesMap().size() + " files" : "null", hasData);

		return hasData;
	}

	private QuestionDto processSingleQuestion(TestDto testDto, Tests test) {
		log.info("Processing single question for test ID: {}", test.getId());

		Questions questionEntity = new Questions();
		questionEntity.setTest(test);

		// Set marks from either questionDto or testDto
		if (testDto.getQuestionDto() != null && testDto.getQuestionDto().getMarks() != null) {
			questionEntity.setMarks(testDto.getQuestionDto().getMarks());
		} else {
			questionEntity.setMarks(testDto.getCorrectMark() != null ? testDto.getCorrectMark() : 1.0);
		}
		log.info("Question marks set to: {}", questionEntity.getMarks());

		String questionImageUrl = null;

		// Handle question image upload
		if (testDto.getQuestion() != null && !testDto.getQuestion().isEmpty()) {
			log.info("Uploading question image for test ID: {}", test.getId());
			try {
				CloudinaryUploadResponse response = cloudinaryService.uploadFile(testDto.getQuestion(),
						"tests/" + test.getId() + "/questions");
				questionImageUrl = response.getSecureUrl();
				questionEntity.setQuestionImageUrl(questionImageUrl);
				log.info("Question image uploaded successfully: {}", questionImageUrl);
			} catch (Exception e) {
				log.error("Failed to upload question image: {}", e.getMessage());
				throw new RuntimeException("Failed to upload question image: " + e.getMessage());
			}
		}

		// Handle question text from questionDto
		if (testDto.getQuestionDto() != null && testDto.getQuestionDto().getQuestionText() != null) {
			questionEntity.setQuestionText(testDto.getQuestionDto().getQuestionText());
			log.info("Question text set: {}", testDto.getQuestionDto().getQuestionText());
		}

		// FIXED: Set subject for the question
		if (testDto.getSubjectsIds() != null && !testDto.getSubjectsIds().isEmpty()) {
			// Take the first subject ID from the list
			Long firstSubjectId = testDto.getSubjectsIds().iterator().next();

			Subjects subject = subjectRepository.findById(firstSubjectId).orElseThrow(
					() -> new CustomException("Subject not found with id: " + firstSubjectId, HttpStatus.NOT_FOUND));
			questionEntity.setSubject(subject);
			log.info("Question subject set to: {}", subject.getName());
		} else if (test.getSubject() != null) {
			// If no subject in testDto, use the test's subject (for subject-wise tests)
			questionEntity.setSubject(test.getSubject());
			log.info("Question subject set from test: {}", test.getSubject().getName());
		} else if (test.getSubjects() != null && !test.getSubjects().isEmpty()) {
			// If exam-wise test, take first subject from test's subjects
			Subjects firstSubject = test.getSubjects().iterator().next();
			questionEntity.setSubject(firstSubject);
			log.info("Question subject set from test subjects: {}", firstSubject.getName());
		}

		// Set language
		if (testDto.getLanguage() != null && !testDto.getLanguage().isEmpty()) {
			try {
				questionEntity.setLanguage(Language.valueOf(testDto.getLanguage().get(0).toString()));
				log.info("Question language set to: {}", testDto.getLanguage().get(0));
			} catch (IllegalArgumentException e) {
				log.warn("Invalid language: {}, using default", testDto.getLanguage().get(0));
				questionEntity.setLanguage(Language.ENGLISH); // default language
			}
		} else {
			questionEntity.setLanguage(Language.ENGLISH); // default language
		}

		log.info("Saving question entity for test ID: {}", test.getId());
		Questions savedQuestion = questionRepository.save(questionEntity);
		log.info("Question saved with ID: {}", savedQuestion.getId());

		// Process options using the new optionFilesMap structure
		try {
			if (testDto.getOptionFilesMap() != null && !testDto.getOptionFilesMap().isEmpty()
					|| (testDto.getQuestionDto() != null && testDto.getQuestionDto().getOptions() != null)) {
				log.info("Processing options for question ID: {}", savedQuestion.getId());
				processQuestionOptions(testDto, savedQuestion, test);
				log.info("Options processed successfully for question ID: {}", savedQuestion.getId());
			} else {
				log.info("No options to process for question ID: {}", savedQuestion.getId());
			}
		} catch (Exception e) {
			log.error("Failed to process options for question ID: {}, error: {}", savedQuestion.getId(),
					e.getMessage());
			// Don't throw exception here, as the question is already saved
		}

		// Return the saved question as DTO for response
		QuestionDto result = mapToQuestionDto(savedQuestion);
		log.info("Successfully processed question with ID: {}", savedQuestion.getId());
		return result;
	}

	private void processQuestionOptions(TestDto testDto, Questions savedQuestion, Tests test) {
		log.info("Processing question options for question ID: {}", savedQuestion.getId());

		// If we have both image files AND text options, prefer the image files
		// and merge the text data with the image options
		if (testDto.getOptionFilesMap() != null && !testDto.getOptionFilesMap().isEmpty()
				&& testDto.getQuestionDto() != null && testDto.getQuestionDto().getOptions() != null) {

			log.info("Merging image options with text data for question ID: {}", savedQuestion.getId());
			processMergedOptions(testDto.getOptionFilesMap(), testDto.getQuestionDto().getOptions(), savedQuestion,
					test);

		} else if (testDto.getOptionFilesMap() != null && !testDto.getOptionFilesMap().isEmpty()) {
			// Only image options available
			log.info("Processing {} image options", testDto.getOptionFilesMap().size());
			processImageOptionsFromMap(testDto.getOptionFilesMap(), savedQuestion, test, null);

		} else if (testDto.getQuestionDto() != null && testDto.getQuestionDto().getOptions() != null
				&& !testDto.getQuestionDto().getOptions().isEmpty()) {
			// Only text options available
			log.info("Processing {} text options", testDto.getQuestionDto().getOptions().size());
			processTextOptions(testDto.getQuestionDto().getOptions(), savedQuestion);
		}

		log.info("Completed processing options for question ID: {}", savedQuestion.getId());
	}

	private void processMergedOptions(Map<Integer, MultipartFile> optionFilesMap, List<OptionDto> optionDtos,
			Questions question, Tests test) {
		String optionsFolder = "tests/" + test.getId() + "/questions/" + question.getId() + "/options";
		log.info("Processing merged options in folder: {}", optionsFolder);

		// Validate that exactly one option is correct
		long correctOptionsCount = optionDtos.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();

		if (correctOptionsCount != 1) {
			throw new IllegalArgumentException(
					"Question must have exactly one correct option. Found: " + correctOptionsCount);
		}

		// Validate option numbers are unique
		Set<Integer> optionNumbers = optionDtos.stream().map(OptionDto::getOptionNumber).collect(Collectors.toSet());

		if (optionNumbers.size() != optionDtos.size()) {
			throw new IllegalArgumentException("Option numbers must be unique. Found duplicates.");
		}

		int processedCount = 0;
		int errorCount = 0;

		// Process each option number that exists in both files and DTO
		for (Integer optionNumber : optionFilesMap.keySet()) {
			MultipartFile optionFile = optionFilesMap.get(optionNumber);

			if (optionFile != null && !optionFile.isEmpty()) {
				try {
					log.info("Uploading option {} image for question {}", optionNumber, question.getId());
					CloudinaryUploadResponse optionResponse = cloudinaryService.uploadFile(optionFile, optionsFolder);

					Options optionEntity = new Options();
					optionEntity.setQuestion(question);
					optionEntity.setOptionImageUrl(optionResponse.getSecureUrl());
					optionEntity.setOptionNumber(optionNumber);

					// Find matching option from DTO to get text and correctness
					Optional<OptionDto> matchingOption = optionDtos.stream()
							.filter(opt -> opt.getOptionNumber() != null && opt.getOptionNumber().equals(optionNumber))
							.findFirst();

					if (matchingOption.isPresent()) {
						OptionDto optionDto = matchingOption.get();
						optionEntity.setOptionText(optionDto.getOptionText());
						optionEntity.setIsCorrect(Boolean.TRUE.equals(optionDto.getIsCorrect()));
					} else {
						// If no matching DTO, set default values
						optionEntity.setOptionText("Option " + optionNumber);
						optionEntity.setIsCorrect(false);
					}

					Options savedOption = optionRepository.save(optionEntity);
					processedCount++;
					log.info("Successfully uploaded merged option {} for question {} with ID: {}", optionNumber,
							question.getId(), savedOption.getId());

				} catch (Exception e) {
					errorCount++;
					log.error("Failed to upload option image for option {}: {}", optionNumber, e.getMessage());
					// Continue with other options
				}
			}
		}

		// Process any remaining text-only options that don't have image files
		Set<Integer> processedOptionNumbers = optionFilesMap.keySet();
		for (OptionDto optionDto : optionDtos) {
			if (!processedOptionNumbers.contains(optionDto.getOptionNumber())) {
				// This is a text-only option (no image file)
				Options optionEntity = new Options();
				optionEntity.setQuestion(question);
				optionEntity.setOptionText(optionDto.getOptionText());
				optionEntity.setOptionNumber(optionDto.getOptionNumber());
				optionEntity.setIsCorrect(Boolean.TRUE.equals(optionDto.getIsCorrect()));
				optionEntity.setOptionImageUrl(null); // Explicitly set to null

				Options savedOption = optionRepository.save(optionEntity);
				processedCount++;
				log.info("Saved text-only option {} for question {} with ID: {}", optionDto.getOptionNumber(),
						question.getId(), savedOption.getId());
			}
		}

		log.info("Merged options processing completed - Success: {}, Failed: {}", processedCount, errorCount);
	}

	private void processImageOptionsFromMap(Map<Integer, MultipartFile> optionFilesMap, Questions question, Tests test,
			QuestionDto questionDto) {
		String optionsFolder = "tests/" + test.getId() + "/questions/" + question.getId() + "/options";
		log.info("Processing image options in folder: {}", optionsFolder);

		int processedCount = 0;
		int errorCount = 0;

		for (Map.Entry<Integer, MultipartFile> entry : optionFilesMap.entrySet()) {
			Integer optionNumber = entry.getKey();
			MultipartFile optionFile = entry.getValue();

			if (optionFile != null && !optionFile.isEmpty()) {
				try {
					log.info("Uploading option {} image for question {}", optionNumber, question.getId());
					CloudinaryUploadResponse optionResponse = cloudinaryService.uploadFile(optionFile, optionsFolder);

					Options optionEntity = new Options();
					optionEntity.setQuestion(question);
					optionEntity.setOptionImageUrl(optionResponse.getSecureUrl());
					optionEntity.setOptionNumber(optionNumber);

					// Set option text and correctness from questionDto if available
					boolean isCorrect = false;
					if (questionDto != null && questionDto.getOptions() != null) {
						Optional<OptionDto> matchingOption = questionDto.getOptions().stream().filter(
								opt -> opt.getOptionNumber() != null && opt.getOptionNumber().equals(optionNumber))
								.findFirst();

						if (matchingOption.isPresent()) {
							OptionDto optionDto = matchingOption.get();
							optionEntity.setOptionText(optionDto.getOptionText());
							isCorrect = Boolean.TRUE.equals(optionDto.getIsCorrect());
						}
					}
					optionEntity.setIsCorrect(isCorrect);

					Options savedOption = optionRepository.save(optionEntity);
					processedCount++;
					log.info("Successfully uploaded option {} image for question {} with ID: {}", optionNumber,
							question.getId(), savedOption.getId());

				} catch (Exception e) {
					errorCount++;
					log.error("Failed to upload option image for option {}: {}", optionNumber, e.getMessage());
					// Continue with other options
				}
			}
		}

		log.info("Image options processing completed - Success: {}, Failed: {}", processedCount, errorCount);
	}

	private void processTextOptions(List<OptionDto> optionDtos, Questions question) {
		log.info("Processing {} text options for question ID: {}", optionDtos.size(), question.getId());

		// Validate that exactly one option is correct
		long correctOptionsCount = optionDtos.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();

		if (correctOptionsCount != 1) {
			throw new IllegalArgumentException(
					"Question must have exactly one correct option. Found: " + correctOptionsCount);
		}

		// Validate option numbers are unique
		Set<Integer> optionNumbers = optionDtos.stream().map(OptionDto::getOptionNumber).collect(Collectors.toSet());

		if (optionNumbers.size() != optionDtos.size()) {
			throw new IllegalArgumentException("Option numbers must be unique. Found duplicates.");
		}

		int savedCount = 0;
		for (OptionDto optionDto : optionDtos) {
			Options optionEntity = new Options();
			optionEntity.setQuestion(question);
			optionEntity.setOptionText(optionDto.getOptionText());
			optionEntity.setOptionNumber(optionDto.getOptionNumber());
			optionEntity.setIsCorrect(Boolean.TRUE.equals(optionDto.getIsCorrect()));

			Options savedOption = optionRepository.save(optionEntity);
			savedCount++;
			log.info("Saved text option {} for question {} with ID: {}", optionDto.getOptionNumber(), question.getId(),
					savedOption.getId());
		}

		log.info("Successfully processed {} text options for question ID: {}", savedCount, question.getId());
	}

	@Override
	public TestDto getTestById(Long id) {
		log.info("Fetching test by ID: {}", id);

		// Validation
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}

		Tests test = testRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

		// Validate test has required fields
		validateTest(test);

		List<QuestionDto> questions = getAllQuestionsForTest(id);
		TestDto result = mapToTestDto(test, questions, null);
		log.info("Successfully fetched test by ID: {}", id);
		return result;
	}

//	@Override
//	public TestDto getTestWithQuestionsByIdAndLanguage(Long id, String language) {
//		log.info("Fetching test with questions by ID: {}", id);
//
//		// Validation
//		if (id == null || id <= 0) {
//			throw new IllegalArgumentException("Test ID must be a positive number");
//		}
//
//		Language language2 = Language.valueOf(language);
//
//		Tests test = testRepository.findByIdWithQuestionsAndOptionsByLanguage(id, language2)
//				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
//
//		// Validate test has required fields
//		validateTest(test);
//
//		TestDto result;
//
//		// Apply subject grouping ONLY for EXAM_WISE tests
//		if (ExamType.EXAM_WISE.equals(test.getExamType())) {
//			log.info("EXAM_WISE exam type detected, organizing questions by subject");
//			result = getTestWithSubjectStructuredQuestions(test);
//		} else {
//			// For SUBJECT_WISE tests, return questions as flat list
//			log.info("SUBJECT_WISE exam type detected, returning questions in original order");
//			List<QuestionDto> questionDtos = test.getQuestions().stream().sorted((q1, q2) -> {
//				if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
//					return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
//				}
//				return q1.getId().compareTo(q2.getId());
//			}).map(question -> {
//				validateQuestion(question);
//				return mapToQuestionDtoWithOptions(question);
//			}).collect(Collectors.toList());
//			result = mapToTestDto(test, questionDtos, null);
//			result.setHasSubjectGroups(false);
//			result.setQuestionsBySubject(null); // No subject grouping for SUBJECT_WISE
//		}
//
//		log.info("Successfully fetched test with questions by ID: {}", id);
//		return result;
//	}

//	@Override
//	public TestDto getTestWithQuestionsByIdAndLanguage(Long id, String language, Long selectedLanguageSubjectId) {
//	    log.info("Fetching test with questions by ID: {} with language: {} and selectedLanguageSubjectId: {}", 
//	            id, language, selectedLanguageSubjectId);
//
//	    // Validation
//	    if (id == null || id <= 0) {
//	        throw new IllegalArgumentException("Test ID must be a positive number");
//	    }
//
//	    Language requestedLanguage;
//	    try {
//	        requestedLanguage = Language.valueOf(language.toUpperCase());
//	    } catch (IllegalArgumentException e) {
//	        throw new IllegalArgumentException("Invalid language: " + language);
//	    }
//
//	    Tests test = testRepository.findById(id)
//	            .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
//
//	    // Validate test has required fields
//	    validateTest(test);
//
//	    // Get ALL questions for the test with the requested language
//	    Tests testWithQuestions = testRepository.findByIdWithQuestionsAndOptionsByLanguage(id, requestedLanguage);
//	             // Fallback to basic test if no language-specific query
//
//	    // Get Hindi and English subjects from the test
//	    Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);
//	    
//	    Set<Questions> filteredQuestions = testWithQuestions.getQuestions() != null ? 
//	            new HashSet<>(testWithQuestions.getQuestions()) : new HashSet<>();
//	    
//	    // Apply Hindi/English filtering if a language subject is selected AND test has Hindi/English subjects
//	    if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
//	        log.info("Applying Hindi/English filtering for selected subject ID: {}", selectedLanguageSubjectId);
//	        
//	        // Verify the selected subject is actually Hindi or English
//	        Subjects selectedSubject = hindiEnglishSubjects.stream()
//	                .filter(subject -> subject.getId().equals(selectedLanguageSubjectId))
//	                .findFirst()
//	                .orElseThrow(() -> new IllegalArgumentException(
//	                        "Selected subject ID " + selectedLanguageSubjectId + 
//	                        " is not a Hindi or English subject in this test"));
//	        
//	        // Get the OTHER Hindi/English subject (to exclude)
//	        Subjects otherLanguageSubject = hindiEnglishSubjects.stream()
//	                .filter(subject -> !subject.getId().equals(selectedLanguageSubjectId))
//	                .findFirst()
//	                .orElse(null);
//	        
//	        log.info("Selected subject: {} (ID: {})", selectedSubject.getName(), selectedSubject.getId());
//	        if (otherLanguageSubject != null) {
//	            log.info("Excluding other language subject: {} (ID: {})", 
//	                    otherLanguageSubject.getName(), otherLanguageSubject.getId());
//	            
//	            // Filter questions: exclude those from the other language subject
//	            filteredQuestions = filteredQuestions.stream()
//	                    .filter(question -> {
//	                        if (question.getSubject() == null) {
//	                            return true; // Keep questions without subject
//	                        }
//	                        
//	                        // Exclude questions from the other language subject
//	                        return !question.getSubject().getId().equals(otherLanguageSubject.getId());
//	                    })
//	                    .collect(Collectors.toSet());
//	            
//	            log.info("Filtered questions: {} remaining (removed {} questions)", 
//	                    filteredQuestions.size(), 
//	                    testWithQuestions.getQuestions().size() - filteredQuestions.size());
//	        }
//	    } else if (selectedLanguageSubjectId != null && hindiEnglishSubjects.isEmpty()) {
//	        log.warn("Selected language subject ID {} but test has no Hindi/English subjects. Ignoring filter.", 
//	                selectedLanguageSubjectId);
//	    }
//
//	    TestDto result;
//
//	    // Apply subject grouping ONLY for EXAM_WISE tests
//	    if (ExamType.EXAM_WISE.equals(test.getExamType())) {
//	        log.info("EXAM_WISE exam type detected, organizing questions by subject");
//	        result = getTestWithSubjectStructuredQuestions(test, filteredQuestions, selectedLanguageSubjectId);
//	    } else {
//	        // For SUBJECT_WISE tests, return questions as flat list
//	        log.info("SUBJECT_WISE exam type detected, returning questions in original order");
//	        List<QuestionDto> questionDtos = filteredQuestions.stream()
//	                .sorted((q1, q2) -> {
//	                    if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
//	                        return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
//	                    }
//	                    return q1.getId().compareTo(q2.getId());
//	                })
//	                .map(question -> {
//	                    validateQuestion(question);
//	                    return mapToQuestionDtoWithOptions(question);
//	                })
//	                .collect(Collectors.toList());
//	        result = mapToTestDto(test, questionDtos, null);
//	        result.setHasSubjectGroups(false);
//	        result.setQuestionsBySubject(null);
//	    }
//	    
//	    // Add language selection info to response
//	    if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
//	        try {
//	            Subjects selectedSubject = subjectRepository.findById(selectedLanguageSubjectId)
//	                    .orElseThrow(() -> new ResourceNotFoundException(
//	                            "Selected subject not found with ID: " + selectedLanguageSubjectId));
//	            
//	            result.setSelectedLanguageSubjectId(selectedSubject.getId());
//	            result.setSelectedLanguageSubjectName(selectedSubject.getName());
//	            result.setIsLanguageSubjectSelected(true);
//	            
//	            log.info("Added language selection info: {} (ID: {})", 
//	                    selectedSubject.getName(), selectedSubject.getId());
//	        } catch (Exception e) {
//	            log.warn("Could not add language selection info: {}", e.getMessage());
//	        }
//	    }
//
//	    log.info("Successfully fetched test with questions by ID: {}", id);
//	    return result;
//	}

//	@Override
//	public TestDto getTestWithQuestionsByIdAndLanguage(Long id, String language, Long selectedLanguageSubjectId) {
//	    log.info("Fetching test with questions by ID: {} with language: {} and selectedLanguageSubjectId: {}", 
//	            id, language, selectedLanguageSubjectId);
//
//	    // Validation
//	    if (id == null || id <= 0) {
//	        throw new IllegalArgumentException("Test ID must be a positive number");
//	    }
//
//	    Language requestedLanguage;
//	    try {
//	        requestedLanguage = Language.valueOf(language.toUpperCase());
//	    } catch (IllegalArgumentException e) {
//	        throw new IllegalArgumentException("Invalid language: " + language);
//	    }
//
//	    // Get test without questions first
//	    Tests test = testRepository.findById(id)
//	            .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
//
//	    // Validate test has required fields
//	    validateTest(test);
//
//	    // Get questions filtered by question language
////	    List<Questions> questionsByLanguage = questionRepository.findByTestIdAndLanguage(id, requestedLanguage);
//	    
////	    List<Questions> questionsByLanguage = questionRepository.findByTestIdAndQuestionLanguage(id, requestedLanguage);
//	    
//	    Tests testWithQuestions = testRepository.findByIdWithQuestionsAndOptionsByLanguage(id, requestedLanguage);
//	    
//	    
////	    log.info("Found {} questions with language {} for test ID {}", 
////	            questionsByLanguage.size(), requestedLanguage, id);
//	    
//	    // Convert to Set
//	    Set<Questions> filteredQuestions = new HashSet<>(testWithQuestions.getQuestions());
//	    
//	    // Fallback: if no questions found with requested language, get all questions
////	    if (filteredQuestions.isEmpty()) {
////	        log.warn("No questions found for language {} in test ID {}. Falling back to all questions.", 
////	                requestedLanguage, id);
////	        
////	        // Get all questions for the test (with options)
////	        Tests testWithAllQuestions = testRepository.findByIdWithQuestionsAndOptions(id)
////	                .orElse(test);
////	        
////	        if (testWithAllQuestions.getQuestions() != null) {
////	            filteredQuestions = new HashSet<>(testWithAllQuestions.getQuestions());
////	        }
////	    }
//
//	    // Get Hindi and English subjects from the test
//	    Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);
//	    
//	    // Apply Hindi/English filtering if a language subject is selected AND test has Hindi/English subjects
//	    if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
//	        log.info("Applying Hindi/English filtering for selected subject ID: {}", selectedLanguageSubjectId);
//	        
//	        // Verify the selected subject is actually Hindi or English
//	        Subjects selectedSubject = hindiEnglishSubjects.stream()
//	                .filter(subject -> subject.getId().equals(selectedLanguageSubjectId))
//	                .findFirst()
//	                .orElseThrow(() -> new IllegalArgumentException(
//	                        "Selected subject ID " + selectedLanguageSubjectId + 
//	                        " is not a Hindi or English subject in this test"));
//	        
//	        // Get the OTHER Hindi/English subject (to exclude)
//	        Subjects otherLanguageSubject = hindiEnglishSubjects.stream()
//	                .filter(subject -> !subject.getId().equals(selectedLanguageSubjectId))
//	                .findFirst()
//	                .orElse(null);
//	        
//	        log.info("Selected subject: {} (ID: {})", selectedSubject.getName(), selectedSubject.getId());
//	        if (otherLanguageSubject != null) {
//	            log.info("Excluding other language subject: {} (ID: {})", 
//	                    otherLanguageSubject.getName(), otherLanguageSubject.getId());
//	            
//	            // Filter questions: exclude those from the other language subject
//	            filteredQuestions = filteredQuestions.stream()
//	                    .filter(question -> {
//	                        if (question.getSubject() == null) {
//	                            return true; // Keep questions without subject
//	                        }
//	                        
//	                        // Exclude questions from the other language subject
//	                        return !question.getSubject().getId().equals(otherLanguageSubject.getId());
//	                    })
//	                    .collect(Collectors.toSet());
//	            
//	            log.info("Filtered questions: {} remaining after subject filtering", 
//	                    filteredQuestions.size());
//	        }
//	    } else if (selectedLanguageSubjectId != null && hindiEnglishSubjects.isEmpty()) {
//	        log.warn("Selected language subject ID {} but test has no Hindi/English subjects. Ignoring filter.", 
//	                selectedLanguageSubjectId);
//	    }
//
//	    TestDto result;
//
//	    // Apply subject grouping ONLY for EXAM_WISE tests
//	    if (ExamType.EXAM_WISE.equals(test.getExamType())) {
//	        log.info("EXAM_WISE exam type detected, organizing questions by subject");
//	        result = getTestWithSubjectStructuredQuestions(test, filteredQuestions, selectedLanguageSubjectId);
//	    } else {
//	        // For SUBJECT_WISE tests, return questions as flat list
//	        log.info("SUBJECT_WISE exam type detected, returning questions in original order");
//	        List<QuestionDto> questionDtos = filteredQuestions.stream()
//	                .sorted((q1, q2) -> {
//	                    if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
//	                        return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
//	                    }
//	                    return q1.getId().compareTo(q2.getId());
//	                })
//	                .map(question -> {
//	                    validateQuestion(question);
//	                    return mapToQuestionDtoWithOptions(question);
//	                })
//	                .collect(Collectors.toList());
//	        result = mapToTestDto(test, questionDtos, null);
//	        result.setHasSubjectGroups(false);
//	        result.setQuestionsBySubject(null);
//	        result.setTestLanguage(requestedLanguage.toString());
//	    }
//	    
//	    // Add language selection info to response
//	    if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
//	        try {
//	            Subjects selectedSubject = subjectRepository.findById(selectedLanguageSubjectId)
//	                    .orElseThrow(() -> new ResourceNotFoundException(
//	                            "Selected subject not found with ID: " + selectedLanguageSubjectId));
//	            
//	            result.setSelectedLanguageSubjectId(selectedSubject.getId());
//	            result.setSelectedLanguageSubjectName(selectedSubject.getName());
//	            result.setIsLanguageSubjectSelected(true);
//	            
//	            log.info("Added language selection info: {} (ID: {})", 
//	                    selectedSubject.getName(), selectedSubject.getId());
//	        } catch (Exception e) {
//	            log.warn("Could not add language selection info: {}", e.getMessage());
//	        }
//	    }
//
//	    // Add language info to response
////	    result.setRequestedLanguage(language);
//	    result.setHasQuestionsForRequestedLanguage(!testWithQuestions.getQuestions().isEmpty());
//	    
//	    log.info("Successfully fetched test with questions by ID: {}", id);
//	    return result;
//	}
//	

	@Override
	public TestDto getTestWithQuestionsByIdAndLanguage(Long id, String language, String subjectName) {
		log.info("Fetching test with questions by ID: {} with language: {} and subjectName: {}", id, language,
				subjectName);

		Subjects subject1 = null;

		if (subjectName != null) {
			subject1 = subjectRepository.findByName(subjectName)
					.orElseThrow(() -> new CustomException("Subject not found", HttpStatus.NOT_FOUND));

		}

		Long selectedLanguageSubjectId = (subject1 != null) ? subject1.getId() : null;

		// Validation
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}

		Language requestedLanguage;
		try {
			requestedLanguage = Language.valueOf(language.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid language: " + language);
		}

		// Get test without questions first
		Tests test = testRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

		// Validate test has required fields
		validateTest(test);

		// Check if the requested language is available in test languages
		List<Language> testLanguages = test.getLanguage();
		if (testLanguages == null || !testLanguages.contains(requestedLanguage)) {
			throw new IllegalArgumentException("Test does not support requested language: " + language
					+ ". Available languages: " + testLanguages);
		}

		// Get all questions for the test (without language filter on question level)
		Tests testWithQuestions = testRepository.findByIdWithQuestionsAndOptions(id)
				.orElseThrow(() -> new ResourceNotFoundException("Test with questions not found with id: " + id));

		// Convert to Set - now we have ALL questions regardless of question language
		Set<Questions> allQuestions = new HashSet<>(testWithQuestions.getQuestions());

		log.info("Found {} total questions for test ID {} with test language {}", allQuestions.size(), id,
				requestedLanguage);

		// Get Hindi and English subjects from the test
		Set<Subjects> hindiEnglishSubjects = getHindiAndEnglishSubjects(test);

		// Apply Hindi/English filtering if a language subject is selected AND test has
		// Hindi/English subjects
		Set<Questions> filteredQuestions = allQuestions;
		if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
			log.info("Applying Hindi/English filtering for selected subject ID: {}", selectedLanguageSubjectId);

			// Verify the selected subject is actually Hindi or English
			Subjects selectedSubject = hindiEnglishSubjects.stream()
					.filter(subject -> subject.getId().equals(selectedLanguageSubjectId)).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Selected subject ID " + selectedLanguageSubjectId
							+ " is not a Hindi or English subject in this test"));

			// Get the OTHER Hindi/English subject (to exclude)
			Subjects otherLanguageSubject = hindiEnglishSubjects.stream()
					.filter(subject -> !subject.getId().equals(selectedLanguageSubjectId)).findFirst().orElse(null);

			log.info("Selected subject: {} (ID: {})", selectedSubject.getName(), selectedSubject.getId());
			if (otherLanguageSubject != null) {
				log.info("Excluding other language subject: {} (ID: {})", otherLanguageSubject.getName(),
						otherLanguageSubject.getId());

				// Filter questions: exclude those from the other language subject
				filteredQuestions = filteredQuestions.stream().filter(question -> {
					if (question.getSubject() == null) {
						return true; // Keep questions without subject
					}

					// Exclude questions from the other language subject
					return !question.getSubject().getId().equals(otherLanguageSubject.getId());
				}).collect(Collectors.toSet());

				log.info("Filtered questions: {} remaining after subject filtering", filteredQuestions.size());
			}
		} else if (selectedLanguageSubjectId != null && hindiEnglishSubjects.isEmpty()) {
			log.warn("Selected language subject ID {} but test has no Hindi/English subjects. Ignoring filter.",
					selectedLanguageSubjectId);
		}

		TestDto result;

		// Apply subject grouping ONLY for EXAM_WISE tests
		if (ExamType.EXAM_WISE.equals(test.getExamType())) {
			log.info("EXAM_WISE exam type detected, organizing questions by subject");
			result = getTestWithSubjectStructuredQuestions(test, filteredQuestions, selectedLanguageSubjectId);
		} else {
			// For SUBJECT_WISE tests, return questions as flat list
			log.info("SUBJECT_WISE exam type detected, returning questions in original order");
			List<QuestionDto> questionDtos = filteredQuestions.stream().sorted((q1, q2) -> {
				if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
					return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
				}
				return q1.getId().compareTo(q2.getId());
			}).map(question -> {
				validateQuestion(question);
				return mapToQuestionDtoWithOptions(question);
			}).collect(Collectors.toList());
			result = mapToTestDto(test, questionDtos, null);
			result.setHasSubjectGroups(false);
			result.setQuestionsBySubject(null);
			result.setTestLanguage(requestedLanguage.toString()); // Set the test language, not question language
		}

		// Add language selection info to response
		if (selectedLanguageSubjectId != null && !hindiEnglishSubjects.isEmpty()) {
			try {
				Subjects selectedSubject = subjectRepository.findById(selectedLanguageSubjectId)
						.orElseThrow(() -> new ResourceNotFoundException(
								"Selected subject not found with ID: " + selectedLanguageSubjectId));

				result.setSelectedLanguageSubjectId(selectedSubject.getId());
				result.setSelectedLanguageSubjectName(selectedSubject.getName());
				result.setIsLanguageSubjectSelected(true);

				log.info("Added language selection info: {} (ID: {})", selectedSubject.getName(),
						selectedSubject.getId());
			} catch (Exception e) {
				log.warn("Could not add language selection info: {}", e.getMessage());
			}
		}

		// Add test language info to response
		result.setTestLanguage(requestedLanguage.toString());
		result.setHasQuestionsForRequestedLanguage(!filteredQuestions.isEmpty());

		log.info("Successfully fetched test with questions by ID: {}", id);
		return result;
	}

	// Helper method to get Hindi and English subjects from a test
	private Set<Subjects> getHindiAndEnglishSubjects(Tests test) {
		if (test.getSubjects() == null || test.getSubjects().isEmpty()) {
			return new HashSet<>();
		}

		return test.getSubjects().stream().filter(this::isHindiOrEnglishSubject).collect(Collectors.toSet());
	}

	// Helper method to check if subject is Hindi or English
	private boolean isHindiOrEnglishSubject(Subjects subject) {
		if (subject == null || subject.getName() == null) {
			return false;
		}

		String subjectName = subject.getName().toLowerCase();
		return subjectName.contains("hindi") || subjectName.contains("हिंदी") || subjectName.contains("hndi")
				|| subjectName.contains("english") || subjectName.contains("अंग्रेजी") || subjectName.contains("eng")
				|| subjectName.contains("भाषा") || // Hindi for "language"
				subjectName.contains("language");
	}

	// Updated method to get test with subject structure using filtered questions
	private TestDto getTestWithSubjectStructuredQuestions(Tests test, Set<Questions> filteredQuestions,
			Long selectedLanguageSubjectId) {
		// Get ALL subjects from the test (not just from questions)
		Set<Subjects> allTestSubjects = test.getSubjects();

		if (allTestSubjects == null || allTestSubjects.isEmpty()) {
			log.warn("No subjects found for test. Returning questions in original order.");
			List<QuestionDto> questionDtos = filteredQuestions.stream().sorted((q1, q2) -> {
				if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
					return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
				}
				return q1.getId().compareTo(q2.getId());
			}).map(this::mapToQuestionDtoWithOptions).collect(Collectors.toList());

			return createTestDtoWithSubjectStructure(test, questionDtos, null, false);
		}

		// Group filtered questions by subject
		Map<Subjects, Set<Questions>> questionsBySubject = filteredQuestions.stream()
				.filter(question -> question.getSubject() != null)
				.collect(Collectors.groupingBy(Questions::getSubject, Collectors.toSet()));

		// Create a map to hold questions by subject name
		Map<String, List<QuestionDto>> questionsBySubjectName = new LinkedHashMap<>();

		// Sort subjects by name for consistent ordering
		List<Subjects> sortedSubjects = allTestSubjects.stream().sorted(Comparator.comparing(Subjects::getName))
				.collect(Collectors.toList());

		// Iterate through ALL test subjects, not just those with questions
		for (Subjects subject : sortedSubjects) {
			Set<Questions> subjectQuestions = questionsBySubject.getOrDefault(subject, new HashSet<>());

			// Sort questions within subject by question number or ID
			List<Questions> sortedSubjectQuestions = subjectQuestions.stream().sorted((q1, q2) -> {
				if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
					return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
				}
				return q1.getId().compareTo(q2.getId());
			}).collect(Collectors.toList());

			// Convert questions to DTOs
			List<QuestionDto> subjectQuestionDtos = sortedSubjectQuestions.stream()
					.map(this::mapToQuestionDtoWithOptions).collect(Collectors.toList());

			// Add to the subject-wise map (even if empty list)
			questionsBySubjectName.put(subject.getName(), subjectQuestionDtos);

			log.info("Subject: {} - {} questions", subject.getName(), subjectQuestionDtos.size());
		}

		// Calculate total questions
		int totalQuestions = filteredQuestions.size();

		// Create TestDto with structured questions
		return createTestDtoWithSubjectStructure(test, new ArrayList<>(), questionsBySubjectName, true, totalQuestions);
	}

	// Overloaded method without selectedLanguageSubjectId for backward
	// compatibility
	private TestDto getTestWithSubjectStructuredQuestions(Tests test, Set<Questions> filteredQuestions) {
		return getTestWithSubjectStructuredQuestions(test, filteredQuestions, null);
	}

	// Updated createTestDtoWithSubjectStructure method
	private TestDto createTestDtoWithSubjectStructure(Tests test, List<QuestionDto> allQuestions,
			Map<String, List<QuestionDto>> questionsBySubject, boolean hasSubjectGroups, int totalQuestions) {

		TestDto testDto = mapToTestDto(test, allQuestions, null);
		testDto.setQuestionsBySubject(questionsBySubject);
		testDto.setHasSubjectGroups(hasSubjectGroups);
		testDto.setTotalQuestions(totalQuestions);

		// Calculate total marks if questions are available
		if (questionsBySubject != null && !questionsBySubject.isEmpty()) {
			double totalMarks = 0.0;
			for (List<QuestionDto> subjectQuestions : questionsBySubject.values()) {
				for (QuestionDto question : subjectQuestions) {
					if (question.getMarks() != null) {
						totalMarks += question.getMarks();
					}
				}
			}
			testDto.setTotalMarks(totalMarks);
		}

		// Set subject names from the map keys
		if (questionsBySubject != null && !questionsBySubject.isEmpty()) {
			testDto.setSubjectNames(new ArrayList<>(questionsBySubject.keySet()));
		}

		return testDto;
	}

	// Overloaded method for backward compatibility
	private TestDto createTestDtoWithSubjectStructure(Tests test, List<QuestionDto> allQuestions,
			Map<String, List<QuestionDto>> questionsBySubject, boolean hasSubjectGroups) {
		return createTestDtoWithSubjectStructure(test, allQuestions, questionsBySubject, hasSubjectGroups,
				allQuestions.size());
	}

	// Updated method to get test with subject structure using filtered questions
//	private TestDto getTestWithSubjectStructuredQuestions(Tests test, Set<Questions> filteredQuestions) {
//	    // Group filtered questions by subject
//	    Map<Subjects, List<Questions>> questionsBySubject = filteredQuestions.stream()
//	            .filter(question -> question.getSubject() != null)
//	            .collect(Collectors.groupingBy(Questions::getSubject));
//
//	    // If no subject grouping found, return questions as flat list
//	    if (questionsBySubject.isEmpty()) {
//	        log.warn("No subjects found for questions. Returning questions in original order.");
//	        List<QuestionDto> questionDtos = filteredQuestions.stream()
//	                .sorted((q1, q2) -> {
//	                    if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
//	                        return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
//	                    }
//	                    return q1.getId().compareTo(q2.getId());
//	                })
//	                .map(this::mapToQuestionDtoWithOptions)
//	                .collect(Collectors.toList());
//
//	        return createTestDtoWithSubjectStructure(test, questionDtos, null, false);
//	    }
//
//	    // Create a map to hold questions by subject name
//	    Map<String, List<QuestionDto>> questionsBySubjectName = new LinkedHashMap<>();
//
//	    for (Map.Entry<Subjects, List<Questions>> entry : questionsBySubject.entrySet()) {
//	        Subjects subject = entry.getKey();
//	        List<Questions> subjectQuestions = entry.getValue();
//
//	        // Sort questions within subject by question number or ID
//	        List<Questions> sortedSubjectQuestions = subjectQuestions.stream()
//	                .sorted((q1, q2) -> {
//	                    if (q1.getQuestionNumber() != null && q2.getQuestionNumber() != null) {
//	                        return q1.getQuestionNumber().compareTo(q2.getQuestionNumber());
//	                    }
//	                    return q1.getId().compareTo(q2.getId());
//	                })
//	                .collect(Collectors.toList());
//
//	        // Convert questions to DTOs
//	        List<QuestionDto> subjectQuestionDtos = sortedSubjectQuestions.stream()
//	                .map(this::mapToQuestionDtoWithOptions)
//	                .collect(Collectors.toList());
//
//	        // Add to the subject-wise map
//	        questionsBySubjectName.put(subject.getName(), subjectQuestionDtos);
//	    }
//
//	    // Create TestDto with structured questions
//	    return createTestDtoWithSubjectStructure(test, new ArrayList<>(), questionsBySubjectName, true);
//	}

	// Update the createTestDtoWithSubjectStructure method
//	private TestDto createTestDtoWithSubjectStructure(Tests test, List<QuestionDto> allQuestions,
//			Map<String, List<QuestionDto>> questionsBySubject, boolean hasSubjectGroups) {
//		TestDto testDto = mapToTestDto(test, allQuestions, null);
//		testDto.setQuestionsBySubject(questionsBySubject);
//		testDto.setHasSubjectGroups(hasSubjectGroups);
//
//		// If we have subject groups, set questions to empty list
//		if (hasSubjectGroups && questionsBySubject != null && !questionsBySubject.isEmpty()) {
//			// Option 1: Set to empty list
//			testDto.setQuestions(new ArrayList<>());
//
//			// Option 2: Or calculate total from subject groups
//			int totalQuestions = questionsBySubject.values().stream().mapToInt(List::size).sum();
//			testDto.setTotalQuestions(totalQuestions);
//		}
//
//		return testDto;
//	}

	@Override
	public List<TestDto> getAllTests() {
		log.info("Fetching all tests");

		List<Tests> tests = testRepository.findAll();
		System.out.println(tests.get(0).getTestName());

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No tests found");
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
//                
			return mapToTestDtoWithOutQuestion(test);

		}).collect(Collectors.toList());

		log.info("Successfully fetched {} tests", result.size());
		return result;
	}

	@Override
	public List<TestDto> getAllTestsWithQuestions() {
		log.info("Fetching all tests with questions");

		List<Tests> tests = testRepository.findAllWithQuestionsAndOptions();

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No tests found");
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
			List<QuestionDto> questionDtos = test.getQuestions().stream().map(question -> {
				validateQuestion(question);
				return mapToQuestionDtoWithOptions(question);
			}).collect(Collectors.toList());
			return mapToTestDto(test, questionDtos, null);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} tests with questions", result.size());
		return result;
	}

	@Override
	@Transactional
	public TestDto updateTest(Long id, TestDto testDto) {
		log.info("Updating test with ID: {}", id);

		// Validation
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}

		Tests existingTest = testRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

		// Update test fields
		if (testDto.getTestName() != null && !testDto.getTestName().trim().isEmpty()) {
			existingTest.setTestName(testDto.getTestName());
		}
		if (testDto.getDurationMinutes() != null && testDto.getDurationMinutes() > 0) {
			existingTest.setDurationMinutes(testDto.getDurationMinutes());
		}
		if (testDto.getCorrectMark() != null && testDto.getCorrectMark() >= 0) {
			existingTest.setCorrectMark(testDto.getCorrectMark());
		}
		if (testDto.getNegativeMark() != null && testDto.getNegativeMark() >= 0) {
			existingTest.setNegativeMark(testDto.getNegativeMark());
		}

		// ADD languages to existing list
		if (testDto.getLanguage() != null && !testDto.getLanguage().isEmpty()) {
			// Initialize if null
			if (existingTest.getLanguage() == null) {
				existingTest.setLanguage(new ArrayList<>());
			}

			// Add new languages that aren't already in the list
			for (Language newLanguage : testDto.getLanguage()) {
				if (!existingTest.getLanguage().contains(newLanguage)) {
					existingTest.getLanguage().add(newLanguage);
					log.info("Added language: {} to test ID: {}", newLanguage, id);
				}
			}
		}

		// Update institutes if provided
		if (testDto.getInstituteIds() != null && !testDto.getInstituteIds().isEmpty()) {
			List<Users> instituteList = userRepository.findAllById(testDto.getInstituteIds());
			if (instituteList.isEmpty()) {
				throw new ResourceNotFoundException("No institutes found with provided IDs");
			}
			existingTest.setInstitutes(instituteList);
		}

		// ADD subjects - don't replace, add to existing
		if (testDto.getSubjectsIds() != null && !testDto.getSubjectsIds().isEmpty()) {
			if (existingTest.getSubjects() == null) {
				existingTest.setSubjects(new HashSet<>());
			}

			Set<Subjects> newSubjects = new HashSet<>(subjectRepository.findAllById(testDto.getSubjectsIds()));

			// Get current subject IDs
			Set<Long> existingSubjectIds = existingTest.getSubjects().stream().map(Subjects::getId)
					.collect(Collectors.toSet());

			// Add only new subjects
			int addedCount = 0;
			for (Subjects newSubject : newSubjects) {
				if (!existingSubjectIds.contains(newSubject.getId())) {
					existingTest.getSubjects().add(newSubject);
					addedCount++;
					log.info("Added subject: {} to test ID: {}", newSubject.getName(), id);
				}
			}

			log.info("Added {} new subjects to test ID: {}", addedCount, id);
		}

		existingTest.setUpdatedAt(LocalDateTime.now());
		Tests updatedTest = testRepository.save(existingTest);

		List<QuestionDto> questions = getAllQuestionsForTest(updatedTest.getId());
		TestDto result = mapToTestDto(updatedTest, questions, null);
		log.info("Successfully updated test with ID: {}", id);
		return result;
	}

	@Override
	@Transactional
	public void deleteTest(Long id) {
		log.info("Deleting test with ID: {}", id);

		// Validation
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}

		Tests test = testRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));

		// First delete all questions and options (if cascade is not set up)
		List<Questions> questions = questionRepository.findByTest(test);
		for (Questions question : questions) {
			// Delete options first
			optionRepository.deleteByQuestion(question);
			// Then delete question
			questionRepository.delete(question);
		}

		testRepository.delete(test);
		log.info("Successfully deleted test with ID: {}", id);
	}

	@Override
	public List<TestDto> getTestsByExamIdWithQuestions(Long examId) {
		log.info("Fetching tests with questions by exam ID: {}", examId);

		// Validation
		if (examId == null || examId <= 0) {
			throw new IllegalArgumentException("Exam ID must be a positive number");
		}

		// Verify exam exists
		if (!examRepository.existsById(examId)) {
			throw new ResourceNotFoundException("Exam not found with id: " + examId);
		}

		List<Tests> tests = testRepository.findByExamId(examId);

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No tests found for exam id: " + examId);
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
			// Use the custom query to fetch test with questions and options
			Tests testWithQuestions = testRepository.findByIdWithQuestionsAndOptions(test.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Test details not found for id: " + test.getId()));

			List<QuestionDto> questionDtos = testWithQuestions.getQuestions().stream().map(question -> {
				validateQuestion(question);
				return mapToQuestionDtoWithOptions(question);
			}).collect(Collectors.toList());

			return mapToTestDto(testWithQuestions, questionDtos, null);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} tests with questions for exam ID: {}", result.size(), examId);
		return result;
	}

//	@Override
//	@Transactional
//	public TestDto processExcelFile(MultipartFile excelFile, TestDto testDto) {
//		log.info("Processing Excel file: {} for test: {}", excelFile.getOriginalFilename(), testDto.getTestName());
//
//		// Validation
//		if (excelFile == null || excelFile.isEmpty()) {
//			throw new IllegalArgumentException("Excel file is required");
//		}
//
//		validateTestDto(testDto);
//
//		try {
//			// Read Excel file and process questions
//			List<QuestionDto> questionsFromExcel = readQuestionsFromExcel(excelFile);
//
//			if (questionsFromExcel.isEmpty()) {
//				throw new IllegalArgumentException("No valid questions found in the Excel file");
//			}
//
//			// Create or get test based on exam type
//			Tests test = createOrGetTest(testDto);
//
//			// Process each question from Excel
//			for (QuestionDto questionDto : questionsFromExcel) {
//				processQuestionFromExcel(questionDto, test, testDto.getLanguage().get(0).toString(),
//						testDto.getSubjectsIds());
//			}
//
//			List<QuestionDto> allQuestions = getAllQuestionsForTest(test.getId());
//			TestDto result = mapToTestDto(test, allQuestions, null);
//			log.info("Successfully processed Excel file with {} questions for {} test", questionsFromExcel.size(),
//					test.getExamType());
//			return result;
//
//		} catch (IOException e) {
//			log.error("Error processing Excel file: {}", e.getMessage(), e);
//			throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
//		}
//	}

	@Override
	@Transactional
	public TestDto processExcelFile(MultipartFile excelFile, TestDto testDto) {
		log.info("Processing Excel file: {} for test: {}", excelFile.getOriginalFilename(), testDto.getTestName());

		// Validation
		if (excelFile == null || excelFile.isEmpty()) {
			throw new IllegalArgumentException("Excel file is required");
		}

		validateTestDto(testDto);

		try {
			// Read Excel file and process questions
			List<QuestionDto> questionsFromExcel = readQuestionsFromExcel(excelFile);

			if (questionsFromExcel.isEmpty()) {
				throw new IllegalArgumentException("No valid questions found in the Excel file");
			}

			// Create or get test based on exam type
			Tests test = createOrGetTest(testDto);

			// Process each question from Excel
			for (QuestionDto questionDto : questionsFromExcel) {
				processQuestionFromExcel(questionDto, test, testDto.getLanguage().get(0).toString(),
						testDto.getSubjectsIds());
			}

			List<QuestionDto> allQuestions = getAllQuestionsForTest(test.getId());
			TestDto result = mapToTestDto(test, allQuestions, null);
			log.info("Successfully processed Excel file with {} questions for {} test", questionsFromExcel.size(),
					test.getExamType());
			
//			List<Long> instituteIds = testDto.getInstituteIds().stream().map(Users::getId).collect(Collectors.toList());

			Notifications dto = new Notifications();
			dto.setUserIds(testDto.getInstituteIds());
			dto.setMessage("New test has been created with test name --> "+ test.getTestName());
			dto.setType(NotificationType.TEST_CREATION);
			
		notificationRepository.save(dto);
			
			
			
			
			return result;

		} catch (IOException e) {
			log.error("Error processing Excel file: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
		}
	}

	// Updated Excel reading method to handle 5 options
	private List<QuestionDto> readQuestionsFromExcel(MultipartFile excelFile) throws IOException {
		log.info("Reading questions from Excel file: {}", excelFile.getOriginalFilename());
		List<QuestionDto> questions = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);

			// Validate Excel format
			validateExcelFormat(sheet);

			// Skip header row and process data rows
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				QuestionDto questionDto = parseQuestionFromRow(row);
				if (questionDto != null) {
					questions.add(questionDto);
				}
			}
		} catch (Exception e) {
			log.error("Error reading Excel file: {}", e.getMessage(), e);
			throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
		}

		log.info("Successfully read {} questions from Excel file", questions.size());
		return questions;
	}

	// Updated validation for Excel format with 5 options
	private void validateExcelFormat(Sheet sheet) {
		Row headerRow = sheet.getRow(0);
		if (headerRow == null) {
			throw new IllegalArgumentException("Excel file is empty or invalid");
		}

		// Basic validation for expected columns
		// Should have at least: Question, Option A, Option B, Option C, Option D, Right
		// Answer
		// Option E is optional
		int minExpectedColumns = 6; // Question + A,B,C,D + Right Answer

		if (headerRow.getLastCellNum() < minExpectedColumns) {
			throw new IllegalArgumentException(
					String.format("Invalid Excel format. Expected at least %d columns, found %d", minExpectedColumns,
							headerRow.getLastCellNum()));
		}

		log.debug("Excel format validation passed with {} columns", headerRow.getLastCellNum());
	}

	// Updated method to parse questions with up to 5 options
	private QuestionDto parseQuestionFromRow(Row row) {
		try {
			String questionText = getCellStringValue(row.getCell(0));
			if (questionText == null || questionText.trim().isEmpty()) {
				return null; // Skip empty rows
			}

			// Default marks since your Excel doesn't have marks column
			Double marks = 1.0;

			List<OptionDto> options = new ArrayList<>();

			// Parse options (columns 1-5: Options A, B, C, D, E)
			// Handle up to 5 options, E might be null/empty
			for (int i = 1; i <= 5; i++) {
				Cell cell = row.getCell(i);
				String optionText = getCellStringValue(cell);

				// For option E (i=5), it's okay if it's null/empty
				// For options A-D (i=1-4), they should exist
				if (i == 5 && (optionText == null || optionText.trim().isEmpty())) {
					// Option E is optional, skip it if empty
					log.debug("Option E is empty for question: {}",
							questionText.substring(0, Math.min(50, questionText.length())));
					break; // No more options after E
				}

				if (optionText != null && !optionText.trim().isEmpty()) {
					int optionNumber = i; // Option numbers: 1, 2, 3, 4, 5
					options.add(OptionDto.builder().optionText(optionText.trim()).optionNumber(optionNumber)
							.isCorrect(false) // Will set correct one below
							.build());
				} else if (i <= 4) {
					// Options A-D should not be empty
					log.warn("Option {} is empty for question: {}", getOptionLetter(i),
							questionText.substring(0, Math.min(50, questionText.length())));
				}
			}

			// Get correct option from column 6 (Right Answer)
			String correctAnswer = getCellStringValue(row.getCell(6));
			Integer correctOptionNumber = parseCorrectOptionNumber(correctAnswer);

			if (correctOptionNumber != null && !options.isEmpty()) {
				// Check if the correct option number exists in our options list
				boolean foundCorrectOption = options.stream()
						.anyMatch(opt -> opt.getOptionNumber().equals(correctOptionNumber));

				if (foundCorrectOption) {
					options.stream().filter(opt -> opt.getOptionNumber().equals(correctOptionNumber)).findFirst()
							.ifPresent(opt -> opt.setIsCorrect(true));
				} else {
					log.warn("Correct answer '{}' not found in options for question: {}", correctAnswer,
							questionText.substring(0, Math.min(50, questionText.length())));
				}
			} else if (correctOptionNumber == null) {
				log.warn("No valid correct answer found for question: {}",
						questionText.substring(0, Math.min(50, questionText.length())));
			}

			// Validate that we have at least 4 options
			if (options.size() < 4) {
				log.warn("Question has only {} options (minimum 4 required): {}", options.size(),
						questionText.substring(0, Math.min(50, questionText.length())));
			}

			// Validate that exactly one option is marked correct
			long correctOptionsCount = options.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();

			if (correctOptionsCount != 1) {
				log.warn("Question must have exactly one correct option. Found: {} for question: {}",
						correctOptionsCount, questionText.substring(0, Math.min(50, questionText.length())));
			}

			QuestionDto questionDto = QuestionDto.builder().questionText(questionText.trim()).marks(marks)
					.options(options).build();

			log.debug("Parsed question from row {}: {} ({} options)", row.getRowNum(),
					questionText.substring(0, Math.min(50, questionText.length())), options.size());
			return questionDto;

		} catch (Exception e) {
			log.warn("Failed to parse question from row {}: {}", row.getRowNum(), e.getMessage());
			return null;
		}
	}

	// Helper method to get option letter (A, B, C, D, E)
	private String getOptionLetter(int optionNumber) {
		switch (optionNumber) {
		case 1:
			return "A";
		case 2:
			return "B";
		case 3:
			return "C";
		case 4:
			return "D";
		case 5:
			return "E";
		default:
			return "Option " + optionNumber;
		}
	}

	// Updated correct option parser to handle up to E option
	private Integer parseCorrectOptionNumber(String correctAnswer) {
		if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
			return null;
		}

		try {
			String trimmedAnswer = correctAnswer.trim().toUpperCase();

			// Handle both "A"/"B"/"C"/"D"/"E" and "1"/"2"/"3"/"4"/"5" formats
			switch (trimmedAnswer) {
			case "A":
				return 1;
			case "B":
				return 2;
			case "C":
				return 3;
			case "D":
				return 4;
			case "E":
				return 5;
			case "1":
				return 1;
			case "2":
				return 2;
			case "3":
				return 3;
			case "4":
				return 4;
			case "5":
				return 5;
			default:
				// Try to parse as number
				try {
					int number = Integer.parseInt(trimmedAnswer);
					if (number >= 1 && number <= 5) {
						return number;
					} else {
						log.warn("Correct answer number out of range (1-5): {}", number);
						return null;
					}
				} catch (NumberFormatException e) {
					log.warn("Invalid correct answer format: {}", correctAnswer);
					return null;
				}
			}
		} catch (Exception e) {
			log.warn("Error parsing correct answer: {}", e.getMessage());
			return null;
		}
	}

	// Updated getCellStringValue to handle more cell types
	private String getCellStringValue(Cell cell) {
		if (cell == null)
			return null;

		try {
			switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue().toString();
				} else {
					// Check if it's a whole number
					double numValue = cell.getNumericCellValue();
					if (numValue == Math.floor(numValue)) {
						return String.valueOf((long) numValue);
					} else {
						return String.valueOf(numValue);
					}
				}
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				// Try to evaluate the formula
				switch (cell.getCachedFormulaResultType()) {
				case STRING:
					return cell.getStringCellValue().trim();
				case NUMERIC:
					return String.valueOf(cell.getNumericCellValue());
				case BOOLEAN:
					return String.valueOf(cell.getBooleanCellValue());
				default:
					return cell.getCellFormula();
				}
			case BLANK:
				return null;
			default:
				return null;
			}
		} catch (Exception e) {
			log.warn("Error reading cell value: {}", e.getMessage());
			return null;
		}
	}

	private Tests createOrGetExamWiseTest(TestDto testDto, List<Users> instituteList) {
		log.info("Creating/getting exam-wise test: {}", testDto.getTestName());

		// Validate exam exists
		Exam exam = examRepository.findById(testDto.getExamId())
				.orElseThrow(() -> new ResourceNotFoundException("Exam not found with exam id " + testDto.getExamId()));

		// Check if test already exists
		Tests test = testRepository.findByTestNameAndExam(testDto.getTestName(), exam).orElse(null);

		if (test == null) {
			log.info("Creating new exam-wise test");
			test = new Tests();
			test.setTestName(testDto.getTestName());
			test.setExam(exam);
			test.setExamType(ExamType.EXAM_WISE);

			// Set subjects from exam if available, or from testDto
			if (testDto.getSubjectsIds() != null && !testDto.getSubjectsIds().isEmpty()) {
				Set<Subjects> subjects = new HashSet<>(subjectRepository.findAllById(testDto.getSubjectsIds()));
				test.setSubjects(subjects);
				log.info("Set {} subjects for test", subjects.size());
			} else if (exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
				test.setSubjects(new HashSet<>(exam.getSubjects()));
				log.info("Set {} subjects from exam", exam.getSubjects().size());
			}
		} else {
			log.info("Using existing exam-wise test with ID: {}", test.getId());
		}

		// Update common fields
		updateCommonTestFields(test, testDto, instituteList, null);
		Tests savedTest = testRepository.save(test);
		log.info("Exam-wise test saved with ID: {}", savedTest.getId());
		return savedTest;
	}

	private Tests createOrGetSubjectWiseTest(TestDto testDto, List<Users> instituteList) {
		log.info("Creating/getting subject-wise test: {}", testDto.getTestName());

		// Validate subject exists
		Subjects subject = subjectRepository.findById(testDto.getSubjectId()).orElseThrow(
				() -> new ResourceNotFoundException("Subject not found with subject id " + testDto.getSubjectId()));

		// Check if test already exists
		Tests test = testRepository.findByTestNameAndSubject(testDto.getTestName(), subject).orElse(null);

		if (test == null) {
			log.info("Creating new subject-wise test");
			test = new Tests();
			test.setTestName(testDto.getTestName());
			test.setSubject(subject);
			test.setExamType(ExamType.SUBJECT_WISE);
			test.setExam(null); // Explicitly set exam to null for subject-wise tests

			// Set chapters if provided
			if (testDto.getChapterIds() != null && !testDto.getChapterIds().isEmpty()) {
				List<Chapter> chapters = new ArrayList<>(chapterRepository.findAllById(testDto.getChapterIds()));
				test.setChapters(chapters);
				log.info("Set {} chapters for test", chapters.size());
			}

			// Set subjects for backward compatibility
			Set<Subjects> subjectsSet = new HashSet<>();
			subjectsSet.add(subject);
			test.setSubjects(subjectsSet);

		} else {
			log.info("Using existing subject-wise test with ID: {}", test.getId());
		}

		// Update common fields
		updateCommonTestFields(test, testDto, instituteList, null);
		Tests savedTest = testRepository.save(test);
		log.info("Subject-wise test saved with ID: {}", savedTest.getId());
		return savedTest;
	}

	@Override
	public boolean testExistsByNameAndExam(String testName, Long examId) {
		log.info("Checking if test exists by name: {} and exam ID: {}", testName, examId);

		// Validation
		if (testName == null || testName.trim().isEmpty()) {
			throw new IllegalArgumentException("Test name is required");
		}
		if (examId == null || examId <= 0) {
			throw new IllegalArgumentException("Exam ID must be a positive number");
		}

		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

		boolean exists = testRepository.existsByTestNameAndExam(testName, exam);
		log.info("Test exists check result: {}", exists);
		return exists;
	}

	@Override
	public boolean testExistsByNameAndSubject(String testName, Long subjectId) {
		log.info("Checking if test exists by name: {} and subject ID: {}", testName, subjectId);

		// Validation
		if (testName == null || testName.trim().isEmpty()) {
			throw new IllegalArgumentException("Test name is required");
		}
		if (subjectId == null || subjectId <= 0) {
			throw new IllegalArgumentException("Subject ID must be a positive number");
		}

		Subjects subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

		boolean exists = testRepository.existsByTestNameAndSubject(testName, subject);
		log.info("Test exists check result: {}", exists);
		return exists;
	}

	@Override
	@Transactional
	public TestDto addQuestionsToTest(Long testId, List<QuestionDto> questions) {
		log.info("Adding {} questions to test ID: {}", questions.size(), testId);

		// Validation
		if (testId == null || testId <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}
		if (questions == null || questions.isEmpty()) {
			throw new IllegalArgumentException("Questions list cannot be empty");
		}

		Tests test = testRepository.findById(testId)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

		validateTest(test);

		// Process each question
		for (QuestionDto questionDto : questions) {
			validateQuestionDto(questionDto);
			processSingleQuestionDto(questionDto, test);
		}

		List<QuestionDto> allQuestions = getAllQuestionsForTest(testId);
		TestDto result = mapToTestDto(test, allQuestions, null);
		log.info("Successfully added {} questions to test ID: {}", questions.size(), testId);
		return result;
	}

	@Override
	public List<QuestionDto> getQuestionsByTestId(Long testId) {
		log.info("Fetching questions for test ID: {}", testId);

		// Validation
		if (testId == null || testId <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}

		// Verify test exists
		Tests tests = testRepository.findById(testId)
				.orElseThrow(() -> new ResourceNotFoundException("Test does not exists with test id " + testId));

		List<Questions> questions = questionRepository.findByTestWithOptions(tests);

		if (questions.isEmpty()) {
			throw new ResourceNotFoundException("No questions found for test id: " + testId);
		}
//		System.out.println(questions.get(0).getOptions().toString());

		List<QuestionDto> result = questions.stream().map(question -> {
			validateQuestion(question);
			return mapToQuestionDtoWithOptions(question);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} questions for test ID: {}", result.size(), testId);
		return result;
	}

	@Override
	public List<TestDto> getTestsByInstituteId(Long instituteId, String examType) {
		log.info("Fetching tests by institute ID: {}", instituteId);

		// Validation
		if (instituteId == null || instituteId <= 0) {
			throw new IllegalArgumentException("Institute ID must be a positive number");
		}

		// Verify institute exists
		if (!userRepository.existsById(instituteId)) {
			throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
		}

		List<Tests> tests = testRepository.findByInstituteIdAndExamType(instituteId, ExamType.valueOf(examType));

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No tests found for institute id: " + instituteId);
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
//			List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
//			return mapToTestDto(test, questions, null);
			return mapToTestDtoWithOutQuestion(test);

		}).collect(Collectors.toList());

		log.info("Successfully fetched {} tests for institute ID: {}", result.size(), instituteId);
		return result;
	}

	@Override
	public List<TestDto> getTestsByInstituteIdWithQuestions(Long instituteId) {
		log.info("Fetching tests with questions by institute ID: {}", instituteId);

		// Validation
		if (instituteId == null || instituteId <= 0) {
			throw new IllegalArgumentException("Institute ID must be a positive number");
		}

		// Verify institute exists
		if (!userRepository.existsById(instituteId)) {
			throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
		}

		List<Tests> tests = testRepository.findByInstituteId(instituteId);

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No tests found for institute id: " + instituteId);
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
			// Fetch test with questions and options
			Tests testWithQuestions = testRepository.findByIdWithQuestionsAndOptions(test.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Test details not found for id: " + test.getId()));

			List<QuestionDto> questionDtos = testWithQuestions.getQuestions().stream().map(question -> {
				validateQuestion(question);
				return mapToQuestionDtoWithOptions(question);
			}).collect(Collectors.toList());

			return mapToTestDto(testWithQuestions, questionDtos, null);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} tests with questions for institute ID: {}", result.size(), instituteId);
		return result;
	}

	@Override
	public List<TestDto> getTestsByExamType(String examType) {
		log.info("Fetching tests by exam type: {}", examType);

		try {
			ExamType type = ExamType.valueOf(examType.toUpperCase());
			List<Tests> tests = testRepository.findByExamType(type);

			if (tests.isEmpty()) {
				throw new ResourceNotFoundException("No " + examType.toLowerCase() + " tests found");
			}

			List<TestDto> result = tests.stream().map(test -> {
				validateTest(test);
//                        List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
//                        List<QuestionDto> questions =null;
				return mapToTestDtoWithOutQuestion(test);
			}).collect(Collectors.toList());

			log.info("Successfully fetched {} {} tests", result.size(), examType.toLowerCase());
			return result;

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid exam type: " + examType);
		}
	}

	@Override
	public List<TestDto> getSubjectWiseTestsBySubject(Long subjectId) {
		log.info("Fetching subject-wise tests by subject ID: {}", subjectId);

		// Validation
		if (subjectId == null || subjectId <= 0) {
			throw new IllegalArgumentException("Subject ID must be a positive number");
		}

		// Verify subject exists
		if (!subjectRepository.existsById(subjectId)) {
			throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
		}

		List<Tests> tests = testRepository.findBySubjectId(subjectId);

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No subject-wise tests found for subject id: " + subjectId);
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
			List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
			return mapToTestDto(test, questions, null);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} subject-wise tests for subject ID: {}", result.size(), subjectId);
		return result;
	}

	@Override
	public List<TestDto> getExamWiseTestsByExam(Long examId) {
		log.info("Fetching exam-wise tests by exam ID: {}", examId);

		// Validation
		if (examId == null || examId <= 0) {
			throw new IllegalArgumentException("Exam ID must be a positive number");
		}

		// Verify exam exists
		if (!examRepository.existsById(examId)) {
			throw new ResourceNotFoundException("Exam not found with id: " + examId);
		}

		List<Tests> tests = testRepository.findByExamId(examId);

		if (tests.isEmpty()) {
			throw new ResourceNotFoundException("No exam-wise tests found for exam id: " + examId);
		}

		List<TestDto> result = tests.stream().map(test -> {
			validateTest(test);
			List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
			return mapToTestDto(test, questions, null);
		}).collect(Collectors.toList());

		log.info("Successfully fetched {} exam-wise tests for exam ID: {}", result.size(), examId);
		return result;
	}
 
	@Override
	public TestStatusResponse isTestCurrentlyOpen(Long testId, Long instituteId, Long studentId) {
		try {
			log.info("Checking if test {} is currently open for institute {} and student {}", testId, instituteId,
					studentId);

			// Validate input parameters
			if (testId == null || testId <= 0) {
				throw new IllegalArgumentException("Test ID must be a positive number");
			}
			if (instituteId == null || instituteId <= 0) {
				throw new IllegalArgumentException("Institute ID must be a positive number");
			}
			if (studentId == null || studentId <= 0) {
				throw new IllegalArgumentException("Student ID must be a positive number");
			}

			// Verify test exists
			Tests test = testRepository.findById(testId)
					.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

			// Verify institute exists
			if (!userRepository.existsById(instituteId)) {
				throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
			}

			// Verify student exists
			Users student = userRepository.findById(studentId)
					.orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

			LocalDateTime currentTime = LocalDateTime.now();

			// Check if test is assigned to this institute
			boolean isTestAssignedToInstitute = test.getInstitutes().stream()
					.anyMatch(institute -> institute.getId() == instituteId);

			if (!isTestAssignedToInstitute) {
				log.info("Test {} is not assigned to institute {}", testId, instituteId);
				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(false).status("NOT_ASSIGNED").currentTime(currentTime)
						.message("This test is not assigned to your institute").build();
			}

			// Check if student has already attempted this test
			boolean hasStudentAttempted = testResultRepository.existsByStudentIdAndTestId(studentId, testId);

			if (hasStudentAttempted) {
				log.info("Student {} has already attempted test {}", studentId, testId);
				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(false).status("ALREADY_ATTEMPTED").currentTime(currentTime)
						.message("You have already attempted this test").build();
			}

			// Check test timing from TestInstituteTime
			Optional<TestInstituteTime> activeTiming = testInstituteTimeRepository.findByTestIdAndInstituteId(testId,
					instituteId);

			if (activeTiming.isEmpty()) {
				log.info("No timing set for test {} and institute {}", testId, instituteId);
				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(false).status("NO_TIMING_SET").currentTime(currentTime)
						.message("No timing schedule found for this test in your institute").build();
			}

			TestInstituteTime timing = activeTiming.get();
			LocalDateTime startTime = timing.getStarDateTime();
			LocalDateTime endTime = timing.getEnDateTime();

			// Check if current time is within test timing
			if (currentTime.isBefore(startTime)) {
				// Test is scheduled for future
				long minutesUntilStart = java.time.Duration.between(currentTime, startTime).toMinutes();
				log.info("Test {} is scheduled to start in {} minutes", testId, minutesUntilStart);

				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(false).status("SCHEDULED").currentTime(currentTime).testStartTime(startTime)
						.testEndTime(endTime).message("Test is scheduled to start in " + minutesUntilStart + " minutes")
						.timeRemainingMinutes(minutesUntilStart)
						.timeRemainingFormatted(formatDuration(minutesUntilStart)).build();

			} else if (currentTime.isAfter(endTime)) {
				// Test has ended
				log.info("Test {} has ended", testId);
				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(false).status("ENDED").currentTime(currentTime).testStartTime(startTime)
						.testEndTime(endTime).message("Test has ended").build();

			} else {
				// Test is currently open
				long minutesRemaining = java.time.Duration.between(currentTime, endTime).toMinutes();
				log.info("Test {} is currently open with {} minutes remaining", testId, minutesRemaining);

				return TestStatusResponse.builder().testId(testId).instituteId(instituteId).studentId(studentId)
						.isOpen(true).status("OPEN").currentTime(currentTime).testStartTime(startTime)
						.testEndTime(endTime).message("Test is currently open").timeRemainingMinutes(minutesRemaining)
						.timeRemainingFormatted(formatDuration(minutesRemaining)).build();
			}

		} catch (ResourceNotFoundException e) {
			log.warn("Resource not found while checking test status: {}", e.getMessage());
			throw e;
		} catch (IllegalArgumentException e) {
			log.warn("Invalid input while checking test status: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error checking if test is open for test {}, institute {}, student {}: ", testId,
					instituteId, studentId, e);
			throw new CustomException("Error checking test status: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ========== HELPER METHODS ==========

	private List<QuestionDto> getAllQuestionsForTest(Long testId) {
		log.info("Fetching all questions for test ID: {}", testId);
		Tests tests = testRepository.findById(testId).orElse(null);
		if (tests == null) {
			log.warn("Test not found with ID: {}", testId);
			return new ArrayList<>();
		}
		List<Questions> questions = questionRepository.findByTest(tests);
		List<QuestionDto> result = questions.stream().map(this::mapToQuestionDto).collect(Collectors.toList());
		log.info("Found {} questions for test ID: {}", result.size(), testId);
		return result;
	}

	private QuestionDto mapToQuestionDto(Questions question) {
		// Get options for this question
		List<Options> options = optionRepository.findByQuestion(question);
		List<OptionDto> optionDtos = options.stream().map(this::mapToOptionDto).collect(Collectors.toList());

		QuestionDto.QuestionDtoBuilder builder = QuestionDto.builder().id(question.getId())
				.questionText(question.getQuestionText()).questionImageUrl(question.getQuestionImageUrl())
				.testId(question.getTest().getId()).marks(question.getMarks()).options(optionDtos)
				.language(question.getLanguage().toString());

		// Add subject information if available
		if (question.getSubject() != null) {
			builder.subjectId(question.getSubject().getId()).subjectName(question.getSubject().getName());
		}

		QuestionDto dto = builder.build();
		log.debug("Mapped question ID: {} with {} options", question.getId(), optionDtos.size());
		return dto;
	}

	private QuestionDto mapToQuestionDtoWithOptions(Questions question) {
		List<Options> options = optionRepository.findByQuestion(question);

		List<OptionDto> optionDtos = options.stream().map((option) -> {
			OptionDto dtos = OptionDto.builder().id(option.getId()).optionText(option.getOptionText())
					.optionImageUrl(option.getOptionImageUrl()).questionId(option.getQuestion().getId())
					.optionNumber(option.getOptionNumber()).isCorrect(option.getIsCorrect()).build();
			return dtos;
		}).collect(Collectors.toList());

		QuestionDto.QuestionDtoBuilder builder = QuestionDto.builder().id(question.getId())
				.questionText(question.getQuestionText()).questionImageUrl(question.getQuestionImageUrl())
				.testId(question.getTest().getId()).marks(question.getMarks()).options(optionDtos);

		// Add subject information if available
		if (question.getSubject() != null) {
			builder.subjectId(question.getSubject().getId()).subjectName(question.getSubject().getName());
		}

		QuestionDto dto = builder.build();
		log.debug("Mapped question with options - ID: {} with {} options", question.getId(), optionDtos.size());
		return dto;
	}

	private OptionDto mapToOptionDto(Options option) {
		return OptionDto.builder().id(option.getId()).optionText(option.getOptionText())
				.optionImageUrl(option.getOptionImageUrl()).questionId(option.getQuestion().getId())
				.optionNumber(option.getOptionNumber()).isCorrect(option.getIsCorrect()).build();
	}

//	private TestDto mapToTestDto(Tests test, List<QuestionDto> questionDtos, QuestionDto savedQuestion) {
//		// Safe null checks for all collections
//		List<String> instituteName = Optional.ofNullable(test.getInstitutes()).orElse(Collections.emptyList()).stream()
//				.filter(Objects::nonNull).map(Users::getInstituteName).filter(Objects::nonNull)
//				.collect(Collectors.toList());
//
//		List<String> subjectNames = Optional.ofNullable(test.getSubjects()).orElse(Collections.emptySet()).stream()
//				.filter(Objects::nonNull).map(Subjects::getName).filter(Objects::nonNull).collect(Collectors.toList());
//
//		// Get languages safely
//		List<Language> languages = Optional.ofNullable(test.getLanguage()).orElse(Collections.emptyList());
//
//		// Calculate totals safely
//		int totalQuestions = questionDtos != null ? questionDtos.size() : 0;
//		Double totalMarks = questionDtos != null ? calculateTotalMarks(questionDtos) : 0.0;
//
//		TestDto.TestDtoBuilder builder = TestDto.builder().id(test.getId()).testName(test.getTestName())
//				.correctMark(test.getCorrectMark()).durationMinutes(test.getDurationMinutes()).language(languages) // Set
//																													// languages
//																													// list
//				.negativeMark(test.getNegativeMark()).questions(questionDtos).questionDto(savedQuestion)
//				.totalQuestions(totalQuestions).totalMarks(totalMarks).examType(test.getExamType().name())
//				.examName(test.getExam() != null ? test.getExam().getExamName() : null).instituteNames(instituteName)
//				.subjectNames(subjectNames).hasSubjectGroups(false).questionsBySubject(null)
//				 .testName(test.getTestName())
//		            .isSubjectFiltered(false)
//				;
//
//		// Set exam-wise or subject-wise specific fields with null checks
//		if (test.isExamWise() && test.getExam() != null) {
//			builder.examId(test.getExam().getId());
//		} else if (test.isSubjectWise() && test.getSubject() != null) {
//			builder.subjectId(test.getSubject().getId()).subjectName(test.getSubject().getName());
//
//			List<String> chapterNames = Optional.ofNullable(test.getChapters()).orElse(Collections.emptyList()).stream()
//					.filter(Objects::nonNull).map(Chapter::getName).filter(Objects::nonNull)
//					.collect(Collectors.toList());
//			builder.chapterNames(chapterNames);
//		}
//
//		TestDto result = builder.build();
//		log.debug("Mapped test DTO for test ID: {} with {} questions", test.getId(), totalQuestions);
//		return result;
//	}

	private TestDto mapToTestDto(Tests test, List<QuestionDto> questionDtos, QuestionDto savedQuestion) {
		// Safe null checks for all collections
		List<String> instituteName = Optional.ofNullable(test.getInstitutes()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Users::getInstituteName).filter(Objects::nonNull)
				.collect(Collectors.toList());

		List<String> subjectNames = Optional.ofNullable(test.getSubjects()).orElse(Collections.emptySet()).stream()
				.filter(Objects::nonNull).map(Subjects::getName).filter(Objects::nonNull).collect(Collectors.toList());

		// Get languages safely
		List<Language> languages = Optional.ofNullable(test.getLanguage()).orElse(Collections.emptyList());

		// Get test language (first language in list or default)
		String testLanguage = languages != null && !languages.isEmpty() ? languages.get(0).toString() : "ENGLISH";

		// Calculate totals safely
		int totalQuestions = questionDtos != null ? questionDtos.size() : 0;
		Double totalMarks = questionDtos != null ? calculateTotalMarks(questionDtos) : 0.0;

		// Get institute IDs
		List<Long> instituteIds = Optional.ofNullable(test.getInstitutes()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Users::getId).filter(Objects::nonNull).collect(Collectors.toList());

		// Get subject IDs
		List<Long> subjectIds = Optional.ofNullable(test.getSubjects()).orElse(Collections.emptySet()).stream()
				.filter(Objects::nonNull).map(Subjects::getId).filter(Objects::nonNull).collect(Collectors.toList());

		// Get chapter names and IDs
		List<String> chapterNames = Optional.ofNullable(test.getChapters()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Chapter::getName).filter(Objects::nonNull).collect(Collectors.toList());

		List<Long> chapterIds = Optional.ofNullable(test.getChapters()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Chapter::getId).filter(Objects::nonNull).collect(Collectors.toList());

		// Build TestDto with all fields
		TestDto.TestDtoBuilder builder = TestDto.builder().id(test.getId()).testName(test.getTestName())
				.correctMark(test.getCorrectMark()).durationMinutes(test.getDurationMinutes()).language(languages) // List
																													// of
																													// languages
																													// supported
																													// by
																													// test
				.testLanguage(testLanguage) // Primary test language
				.negativeMark(test.getNegativeMark()).questions(questionDtos).questionDto(savedQuestion)
				.totalQuestions(totalQuestions).totalMarks(totalMarks).examType(test.getExamType().name())
				.examName(test.getExam() != null ? test.getExam().getExamName() : null).instituteNames(instituteName)
				.instituteIds(instituteIds).subjectNames(subjectNames)
//	            .subjectIds(subjectIds)
				.chapterNames(chapterNames).chapterIds(chapterIds).hasSubjectGroups(false).questionsBySubject(null)
				.isSubjectFiltered(false).testName(test.getTestName());
//	            .createdAt(test.getCreatedAt())
//	            .updatedAt(test.getUpdatedAt());

		// Set exam-wise or subject-wise specific fields with null checks
		if (test.isExamWise() && test.getExam() != null) {
			builder.examId(test.getExam().getId()).examName(test.getExam().getExamName());
		} else if (test.isSubjectWise() && test.getSubject() != null) {
			builder.subjectId(test.getSubject().getId()).subjectName(test.getSubject().getName());

			// For subject-wise tests, also set exam info if available
			if (test.getExam() != null) {
				builder.examId(test.getExam().getId()).examName(test.getExam().getExamName());
			}
		}

		TestDto result = builder.build();
		log.debug("Mapped test DTO for test ID: {} with {} questions, test language: {}", test.getId(), totalQuestions,
				testLanguage);
		return result;
	}

//	private TestDto mapToTestDtoWithOutQuestion(Tests test) {
//		TestDto.TestDtoBuilder builder = TestDto.builder().id(test.getId()).testName(test.getTestName())
//				.correctMark(test.getCorrectMark()).durationMinutes(test.getDurationMinutes())
//				.examName(test.getExam() != null ? test.getExam().getExamName() : null) // FIXED: Check for null exam
//																						// first
//				.language(test.getLanguage()).negativeMark(test.getNegativeMark()).examType(test.getExamType().name());
//
//		// Set exam-wise or subject-wise specific fields
//		if (test.isExamWise() && test.getExam() != null) { // ADDED: null check
//			builder.examId(test.getExam().getId());
//			if (test.getSubjects() != null) {
//				builder.subjectNames(test.getSubjects().stream().map(Subjects::getName).collect(Collectors.toList()));
//			}
//		} else if (test.isSubjectWise()) {
//			builder.subjectId(test.getSubject().getId());
//			builder.subjectName(test.getSubject().getName());
//			if (test.getChapters() != null) {
//				builder.chapterNames(test.getChapters().stream().map(Chapter::getName).collect(Collectors.toList()));
//			}
//		}
//
//		TestDto result = builder.build();
//		log.debug("Mapped test DTO for test ID: {}", test.getId());
//		return result;
//	}

	private TestDto mapToTestDtoWithOutQuestion(Tests test) {
		// Get languages safely
		List<Language> languages = Optional.ofNullable(test.getLanguage()).orElse(Collections.emptyList());

		// Get test language (first language in list or default)
		String testLanguage = languages != null && !languages.isEmpty() ? languages.get(0).toString() : "ENGLISH";

		// Get institute names and IDs
		List<String> instituteNames = Optional.ofNullable(test.getInstitutes()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Users::getInstituteName).filter(Objects::nonNull)
				.collect(Collectors.toList());

		List<Long> instituteIds = Optional.ofNullable(test.getInstitutes()).orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull).map(Users::getId).filter(Objects::nonNull).collect(Collectors.toList());

		// Get subject names and IDs
		List<String> subjectNames = Optional.ofNullable(test.getSubjects()).orElse(Collections.emptySet()).stream()
				.filter(Objects::nonNull).map(Subjects::getName).filter(Objects::nonNull).collect(Collectors.toList());

		List<Long> subjectIds = Optional.ofNullable(test.getSubjects()).orElse(Collections.emptySet()).stream()
				.filter(Objects::nonNull).map(Subjects::getId).filter(Objects::nonNull).collect(Collectors.toList());

		TestDto.TestDtoBuilder builder = TestDto.builder().id(test.getId()).testName(test.getTestName())
				.correctMark(test.getCorrectMark()).durationMinutes(test.getDurationMinutes())
				.examName(test.getExam() != null ? test.getExam().getExamName() : null).language(languages)
				.testLanguage(testLanguage) // Add test language
				.negativeMark(test.getNegativeMark()).examType(test.getExamType().name()).instituteNames(instituteNames)
				.instituteIds(instituteIds).subjectNames(subjectNames);
//	            .subjectIds(subjectIds)
//	            .createdAt(test.getCreatedAt())
//	            .updatedAt(test.getUpdatedAt());

		// Set exam-wise or subject-wise specific fields
		if (test.isExamWise() && test.getExam() != null) {
			builder.examId(test.getExam().getId()).examName(test.getExam().getExamName());
		} else if (test.isSubjectWise()) {
			builder.subjectId(test.getSubject().getId()).subjectName(test.getSubject().getName());

			// For subject-wise tests, also set exam info if available
			if (test.getExam() != null) {
				builder.examId(test.getExam().getId()).examName(test.getExam().getExamName());
			}

			// Add chapter info for subject-wise tests
			if (test.getChapters() != null && !test.getChapters().isEmpty()) {
				List<String> chapterNames = test.getChapters().stream().filter(Objects::nonNull).map(Chapter::getName)
						.filter(Objects::nonNull).collect(Collectors.toList());

				List<Long> chapterIds = test.getChapters().stream().filter(Objects::nonNull).map(Chapter::getId)
						.filter(Objects::nonNull).collect(Collectors.toList());

				builder.chapterNames(chapterNames).chapterIds(chapterIds);
			}
		}

		TestDto result = builder.build();
		log.debug("Mapped test DTO without questions for test ID: {}, test language: {}", test.getId(), testLanguage);
		return result;
	}

	private Double calculateTotalMarks(List<QuestionDto> questions) {
		double total = questions.stream()
				.mapToDouble(question -> question.getMarks() != null ? question.getMarks() : 0.0).sum();
		log.debug("Calculated total marks: {} for {} questions", total, questions.size());
		return total;
	}

	private void validateTest(Tests test) {
		if (test == null) {
			throw new IllegalArgumentException("Test cannot be null");
		}
		if (test.getTestName() == null || test.getTestName().trim().isEmpty()) {
			throw new IllegalArgumentException("Test name is required");
		}
		if (test.getExamType() == null) {
			throw new IllegalArgumentException("Test type is required");
		}
		if (test.isExamWise() && test.getExam() == null) {
			throw new IllegalArgumentException("Exam-wise test must be associated with an exam");
		}
		if (test.isSubjectWise() && test.getSubject() == null) {
			throw new IllegalArgumentException("Subject-wise test must be associated with a subject");
		}
		if (test.getDurationMinutes() == null || test.getDurationMinutes() <= 0) {
			throw new IllegalArgumentException("Test duration must be positive");
		}
		log.debug("Test validation passed for test ID: {}", test.getId());
	}

	private void validateTestDto(TestDto testDto) {
		if (testDto == null) {
			throw new IllegalArgumentException("Test DTO cannot be null");
		}
		if (testDto.getTestName() == null || testDto.getTestName().trim().isEmpty()) {
			throw new IllegalArgumentException("Test name is required");
		}
//		if (testDto.getAll().isEmpty() ||testDto.getInstituteIds() == null || testDto.getInstituteIds().isEmpty()) {
//			throw new IllegalArgumentException("At least one institute is required");
//		}
		// Validate either examId or subjectId is provided
		if (testDto.getExamId() == null && testDto.getSubjectId() == null) {
			throw new IllegalArgumentException("Either examId or subjectId must be provided");
		}
		// Validate not both are provided
		if (testDto.getExamId() != null && testDto.getSubjectId() != null) {
			throw new IllegalArgumentException("Cannot provide both examId and subjectId");
		}
		log.debug("Test DTO validation passed for test: {}", testDto.getTestName());
	}

	private void validateQuestion(Questions question) {
		if (question == null) {
			throw new IllegalArgumentException("Question cannot be null");
		}
		if (question.getQuestionText() == null && question.getQuestionImageUrl() == null) {
			throw new IllegalArgumentException("Question must have either text or image");
		}
		if (question.getMarks() == null || question.getMarks() < 0) {
			throw new IllegalArgumentException("Question marks must be non-negative");
		}
		log.debug("Question validation passed for question ID: {}", question.getId());
	}

	private void validateQuestionDto(QuestionDto questionDto) {
		if (questionDto == null) {
			throw new IllegalArgumentException("Question DTO cannot be null");
		}
		if (questionDto.getQuestionText() == null && questionDto.getQuestionImageUrl() == null) {
			throw new IllegalArgumentException("Question must have either text or image");
		}
		if (questionDto.getMarks() == null || questionDto.getMarks() < 0) {
			throw new IllegalArgumentException("Question marks must be non-negative");
		}
		if (questionDto.getOptions() == null || questionDto.getOptions().isEmpty()) {
			throw new IllegalArgumentException("Question must have at least one option");
		}

		// Validate that exactly one option is correct
		long correctOptionsCount = questionDto.getOptions().stream()
				.filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();

		if (correctOptionsCount != 1) {
			throw new IllegalArgumentException("Question must have exactly one correct option");
		}
		log.debug("Question DTO validation passed");
	}

	private String formatDuration(long minutes) {
		if (minutes < 60) {
			return minutes + " minutes";
		} else {
			long hours = minutes / 60;
			long remainingMinutes = minutes % 60;
			if (remainingMinutes == 0) {
				return hours + " hours";
			} else {
				return hours + " hours " + remainingMinutes + " minutes";
			}
		}
	}

//	// Excel processing methods
//	private List<QuestionDto> readQuestionsFromExcel(MultipartFile excelFile) throws IOException {
//		log.info("Reading questions from Excel file: {}", excelFile.getOriginalFilename());
//		List<QuestionDto> questions = new ArrayList<>();
//
//		try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
//			Sheet sheet = workbook.getSheetAt(0);
//
//			// Validate Excel format
//			validateExcelFormat(sheet);
//
//			// Skip header row and process data rows
//			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//				Row row = sheet.getRow(i);
//				if (row == null)
//					continue;
//
//				QuestionDto questionDto = parseQuestionFromRow(row);
//				if (questionDto != null) {
//					questions.add(questionDto);
//				}
//			}
//		} catch (Exception e) {
//			log.error("Error reading Excel file: {}", e.getMessage(), e);
//			throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
//		}
//
//		log.info("Successfully read {} questions from Excel file", questions.size());
//		return questions;
//	}
//
//	private void validateExcelFormat(Sheet sheet) {
//		Row headerRow = sheet.getRow(0);
//		if (headerRow == null) {
//			throw new IllegalArgumentException("Excel file is empty or invalid");
//		}
//
//		// Basic validation for expected columns
//		String firstHeader = getCellStringValue(headerRow.getCell(0));
//		if (firstHeader == null || !firstHeader.toLowerCase().contains("question")) {
//			throw new IllegalArgumentException("Invalid Excel format. First column should be 'Question'");
//		}
//		log.debug("Excel format validation passed");
//	}
//
//	private QuestionDto parseQuestionFromRow(Row row) {
//		try {
//			String questionText = getCellStringValue(row.getCell(0));
//			if (questionText == null || questionText.trim().isEmpty()) {
//				return null; // Skip empty rows
//			}
//
//			// Default marks since your Excel doesn't have marks column
//			Double marks = 1.0;
//
//			List<OptionDto> options = new ArrayList<>();
//
//			// Parse options (columns 1-4: Options A, B, C, D)
//			for (int i = 1; i <= 4; i++) {
//				String optionText = getCellStringValue(row.getCell(i));
//				if (optionText != null && !optionText.trim().isEmpty()) {
//					int optionNumber = i; // Option numbers: 1, 2, 3, 4
//					options.add(OptionDto.builder().optionText(optionText.trim()).optionNumber(optionNumber)
//							.isCorrect(false) // Will set correct one below
//							.build());
//				}
//			}
//
//			// Get correct option from column 5 (Right Answer)
//			String correctAnswer = getCellStringValue(row.getCell(5));
//			Integer correctOptionNumber = parseCorrectOptionNumber(correctAnswer);
//
//			if (correctOptionNumber != null && !options.isEmpty()) {
//				options.stream().filter(opt -> opt.getOptionNumber().equals(correctOptionNumber)).findFirst()
//						.ifPresent(opt -> opt.setIsCorrect(true));
//			}
//
//			QuestionDto questionDto = QuestionDto.builder().questionText(questionText.trim()).marks(marks)
//					.options(options).build();
//
//			log.debug("Parsed question from row {}: {}", row.getRowNum(), questionText);
//			return questionDto;
//
//		} catch (Exception e) {
//			log.warn("Failed to parse question from row {}: {}", row.getRowNum(), e.getMessage());
//			return null;
//		}
//	}
//
//	private Integer parseCorrectOptionNumber(String correctAnswer) {
//		if (correctAnswer == null)
//			return null;
//
//		try {
//			// Handle both "A"/"B"/"C"/"D" and "1"/"2"/"3"/"4" formats
//			switch (correctAnswer.trim().toUpperCase()) {
//			case "A":
//				return 1;
//			case "B":
//				return 2;
//			case "C":
//				return 3;
//			case "D":
//				return 4;
//			case "E":
//				return 5;
//			case "1":
//				return 1;
//			case "2":
//				return 2;
//			case "3":
//				return 3;
//			case "4":
//				return 4;
//			default:
//				// Try to parse as number
//				return Integer.parseInt(correctAnswer.trim());
//			}
//		} catch (NumberFormatException e) {
//			log.warn("Invalid correct answer format: {}", correctAnswer);
//			return null;
//		}
//	}
//
//	private String getCellStringValue(Cell cell) {
//		if (cell == null)
//			return null;
//
//		switch (cell.getCellType()) {
//		case STRING:
//			return cell.getStringCellValue();
//		case NUMERIC:
//			if (DateUtil.isCellDateFormatted(cell)) {
//				return cell.getDateCellValue().toString();
//			} else {
//				return String.valueOf((long) cell.getNumericCellValue());
//			}
//		case BOOLEAN:
//			return String.valueOf(cell.getBooleanCellValue());
//		case FORMULA:
//			return cell.getCellFormula();
//		default:
//			return null;
//		}
//	}

	private Tests createOrGetTest(TestDto testDto) {
		log.info("Creating or getting test: {}", testDto.getTestName());

		// Validation
//		if (testDto.getInstituteIds() == null || testDto.getInstituteIds().isEmpty()) {
//			throw new IllegalArgumentException("Institute IDs are required");
//		}

//		List<Users> instituteList = userRepository.findAllById(testDto.getInstituteIds());
		List<Users> instituteList = null;
		if (testDto.getInstituteIds().get(0) == 0) {
			instituteList = userRepository.findByRole(Role.valueOf("ADMIN"));
		} else {
			instituteList = userRepository.findAllById(testDto.getInstituteIds());
		}

		if (instituteList.isEmpty()) {
			throw new ResourceNotFoundException("No institutes found with provided IDs");
		}

		Tests test;
		if (testDto.getExamId() != null) {
			// Exam-wise test
			Exam exam = examRepository.findById(testDto.getExamId())
					.orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + testDto.getExamId()));

			test = testRepository.findByTestNameAndExam(testDto.getTestName(), exam).orElse(null);

			if (test == null) {
				test = new Tests();
				test.setTestName(testDto.getTestName());
				test.setExam(exam);
				test.setExamType(ExamType.EXAM_WISE);

				if (testDto.getSubjectsIds() != null && !testDto.getSubjectsIds().isEmpty()) {
					Set<Subjects> subjects = new HashSet<>(subjectRepository.findAllById(testDto.getSubjectsIds()));
					test.setSubjects(subjects);
				} else if (exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
					test.setSubjects(new HashSet<>(exam.getSubjects()));
				}
			}
		} else {
			// Subject-wise test
			Subjects subject = subjectRepository.findById(testDto.getSubjectId()).orElseThrow(
					() -> new ResourceNotFoundException("Subject not found with id: " + testDto.getSubjectId()));

			test = testRepository.findByTestNameAndSubject(testDto.getTestName(), subject).orElse(null);

			if (test == null) {
				test = new Tests();
				test.setTestName(testDto.getTestName());
				test.setSubject(subject);
				test.setExamType(ExamType.SUBJECT_WISE);

				if (testDto.getChapterIds() != null && !testDto.getChapterIds().isEmpty()) {
					List<Chapter> chapters = chapterRepository.findAllById(testDto.getChapterIds());
					test.setChapters(chapters);
				}
			}
		}

		// Update common fields
		updateCommonTestFields(test, testDto, instituteList, null);
		Tests savedTest = testRepository.save(test);
		log.info("Created/got test with ID: {}", savedTest.getId());
		return savedTest;
	}

	private void processQuestionFromExcel(QuestionDto questionDto, Tests test, String language, Set<Long> subjectIds) {
		validateQuestionDto(questionDto);

		Subjects subject = subjectRepository.findById(subjectIds.iterator().next()).orElse(null);

		Questions questionEntity = new Questions();
		questionEntity.setTest(test);
		questionEntity.setQuestionText(questionDto.getQuestionText());
		questionEntity.setMarks(questionDto.getMarks() != null ? questionDto.getMarks() : 1.0);
		questionEntity.setLanguage(Language.valueOf(language));

		questionEntity.setSubject(subject);

		Questions savedQuestion = questionRepository.save(questionEntity);

		// Process options
		if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
			processTextOptions(questionDto.getOptions(), savedQuestion);
		}
		log.info("Processed question from Excel with ID: {}", savedQuestion.getId());
	}

	private void processSingleQuestionDto(QuestionDto questionDto, Tests test) {
		validateQuestionDto(questionDto);

		Questions questionEntity = new Questions();
		questionEntity.setTest(test);
		questionEntity.setQuestionText(questionDto.getQuestionText());
		questionEntity.setQuestionImageUrl(questionDto.getQuestionImageUrl());
		questionEntity.setMarks(questionDto.getMarks() != null ? questionDto.getMarks() : 1.0);

		Questions savedQuestion = questionRepository.save(questionEntity);

		// Process options
		if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
			processTextOptions(questionDto.getOptions(), savedQuestion);
		}
		log.info("Processed single question DTO with ID: {}", savedQuestion.getId());
	}

	@Override
	@Transactional
	public void deleteQuestionFromTest(Long testId, Long questionId) {
		log.info("Deleting question ID: {} from test ID: {}", questionId, testId);

		// Validation
		if (testId == null || testId <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}
		if (questionId == null || questionId <= 0) {
			throw new IllegalArgumentException("Question ID must be a positive number");
		}

		// Verify test exists
		Tests test = testRepository.findById(testId)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

		// Verify question exists and belongs to this test
		Questions question = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

		if (!question.getTest().getId().equals(testId)) {
			throw new IllegalArgumentException("Question does not belong to the specified test");
		}

		// Delete all options associated with this question first
		log.info("Deleting options for question ID: {}", questionId);
		int deletedOptions = optionRepository.deleteByQuestion(question);
		log.info("Deleted {} options for question ID: {}", deletedOptions, questionId);

		// Delete the question
		questionRepository.delete(question);
		log.info("Successfully deleted question ID: {} from test ID: {}", questionId, testId);
	}

	@Override
	public List<TestDto> getAllTestByExamType(String examType) {
		// This method is already implemented in getTestsByExamType
		return getTestsByExamType(examType);
	}

	@Override
	@Transactional
	public QuestionDto updateQuestionFromTest(Long testId, Long questionId, QuestionDto questionDto) {
		log.info("Updating question ID: {} for test ID: {}", questionId, testId);

		// Validation
		if (testId == null || testId <= 0) {
			throw new IllegalArgumentException("Test ID must be a positive number");
		}
		if (questionId == null || questionId <= 0) {
			throw new IllegalArgumentException("Question ID must be a positive number");
		}
		if (questionDto == null) {
			throw new IllegalArgumentException("Question DTO cannot be null");
		}

		// Verify test exists
		Tests test = testRepository.findById(testId)
				.orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

		// Verify question exists and belongs to this test
		Questions existingQuestion = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

		if (!existingQuestion.getTest().getId().equals(testId)) {
			throw new IllegalArgumentException("Question does not belong to the specified test");
		}

		// Update question fields
		boolean isUpdated = false;

		// Update question text if provided
		if (questionDto.getQuestionText() != null && !questionDto.getQuestionText().trim().isEmpty()) {
			existingQuestion.setQuestionText(questionDto.getQuestionText().trim());
			isUpdated = true;
			log.info("Updated question text for question ID: {}", questionId);
		}

		// Update marks if provided
		if (questionDto.getMarks() != null && questionDto.getMarks() >= 0) {
			existingQuestion.setMarks(questionDto.getMarks());
			isUpdated = true;
			log.info("Updated marks to {} for question ID: {}", questionDto.getMarks(), questionId);
		}

		// Update question number if provided
		if (questionDto.getQuestionNumber() != null && questionDto.getQuestionNumber() > 0) {
			existingQuestion.setQuestionNumber(questionDto.getQuestionNumber());
			isUpdated = true;
			log.info("Updated question number to {} for question ID: {}", questionDto.getQuestionNumber(), questionId);
		}

		// Handle question image upload if provided
		if (questionDto.getQuestionImage() != null && !questionDto.getQuestionImage().isEmpty()) {
			log.info("Uploading new question image for question ID: {}", questionId);
			try {
				CloudinaryUploadResponse response = cloudinaryService.uploadFile(questionDto.getQuestionImage(),
						"tests/" + testId + "/questions/" + questionId);
				existingQuestion.setQuestionImageUrl(response.getSecureUrl());
				isUpdated = true;
				log.info("Successfully uploaded question image: {}", response.getSecureUrl());
			} catch (Exception e) {
				log.error("Failed to upload question image for question ID {}: {}", questionId, e.getMessage());
				throw new RuntimeException("Failed to upload question image: " + e.getMessage());
			}
		} else if (questionDto.getQuestionImageUrl() != null) {
			// Direct URL update (if no file upload)
			existingQuestion.setQuestionImageUrl(questionDto.getQuestionImageUrl());
			isUpdated = true;
			log.info("Updated question image URL for question ID: {}", questionId);
		}

		// Update options if provided - FIXED: Added testId and questionId parameters
		if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
			log.info("Updating options for question ID: {}", questionId);
			updateQuestionOptions(existingQuestion, questionDto.getOptions(), testId, questionId);
			isUpdated = true;
			log.info("Successfully updated options for question ID: {}", questionId);
		}

		if (!isUpdated) {
			log.warn("No fields were updated for question ID: {}", questionId);
			throw new IllegalArgumentException("No valid fields provided for update");
		}

		existingQuestion.setUpdatedAt(LocalDateTime.now());
		Questions updatedQuestion = questionRepository.save(existingQuestion);
		log.info("Successfully updated question ID: {} for test ID: {}", questionId, testId);

		// Return the updated question with options
		return mapToQuestionDtoWithOptions(updatedQuestion);
	}

	private void updateQuestionOptions(Questions question, List<OptionDto> optionDtos, Long testId, Long questionId) {
		log.info("Updating options with images for question ID: {}", question.getId());

		// Validate options
		validateOptions(optionDtos);

		// Get existing options for this question
		List<Options> existingOptions = optionRepository.findByQuestion(question);
		Map<Integer, Options> existingOptionsMap = existingOptions.stream()
				.collect(Collectors.toMap(Options::getOptionNumber, opt -> opt));

		List<Options> optionsToSave = new ArrayList<>();
		String optionsFolder = "tests/" + testId + "/questions/" + questionId + "/options";

		for (OptionDto optionDto : optionDtos) {
			Options optionEntity;

			if (existingOptionsMap.containsKey(optionDto.getOptionNumber())) {
				// Update existing option
				optionEntity = existingOptionsMap.get(optionDto.getOptionNumber());
				log.debug("Updating existing option {} for question ID: {}", optionDto.getOptionNumber(),
						question.getId());
			} else {
				// Create new option
				optionEntity = new Options();
				optionEntity.setQuestion(question);
				optionEntity.setOptionNumber(optionDto.getOptionNumber());
				log.debug("Creating new option {} for question ID: {}", optionDto.getOptionNumber(), question.getId());
			}

			// Update option text
			if (optionDto.getOptionText() != null) {
				optionEntity.setOptionText(optionDto.getOptionText());
			}

			// Handle option image upload if provided (using List)
			if (optionDto.getOptionImage() != null && !optionDto.getOptionImage().isEmpty()) {
				// Take the first file from the list
				MultipartFile optionFile = optionDto.getOptionImage().get(0);
				if (optionFile != null && !optionFile.isEmpty()) {
					log.info("Uploading option {} image for question {}", optionDto.getOptionNumber(),
							question.getId());
					try {
						CloudinaryUploadResponse optionResponse = cloudinaryService.uploadFile(optionFile,
								optionsFolder);
						optionEntity.setOptionImageUrl(optionResponse.getSecureUrl());
						log.info("Successfully uploaded option {} image: {}", optionDto.getOptionNumber(),
								optionResponse.getSecureUrl());
					} catch (Exception e) {
						log.error("Failed to upload option image for option {}: {}", optionDto.getOptionNumber(),
								e.getMessage());
						// Continue without throwing exception to allow other updates
					}
				}
			} else if (optionDto.getOptionImageUrl() != null) {
				// Direct URL update
				optionEntity.setOptionImageUrl(optionDto.getOptionImageUrl());
			}

			// Update correctness
			if (optionDto.getIsCorrect() != null) {
				optionEntity.setIsCorrect(optionDto.getIsCorrect());
			}

			optionsToSave.add(optionEntity);
		}

		// Save all options
		List<Options> savedOptions = optionRepository.saveAll(optionsToSave);
		log.info("Successfully saved {} options for question ID: {}", savedOptions.size(), question.getId());

		// Delete options that are no longer in the updated list
		Set<Integer> updatedOptionNumbers = optionDtos.stream().map(OptionDto::getOptionNumber)
				.collect(Collectors.toSet());

		List<Options> optionsToDelete = existingOptions.stream()
				.filter(opt -> !updatedOptionNumbers.contains(opt.getOptionNumber())).collect(Collectors.toList());

		if (!optionsToDelete.isEmpty()) {
			optionRepository.deleteAll(optionsToDelete);
			log.info("Deleted {} old options for question ID: {}", optionsToDelete.size(), question.getId());
		}
	}

	private void validateOptions(List<OptionDto> optionDtos) {
		if (optionDtos == null || optionDtos.isEmpty()) {
			throw new IllegalArgumentException("Options list cannot be empty");
		}

		// Validate that exactly one option is correct
		long correctOptionsCount = optionDtos.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();

		if (correctOptionsCount != 1) {
			throw new IllegalArgumentException(
					"Question must have exactly one correct option. Found: " + correctOptionsCount);
		}

		// Validate option numbers are unique
		Set<Integer> optionNumbers = optionDtos.stream().map(OptionDto::getOptionNumber).collect(Collectors.toSet());

		if (optionNumbers.size() != optionDtos.size()) {
			throw new IllegalArgumentException("Option numbers must be unique. Found duplicates.");
		}

		// Validate option numbers are within reasonable range
		for (OptionDto option : optionDtos) {
			if (option.getOptionNumber() == null || option.getOptionNumber() <= 0) {
				throw new IllegalArgumentException("Option number must be a positive integer");
			}
			if (option.getOptionNumber() > 10) { // Reasonable limit
				throw new IllegalArgumentException("Option number cannot exceed 10");
			}
		}

		log.debug("Options validation passed for {} options", optionDtos.size());
	}

//	@Override
//	public List<TestDto> getTestsByExamAndInstitute(Long examId, Long instituteId) {
//		log.info("Fetching tests for exam ID: {} and institute ID: {}", examId, instituteId);
//
//		// Validate exam exists
//		if (!examRepository.existsById(examId)) {
//			throw new ResourceNotFoundException("Exam not found with id: " + examId);
//		}
//
//		// Validate institute exists
//		if (!userRepository.existsById(instituteId)) {
//			throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
//		}
//
//		List<Tests> tests = testRepository.findByExamIdAndInstitutesId(examId, instituteId);
//		log.info("Found {} tests for exam ID: {} and institute ID: {}", tests.size(), examId, instituteId);
//
//		return tests.stream().map(test -> {
////	                List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
//			return mapToTestDtoWithOutQuestion(test);
//		}).collect(Collectors.toList());
//	}
	
	
	@Override
	public List<TestDto> getTestsByExamAndInstitute(Long examId, Long instituteId) {
	    log.info("Fetching tests for exam ID: {} and institute ID: {}", examId, instituteId);

	    // Validate exam exists
	    if (!examRepository.existsById(examId)) {
	        throw new ResourceNotFoundException("Exam not found with id: " + examId);
	    }

	    // Validate institute exists
	    if (!userRepository.existsById(instituteId)) {
	        throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
	    }

	    List<Tests> tests = testRepository.findByExamIdAndInstitutesId(examId, instituteId);
	    log.info("Found {} tests for exam ID: {} and institute ID: {}", tests.size(), examId, instituteId);

	    return tests.stream().map(test -> {
	        // Get all questions for this test to calculate totals
	        List<Questions> questions = questionRepository.findByTestId(test.getId());
	        
	        // Calculate total marks
	        Double totalMarks = calculateTotalMarksForTest(test, questions);
	        
	        // Get test timing for this institute
	        TestInstituteTimeDto timingDto = getTestTimingForInstitute(test.getId(), instituteId);
	        
	        // Map to DTO with all required fields
	        return mapToTestDtoWithCompleteInfo(test, questions, timingDto);
	    }).collect(Collectors.toList());
	}

	/**
	 * Calculate total marks for a test
	 */
	private Double calculateTotalMarksForTest(Tests test, List<Questions> questions) {
	    if (questions == null || questions.isEmpty()) {
	        return 0.0;
	    }
	    
	    // If questions have individual marks, sum them
	    // Otherwise, use test's correct mark * number of questions
	    boolean hasIndividualMarks = questions.stream()
	            .anyMatch(q -> q.getMarks() != null && q.getMarks() > 0);
	    
	    if (hasIndividualMarks) {
	        return questions.stream()
	                .mapToDouble(q -> q.getMarks() != null ? q.getMarks() : test.getCorrectMark())
	                .sum();
	    } else {
	        return questions.size() * test.getCorrectMark();
	    }
	}

	/**
	 * Get test timing for specific institute
	 */
	private TestInstituteTimeDto getTestTimingForInstitute(Long testId, Long instituteId) {
	    try {
	        return testInstituteTimeRepository.findByTestIdAndInstituteId(testId, instituteId)
	                .map(timing -> {
	                    TestInstituteTimeDto dto = new TestInstituteTimeDto();
	                    BeanUtils.copyProperties(timing, dto);
	                    return dto;
	                })
	                .orElse(null);
	    } catch (Exception e) {
	        log.warn("Could not fetch timing for test {} and institute {}: {}", 
	                testId, instituteId, e.getMessage());
	        return null;
	    }
	}

	/**
	 * Enhanced method to map test to DTO with complete information
	 */
	private TestDto mapToTestDtoWithCompleteInfo(Tests test, List<Questions> questions, 
	                                            TestInstituteTimeDto timingDto) {
	    
	    // Calculate totals
	    int totalQuestions = questions != null ? questions.size() : 0;
	    Double totalMarks = calculateTotalMarksForTest(test, questions);
	    
	    // Get timing information
	    String testStartTime = null;
	    String testEndTime = null;
	    
	    if (timingDto != null) {
	        testStartTime = timingDto.getStarDateTime();
	        testEndTime = timingDto.getEnDateTime();
	    }
	    
	    // Get institute names
	    List<String> instituteNames = Optional.ofNullable(test.getInstitutes())
	            .orElse(Collections.emptyList())
	            .stream()
	            .filter(Objects::nonNull)
	            .map(Users::getInstituteName)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	    
	    // Get subject names
	    List<String> subjectNames = Optional.ofNullable(test.getSubjects())
	            .orElse(Collections.emptySet())
	            .stream()
	            .filter(Objects::nonNull)
	            .map(Subjects::getName)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	    
	    // Get institute IDs
	    List<Long> instituteIds = Optional.ofNullable(test.getInstitutes())
	            .orElse(Collections.emptyList())
	            .stream()
	            .filter(Objects::nonNull)
	            .map(Users::getId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	    
	    // Get languages
	    List<Language> languages = Optional.ofNullable(test.getLanguage())
	            .orElse(Collections.emptyList());
	    
	    String testLanguage = languages != null && !languages.isEmpty() 
	            ? languages.get(0).toString() 
	            : "ENGLISH";
	    
	    // Build the complete TestDto
	    return TestDto.builder()
	            .id(test.getId())
	            .testName(test.getTestName())
	            .durationMinutes(test.getDurationMinutes())
	            .correctMark(test.getCorrectMark())
	            .negativeMark(test.getNegativeMark())
	            .examId(test.getExam() != null ? test.getExam().getId() : null)
	            .examName(test.getExam() != null ? test.getExam().getExamName() : null)
	            .subjectId(test.getSubject() != null ? test.getSubject().getId() : null)
	            .subjectName(test.getSubject() != null ? test.getSubject().getName() : null)
	            .language(languages)
	            .testLanguage(testLanguage)
	            .subjectNames(subjectNames)
	            .totalQuestions(totalQuestions)
	            .totalMarks(totalMarks)
	            .examType(test.getExamType().name())
	            .instituteNames(instituteNames)
	            .instituteIds(instituteIds)
	            .testStartTime(testStartTime)  // Added
	            .testEndTime(testEndTime)      // Added
	            .hasSubjectGroups(false)
	            .isSubjectFiltered(null)
	            .selectedLanguageSubjectId(null)
	            .selectedLanguageSubjectName(null)
	            .isLanguageSubjectSelected(null)
//	            .hasQuestionsForRequestedLanguage(totalQuestions > 0)
	            .build();
	}

	@Override
	public List<TestDto> getTestsByExamId(Long examId) {
		log.info("Fetching all tests for exam ID: {}", examId);

		if (!examRepository.existsById(examId)) {
			throw new ResourceNotFoundException("Exam not found with id: " + examId);
		}

		List<Tests> tests = testRepository.findByExamId(examId);
		log.info("Found {} tests for exam ID: {}", tests.size(), examId);

		return tests.stream().map(test -> {
			List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
			return mapToTestDto(test, questions, null);
		}).collect(Collectors.toList());
	}

	@Override
	public List<TestDto> getTestsByInstituteId(Long instituteId) {
		log.info("Fetching all tests for institute ID: {}", instituteId);

		if (!userRepository.existsById(instituteId)) {
			throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
		}

		List<Tests> tests = testRepository.findByInstitutesId(instituteId);
		log.info("Found {} tests for institute ID: {}", tests.size(), instituteId);

		return tests.stream().map(test -> {
			List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
			return mapToTestDto(test, questions, null);
		}).collect(Collectors.toList());
	}

	@Override
	public List<TestDto> getTestsBySubjectAndInstitute(Long subjectId, Long instituteId) {
		log.info("Fetching tests for subject ID: {} and institute ID: {}", subjectId, instituteId);

		// Validate subject exists
		if (!subjectRepository.existsById(subjectId)) {
			throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
		}

		// Validate institute exists
		if (!userRepository.existsById(instituteId)) {
			throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
		}

		List<Tests> tests = testRepository.findBySubjectIdAndInstitutesId(subjectId, instituteId);
		log.info("Found {} tests for subject ID: {} and institute ID: {}", tests.size(), subjectId, instituteId);

		return tests.stream().map(test -> {
//	                List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
			return mapToTestDtoWithOutQuestion(test);
		}).collect(Collectors.toList());
	}

	public List<StudentListWithTestDto> getAllStudentForInstituteWithTestAttempted(Long instituteId) {

		Users institute = userRepository.findById(instituteId)
				.orElseThrow(() -> new CustomException("Institute Not found", HttpStatus.NOT_FOUND));

		List<Users> studentList = userRepository.findByInstituteEmail(institute.getEmail());
		if (studentList == null) {
			throw new CustomException("No student Found for institute" + institute.getEmail(), HttpStatus.NOT_FOUND);
		}

//		List<Users> testAttemptedStudent = studentList.stream().filter(
//                student -> testResultRepository.existsByStudentId(student.getId())
//       
//				).collect(Collectors.toList());

		List<StudentListWithTestDto> result = studentList.stream().map((student) -> {

			StudentListWithTestDto dto = new StudentListWithTestDto();
			Long count = testResultRepository.countByStudentId(student.getId());
			dto.setStudentId(student.getId());
			dto.setStudentName(student.getName());
			dto.setTestCount(count);
			return dto;

		}).collect(Collectors.toList());

		return result;

	}

//	@Override
//	public List<TestDto> getTestsBySubjectId(Long subjectId) {
//	    log.info("Fetching all tests for subject ID: {}", subjectId);
//	    
//	    if (!subjectRepository.existsById(subjectId)) {
//	        throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
//	    }
//	    
//	    List<Tests> tests = testRepository.findBySubjectId(subjectId);
//	    log.info("Found {} tests for subject ID: {}", tests.size(), subjectId);
//	    
//	    return tests.stream()
//	            .map(test -> {
//	                List<QuestionDto> questions = getAllQuestionsForTest(test.getId());
//	                return mapToTestDto(test, questions, null);
//	            })
//	            .collect(Collectors.toList());
//	}

}