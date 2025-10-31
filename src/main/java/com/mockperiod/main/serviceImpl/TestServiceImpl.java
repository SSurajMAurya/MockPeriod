package com.mockperiod.main.serviceImpl;

import org.springframework.stereotype.Service;

import com.mockperiod.main.repository.ChapterRepository;
import com.mockperiod.main.repository.ExamRepository;
import com.mockperiod.main.repository.QuestionRepository;
import com.mockperiod.main.repository.TestRepository;
import com.mockperiod.main.service.TestService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TestServiceImpl implements TestService {
    
    private final TestRepository testRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;
    private final TestMapper testMapper;
    private final QuestionMapper questionMapper;
    
    public TestServiceImpl(TestRepository testRepository, ExamRepository examRepository,
                         QuestionRepository questionRepository, ChapterRepository chapterRepository,
                         FileStorageService fileStorageService, TestMapper testMapper,
                         QuestionMapper questionMapper) {
        this.testRepository = testRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.chapterRepository = chapterRepository;
        this.fileStorageService = fileStorageService;
        this.testMapper = testMapper;
        this.questionMapper = questionMapper;
    }
    
    @Override
    public TestResponseDTO createTest(CreateTestRequest request) {
        log.info("Creating new test with title: {}", request.getTitle());
        
        // Validate exam exists
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", request.getExamId()));
        
        // Validate chapter if provided
        if (request.getChapterId() != null) {
            chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", request.getChapterId()));
        }
        
        // Create test entity
        Test test = testMapper.toEntity(request);
        test.setExam(exam);
        test.setCreatedAt(LocalDateTime.now());
        test.setUpdatedAt(LocalDateTime.now());
        
        Test savedTest = testRepository.save(test);
        log.info("Test created successfully with ID: {}", savedTest.getId());
        
        return testMapper.toDTO(savedTest);
    }
    
    @Override
    public TestResponseDTO getTestById(Long testId) {
        log.debug("Fetching test by ID: {}", testId);
        
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        
        return testMapper.toDTO(test);
    }
    
    @Override
    public Page<TestResponseDTO> getAllTests(TestFilter filter, Pageable pageable) {
        log.debug("Fetching tests with filter: {}", filter);
        
        Specification<Test> specification = buildTestSpecification(filter);
        Page<Test> tests = testRepository.findAll(specification, pageable);
        
        return tests.map(testMapper::toDTO);
    }
    
    @Override
    public TestResponseDTO updateTest(Long testId, UpdateTestRequest request) {
        log.info("Updating test with ID: {}", testId);
        
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        
        testMapper.updateEntityFromDTO(request, test);
        test.setUpdatedAt(LocalDateTime.now());
        
        Test updatedTest = testRepository.save(test);
        
        return testMapper.toDTO(updatedTest);
    }
    
    @Override
    public void deleteTest(Long testId) {
        log.info("Deleting test with ID: {}", testId);
        
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        
        testRepository.delete(test);
        log.info("Test deleted successfully with ID: {}", testId);
    }
    
    @Override
    public TestResponseDTO addQuestionToTest(Long testId, CreateQuestionRequest request) {
        log.info("Adding question to test ID: {}", testId);
        
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        
        // This would typically call QuestionService
        Question question = questionMapper.toEntity(request);
        question.setTest(test);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        
        Question savedQuestion = questionRepository.save(question);
        test.getQuestions().add(savedQuestion);
        
        return testMapper.toDTO(test);
    }
    
    @Override
    public List<QuestionResponseDTO> getTestQuestions(Long testId) {
        log.debug("Fetching questions for test ID: {}", testId);
        
        List<Question> questions = questionRepository.findByTestIdOrderByQuestionNumber(testId);
        
        return questions.stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TestSummaryDTO getTestSummary(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        
        long totalQuestions = questionRepository.countByTestId(testId);
        long questionsWithImages = questionRepository.countByTestIdAndQuestionImageUrlIsNotNull(testId);
        
        return TestSummaryDTO.builder()
                .testId(testId)
                .title(test.getTitle())
                .totalQuestions(totalQuestions)
                .questionsWithImages(questionsWithImages)
                .totalMarks(calculateTotalMarks(testId))
                .createdAt(test.getCreatedAt())
                .build();
    }
    
    private Specification<Test> buildTestSpecification(TestFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filter.getExamId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("exam").get("id"), filter.getExamId()));
            }
            
            if (filter.getSubjectCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectCategory"), filter.getSubjectCategory()));
            }
            
            if (filter.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private Double calculateTotalMarks(Long testId) {
        // Implementation to calculate total marks
        return questionRepository.sumMarksByTestId(testId);
    }
}
