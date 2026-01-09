
package com.mockperiod.main.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockperiod.main.dto.TestDto;
import com.mockperiod.main.dto.TestStatusResponse;
import com.mockperiod.main.dto.OptionDto;
import com.mockperiod.main.dto.QuestionDto;
import com.mockperiod.main.dto.StudentListWithTestDto;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tests") 
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Test Management", description = "APIs for managing tests, questions, and options")
public class TestController {

    private final TestService testService;
    private final ObjectMapper objectMapper;


    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTestWithSingleQuestion(
            @RequestPart("testData") String testDataJson,
            @RequestPart(value = "questionImage", required = false) MultipartFile questionImage,
            @RequestPart(value = "option1File", required = false) MultipartFile option1File,
            @RequestPart(value = "option2File", required = false) MultipartFile option2File,
            @RequestPart(value = "option3File", required = false) MultipartFile option3File,
            @RequestPart(value = "option4File", required = false) MultipartFile option4File,
            @RequestPart(value = "option5File", required = false) MultipartFile option5File) {
        
        try {
            TestDto testDto = objectMapper.readValue(testDataJson, TestDto.class);
            
            // Map option files to their respective numbers
            Map<Integer, MultipartFile> optionFilesMap = new HashMap<>();
            if (option1File != null && !option1File.isEmpty()) optionFilesMap.put(1, option1File);
            if (option2File != null && !option2File.isEmpty()) optionFilesMap.put(2, option2File);
            if (option3File != null && !option3File.isEmpty()) optionFilesMap.put(3, option3File);
            if (option4File != null && !option4File.isEmpty()) optionFilesMap.put(4, option4File);
            if (option5File != null && !option5File.isEmpty()) optionFilesMap.put(5, option5File);
            
            testDto.setQuestion(questionImage);
            testDto.setOptionFilesMap(optionFilesMap);
            
            log.info("Creating test: {} with {} option files", 
                    testDto.getTestName(), optionFilesMap.size());
            
            TestDto createdTest = testService.createOrUpdateTest(testDto);
            return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid JSON format: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating test: " + e.getMessage()));
        }
    }
    

    @GetMapping("/{id}")
    @Operation(
        summary = "Get test by ID",
        description = "Get test details without questions for better performance"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Test retrieved successfully",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid ID format"),
        @ApiResponse(responseCode = "404", description = "Test not found")
    })
    public ResponseEntity<?> getTestById(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long id) {
        try {
            log.info("Fetching test (without questions) with id: {}", id);
            TestDto testDto = testService.getTestById(id);
            return ResponseEntity.ok(testDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test: " + e.getMessage()));
        }
    }
    
	@GetMapping("/institute/{instituteId}/{examType}")
	public ResponseEntity<?> getTestsByInstituteId(@PathVariable Long instituteId , @PathVariable String examType) {
		try {
			log.info("Fetching tests for institute id: {}", instituteId);
			List<TestDto> tests = testService.getTestsByInstituteId(instituteId, examType);
			return ResponseEntity.ok(tests);
		} catch (IllegalArgumentException e) {
			log.warn("Invalid request: {}", e.getMessage());
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		} catch (RuntimeException e) {
			log.warn("No tests found for institute: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("Error fetching tests for institute: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("Error fetching tests for institute: " + e.getMessage()));
		}
	}
    

    @GetMapping("/{id}/with-questions/{language}")
    @Operation(
        summary = "Get test with all questions and options",
        description = "Get complete test details including all questions and their options"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Test with questions retrieved successfully",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Test not found")
    })
    public ResponseEntity<?> getTestWithQuestionsById(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long id,
            @PathVariable String language,
            @RequestParam(required = false) String subjectName
    		) {
        try {
            log.info("Fetching test with questions and options for id: {}", id);
            TestDto testDto = testService.getTestWithQuestionsByIdAndLanguage(id , language , subjectName);
            return ResponseEntity.ok(testDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching test with questions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching test with questions: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all tests",
        description = "Get all tests without questions for better performance"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Tests retrieved successfully",
        content = @Content(schema = @Schema(implementation = TestDto[].class))
    )
    public ResponseEntity<?> getAllTests() {
        try {
            log.info("Fetching all tests (without questions)");
            List<TestDto> tests = testService.getAllTests();
            return ResponseEntity.ok(tests);
        } catch (RuntimeException e) {
            log.warn("No tests found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }

    @GetMapping("/exam-type/{examType}")
    @Operation(
        summary = "Get tests by exam type",
        description = "Get tests filtered by exam type (EXAM_WISE or SUBJECT_WISE)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tests retrieved successfully",
            content = @Content(schema = @Schema(implementation = TestDto[].class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid exam type"),
        @ApiResponse(responseCode = "404", description = "No tests found for the specified type")
    })
    public ResponseEntity<?> getTestsByExamType(
            @Parameter(description = "Exam type (EXAM_WISE or SUBJECT_WISE)", example = "EXAM_WISE", required = true)
            @PathVariable String examType) {
        try {
            log.info("Fetching tests by exam type: {}", examType);
            List<TestDto> tests = testService.getTestsByExamType(examType);
            return ResponseEntity.ok(tests);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid exam type: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.warn("No tests found for exam type: {}", examType);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests by exam type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests by exam type: " + e.getMessage()));
        }
    }

    @GetMapping("/subject-wise/subject/{subjectId}")
    @Operation(
        summary = "Get subject-wise tests by subject",
        description = "Get all subject-wise tests for a specific subject"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Subject-wise tests retrieved successfully",
            content = @Content(schema = @Schema(implementation = TestDto[].class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid subject ID"),
        @ApiResponse(responseCode = "404", description = "No subject-wise tests found")
    })
    public ResponseEntity<?> getSubjectWiseTestsBySubject(
            @Parameter(description = "Subject ID", example = "1", required = true)
            @PathVariable Long subjectId) {
        try {
            log.info("Fetching subject-wise tests for subject id: {}", subjectId);
            List<TestDto> tests = testService.getSubjectWiseTestsBySubject(subjectId);
            return ResponseEntity.ok(tests);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.warn("No subject-wise tests found for subject id: {}", subjectId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching subject-wise tests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching subject-wise tests: " + e.getMessage()));
        }
    }

    @GetMapping("/exam-wise/exam/{examId}")
    @Operation(
        summary = "Get exam-wise tests by exam",
        description = "Get all exam-wise tests for a specific exam"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Exam-wise tests retrieved successfully",
            content = @Content(schema = @Schema(implementation = TestDto[].class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid exam ID"),
        @ApiResponse(responseCode = "404", description = "No exam-wise tests found")
    })
    public ResponseEntity<?> getExamWiseTestsByExam(
            @Parameter(description = "Exam ID", example = "1", required = true)
            @PathVariable Long examId) {
        try {
            log.info("Fetching exam-wise tests for exam id: {}", examId);
            List<TestDto> tests = testService.getExamWiseTestsByExam(examId);
            return ResponseEntity.ok(tests);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.warn("No exam-wise tests found for exam id: {}", examId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching exam-wise tests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching exam-wise tests: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload Excel file to create/update test",
        description = """
            Upload an Excel file to create or update a test with questions.
            
            **Supported Excel Formats:**
            - .xlsx, .xls
            
            **For Exam-wise Test (provide examId):**
            - examId: ID of the exam
            
            **For Subject-wise Test (provide subjectId):**
            - subjectId: ID of the subject
            - chapterIds: Optional list of chapter IDs
            
            **Common Parameters:**
            - file: Excel file containing questions
            - testName: Name of the test
            - instituteIds: List of institute IDs
            - durationMinutes: Test duration (default: 60)
            - correctMark: Marks for correct answer (default: 1.0)
            - negativeMark: Negative marks for wrong answer (default: 0.0)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Excel processed successfully",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type")
    })
    public ResponseEntity<?> uploadExcelFile(
            @Parameter(description = "Excel file (.xlsx, .xls)", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Name of the test", example = "Mathematics Final Exam", required = true)
            @RequestParam String testName,
            
            @Parameter(description = "Exam ID (for exam-wise test)", example = "1")
            @RequestParam(required = false) Long examId,
            
            @Parameter(description = "Subject ID (for subject-wise test)", example = "1")
            @RequestParam(required = false) Long subjectId,
            
            @Parameter(description = "List of chapter IDs (for subject-wise test)", example = "[1, 2, 3]")
            @RequestParam(required = false) List<Long> chapterIds,
            
            @Parameter(description = "List of institute IDs", example = "[1, 2, 3]", required = true)
            @RequestParam List<Long> instituteIds,
            
            @Parameter(description = "Test duration in minutes", example = "60")
            @RequestParam(required = false) Integer durationMinutes,
            
            @Parameter(description = "Marks for correct answer", example = "1.0")
            @RequestParam(required = false) Double correctMark,
            
            @Parameter(description = "Negative marks for wrong answer", example = "0.25")
            @RequestParam(required = false) Double negativeMark) {
        try {
            log.info("Processing Excel file upload: {} for test: {}", file.getOriginalFilename(), testName);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }
            
            if (!isExcelFile(file)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(createErrorResponse("Only Excel files are allowed (.xlsx, .xls)"));
            }

            // Validate either examId or subjectId is provided
            if (examId == null && subjectId == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Either examId or subjectId must be provided"));
            }

            TestDto testDto = TestDto.builder()
                    .testName(testName)
                    .examId(examId)
                    .subjectId(subjectId)
                    .chapterIds(chapterIds)
                    .instituteIds(instituteIds)
                    .durationMinutes(durationMinutes != null ? durationMinutes : 60)
                    .correctMark(correctMark != null ? correctMark : 1.0)
                    .negativeMark(negativeMark != null ? negativeMark : 0.0)
                    .build();

            TestDto result = testService.processExcelFile(file, testDto);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing Excel file: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/create-with-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create test with Excel file and complete test data",
        description = """
            Advanced version - Create test with Excel file and complete test metadata.
            
            **Request Parts:**
            - testData: Complete TestDto in JSON format
            - file: Excel file containing questions and options
            
            **TestDto Structure for Excel Upload:**
            ```json
            {
              "testName": "Physics Midterm",
              "examId": 1,
              "instituteIds": [1, 2],
              "durationMinutes": 90,
              "correctMark": 1.0,
              "negativeMark": 0.33,
              "subjectsIds": [201, 202],
              "language": ["ENGLISH"]
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Test created successfully from Excel",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid JSON or file format")
    })
    public ResponseEntity<?> createTestWithExcel(
            @Parameter(
                description = "Complete test data in JSON format",
                required = true,
                example = "{\"testName\": \"Physics Test\", \"examId\": 1, \"instituteIds\": [1, 2], \"durationMinutes\": 90}"
            )
            @RequestPart("testData") String testDataJson,
            
            @Parameter(description = "Excel file with questions", required = true)
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("Received file: {} with size: {} bytes", 
                    file.getOriginalFilename(), file.getSize());
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }
            
            if (!isExcelFile(file)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(createErrorResponse("Only Excel files are allowed (.xlsx, .xls)"));
            }

            TestDto testDto = objectMapper.readValue(testDataJson, TestDto.class); 
            log.info("Creating test: {}", testDto.getTestName());

            TestDto result = testService.processExcelFile(file, testDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing test data JSON: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid test data format: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating test with Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating test with Excel: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}/questions")
    @Operation(
        summary = "Get questions for a specific test",
        description = "Retrieve all questions for a given test ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Questions retrieved successfully",
        content = @Content(schema = @Schema(implementation = QuestionDto[].class))
    )
    public ResponseEntity<?> getQuestionsByTestId(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long testId) {
        try {
            log.info("Fetching questions for test id: {}", testId);
            List<QuestionDto> questions = testService.getQuestionsByTestId(testId);
            return ResponseEntity.ok(questions);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", testId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching questions for test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching questions for test: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}/institute/{instituteId}/student/{studentId}check-access")
    @Operation(
        summary = "Check test access for institute",
        description = "Check if a test is currently accessible for a specific institute"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Access check completed",
            content = @Content(schema = @Schema(implementation = TestStatusResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Test or institute not found")
    })
    public ResponseEntity<?> checkTestAccess(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long testId,
            @PathVariable Long studentId,
            
            @Parameter(description = "Institute ID", example = "1", required = true)
            @PathVariable Long instituteId) {
        try {
            log.info("Checking test access for testId: {}, instituteId: {}", testId, instituteId);
            TestStatusResponse status = testService.isTestCurrentlyOpen(testId, instituteId,studentId);
            return ResponseEntity.ok(status);
            
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found while checking test access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Resource Not Found",
                    "message", e.getMessage(),
                    "path", "/api/tests/" + testId + "/institute/" + instituteId + "/check-access"
                )
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input while checking test access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Bad Request",
                    "message", e.getMessage(),
                    "path", "/api/tests/" + testId + "/institute/" + instituteId + "/check-access"
                )
            );
        } catch (Exception e) {
            log.error("Unexpected error checking test access: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                    "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", "An unexpected error occurred while checking test access",
                    "path", "/api/tests/" + testId + "/institute/" + instituteId + "/check-access"
                )
            );
        }
    }

    @GetMapping("/exists")
    @Operation(
        summary = "Check if test exists by name and exam",
        description = "Check if an exam-wise test already exists with the given name and exam"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Check completed successfully",
            content = @Content(schema = @Schema(implementation = Boolean.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<?> checkTestExists(
            @Parameter(description = "Test name", example = "Mathematics Final", required = true)
            @RequestParam String testName, 
            
            @Parameter(description = "Exam ID", example = "1", required = true)
            @RequestParam Long examId) {
        try {
            log.info("Checking if test exists: {} for exam: {}", testName, examId);
            boolean exists = testService.testExistsByNameAndExam(testName, examId);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error checking test existence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error checking test existence: " + e.getMessage()));
        }
    }

    @GetMapping("/exists/subject")
    @Operation(
        summary = "Check if test exists by name and subject",
        description = "Check if a subject-wise test already exists with the given name and subject"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Check completed successfully",
            content = @Content(schema = @Schema(implementation = Boolean.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<?> checkTestExistsBySubject(
            @Parameter(description = "Test name", example = "Physics Chapter Test", required = true)
            @RequestParam String testName,
            
            @Parameter(description = "Subject ID", example = "1", required = true)
            @RequestParam Long subjectId) {
        try {
            log.info("Checking if test exists: {} for subject: {}", testName, subjectId);
            boolean exists = testService.testExistsByNameAndSubject(testName, subjectId);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error checking test existence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error checking test existence: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update test",
        description = "Update test details without files"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Test updated successfully",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Test not found")
    })
    public ResponseEntity<?> updateTest(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long id, 
            
            @Parameter(description = "Test data to update", required = true)
            @RequestBody TestDto testDto) {
        try {
            log.info("Updating test with id: {}", id);
            TestDto updatedTest = testService.updateTest(id, testDto);
            return ResponseEntity.ok(updatedTest);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating test: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete test",
        description = "Delete a test and all its associated questions and options"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Test deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid ID"),
        @ApiResponse(responseCode = "404", description = "Test not found")
    })
    public ResponseEntity<?> deleteTest(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long id) {
        try {
            log.info("Deleting test with id: {}", id);
            testService.deleteTest(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting test: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/{testId}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Add questions to existing test",
        description = "Add multiple questions to an existing test"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Questions added successfully",
            content = @Content(schema = @Schema(implementation = TestDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Test not found")
    })
    public ResponseEntity<?> addQuestionsToTest(
            @Parameter(description = "Test ID", example = "1", required = true)
            @PathVariable Long testId, 
            
            @Parameter(description = "List of questions to add", required = true)
            @RequestBody List<QuestionDto> questions) {
        try {
            log.info("Adding {} questions to test id: {}", questions.size(), testId);
            TestDto updatedTest = testService.addQuestionsToTest(testId, questions);
            return ResponseEntity.ok(updatedTest);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Test not found with id: {}", testId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding questions to test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error adding questions to test: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<?> deleteQuestionFromTest(
            @PathVariable Long testId, 
            @PathVariable Long questionId) {
        try {
            log.info("Received request to delete question {} from test {}", questionId, testId);
            
            // Use the basic version (void return)
            testService.deleteQuestionFromTest(testId, questionId);
            
            // Or use the enhanced version (returns details)
            // Map<String, Object> deletionDetails = testService.deleteQuestionFromTest(testId, questionId);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Question deleted successfully",
                "testId", testId,
                "questionId", questionId,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found while deleting question: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request while deleting question: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error deleting question {} from test {}: ", questionId, testId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error deleting question",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @PutMapping(value = "/{testId}/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQuestionInTest(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @RequestPart("questionData") String questionDataJson,
            @RequestPart(value = "questionImage", required = false) MultipartFile questionImage,
            @RequestPart(value = "option1File", required = false) MultipartFile option1File,
            @RequestPart(value = "option2File", required = false) MultipartFile option2File,
            @RequestPart(value = "option3File", required = false) MultipartFile option3File,
            @RequestPart(value = "option4File", required = false) MultipartFile option4File,
            @RequestPart(value = "option5File", required = false) MultipartFile option5File) {
        
        log.info("Received request to update question ID: {} in test ID: {}", questionId, testId);
        
        try {
            // Parse JSON data
            QuestionDto questionDto = objectMapper.readValue(questionDataJson, QuestionDto.class);
            
            // Set question image
            questionDto.setQuestionImage(questionImage);
            
            // Map option files to their respective numbers
            Map<Integer, MultipartFile> optionFilesMap = new HashMap<>();
            if (option1File != null && !option1File.isEmpty()) optionFilesMap.put(1, option1File);
            if (option2File != null && !option2File.isEmpty()) optionFilesMap.put(2, option2File);
            if (option3File != null && !option3File.isEmpty()) optionFilesMap.put(3, option3File);
            if (option4File != null && !option4File.isEmpty()) optionFilesMap.put(4, option4File);
            if (option5File != null && !option5File.isEmpty()) optionFilesMap.put(5, option5File);
            
            // Assign option files to the corresponding options in the DTO
            if (questionDto.getOptions() != null && !optionFilesMap.isEmpty()) {
                for (OptionDto option : questionDto.getOptions()) {
                    if (option.getOptionNumber() != null && optionFilesMap.containsKey(option.getOptionNumber())) {
                        // Create a list with single file and set it
                        List<MultipartFile> fileList = new ArrayList<>();
                        fileList.add(optionFilesMap.get(option.getOptionNumber()));
                        option.setOptionImage(fileList);
                    }
                }
            }
            
            log.info("Updating question with {} option files for test ID: {}", optionFilesMap.size(), testId);
            
            QuestionDto updatedQuestion = testService.updateQuestionFromTest(testId, questionId, questionDto);
            return ResponseEntity.ok(updatedQuestion);
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON for question update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid JSON format: " + e.getMessage()));
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found while updating question: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request while updating question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating question ID: {} in test ID: {}", questionId, testId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating question: " + e.getMessage()));
        }
    }
    
    
    
    @GetMapping("/exam/{examId}/institute/{instituteId}")
    public ResponseEntity<?> getTestsByExamAndInstitute(
            @PathVariable Long examId,
            @PathVariable Long instituteId) {
        try {
            log.info("Request received for tests by exam ID: {} and institute ID: {}", examId, instituteId);
            List<TestDto> tests = testService.getTestsByExamAndInstitute(examId, instituteId);
            return ResponseEntity.ok(tests);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }
    
    
    @GetMapping("/subject/{subjectId}/institute/{instituteId}")
    public ResponseEntity<?> getTestsBySubjectAndInstitute(
            @PathVariable Long subjectId,
            @PathVariable Long instituteId) {
        try {
            log.info("Request received for tests by subject ID: {} and institute ID: {}", subjectId, instituteId);
            List<TestDto> tests = testService.getTestsBySubjectAndInstitute(subjectId, instituteId);
            return ResponseEntity.ok(tests);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }

    // Get tests by exam ID only
    @GetMapping("/exam/{examId}")
    public ResponseEntity<?> getTestsByExamId(@PathVariable Long examId) {
        try {
            log.info("Request received for tests by exam ID: {}", examId);
            List<TestDto> tests = testService.getTestsByExamId(examId);
            return ResponseEntity.ok(tests);
        } catch (ResourceNotFoundException e) {
            log.error("Exam not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests by exam: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }

    // Get tests by institute ID only
    @GetMapping("/institute/{instituteId}")
    public ResponseEntity<?> getTestsByInstituteId(@PathVariable Long instituteId) {
        try {
            log.info("Request received for tests by institute ID: {}", instituteId);
            List<TestDto> tests = testService.getTestsByInstituteId(instituteId);
            return ResponseEntity.ok(tests);
        } catch (ResourceNotFoundException e) {
            log.error("Institute not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests by institute: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }
    
    @GetMapping("/getAllStudentWithTest/{instituteId}")
    public ResponseEntity<?> getAllStudentWithTestDeatls(@PathVariable Long instituteId) {
        try {
            log.info("Request received for tests by institute ID: {}", instituteId); 
            List<StudentListWithTestDto> tests = testService.getAllStudentForInstituteWithTestAttempted(instituteId);
            return ResponseEntity.ok(tests);
        } catch (ResourceNotFoundException e) {
            log.error("Institute not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests by institute: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching tests: " + e.getMessage()));
        }
    }
    
    

    @GetMapping("/upload-health")
    @Operation(
        summary = "File upload health check",
        description = "Check if file upload endpoints are available and get supported formats"
    )
    public ResponseEntity<?> uploadHealthCheck() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "File upload endpoints are available");
            response.put("supportedFileTypes", List.of(".xlsx", ".xls"));
            response.put("maxFileSize", "50MB");
            response.put("supportedTestTypes", List.of("EXAM_WISE", "SUBJECT_WISE"));
            response.put("examWiseRequirements", Map.of(
                "required", List.of("testName", "examId", "instituteIds"),
                "optional", List.of("subjectsIds", "durationMinutes", "correctMark", "negativeMark", "language")
            ));
            response.put("subjectWiseRequirements", Map.of(
                "required", List.of("testName", "subjectId", "instituteIds"),
                "optional", List.of("chapterIds", "durationMinutes", "correctMark", "negativeMark", "language")
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Health check failed: " + e.getMessage()));
        }
    }

    // Utility methods
    private boolean isExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        boolean validContentType = contentType != null && (
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/octet-stream") ||
                contentType.startsWith("application/vnd.ms-excel") ||
                contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                contentType.contains("excel") ||
                contentType.contains("spreadsheet")
        );
        
        boolean validExtension = fileName != null && (
                fileName.toLowerCase().endsWith(".xlsx") ||
                fileName.toLowerCase().endsWith(".xls")
        );
        
        return validContentType || validExtension;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
    
    
    
}