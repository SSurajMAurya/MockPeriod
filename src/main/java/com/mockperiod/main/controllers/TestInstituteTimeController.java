package com.mockperiod.main.controllers;
import com.mockperiod.main.dto.TestInstituteTimeDto;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.service.TestInstituteTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-institute-timings")
@RequiredArgsConstructor
public class TestInstituteTimeController {

    private final TestInstituteTimeService testInstituteTimeService;

//    @PostMapping
//    public ResponseEntity<?> createTestInstituteTime(
//            @Valid @RequestBody TestInstituteTimeDto testInstituteTimeDto) {
//        try {
//            TestInstituteTimeDto createdTiming = testInstituteTimeService.creaTestInstituteTime(testInstituteTimeDto);
//            return ResponseEntity.status(HttpStatus.CREATED).body(createdTiming);
//            
//        } catch (CustomException e) {
//            // Re-throw CustomException to let global exception handler handle it
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException("Failed to create test institute timing: " + e.getMessage(), 
//                                   HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    
        
        @PostMapping
        public ResponseEntity<?> createOrUpdateTestTiming(@RequestBody TestInstituteTimeDto testInstituteTimeDto) {
            try {
                TestInstituteTimeDto result = testInstituteTimeService.createOrUpdateTestInstituteTime(testInstituteTimeDto);
                return ResponseEntity.ok(result);
            } catch (CustomException e) {
               throw new CustomException("Error creating or updating test timing :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        @GetMapping("/institute/{instituteId}/test/{testId}")
        public ResponseEntity<?> getTestTiming(@PathVariable Long instituteId, @PathVariable Long testId) {
            try {
                TestInstituteTimeDto result = testInstituteTimeService.getTestInstituteTime(instituteId, testId);
                return ResponseEntity.ok(result);
            } catch (CustomException e) {
            	 throw new CustomException("Error creating or updating test timing", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
//        @DeleteMapping("/institute/{instituteId}/test/{testId}")
//        public ResponseEntity<?> deleteTestTiming(@PathVariable Long instituteId, @PathVariable Long testId) {
//            try {
//                testInstituteTimeService.deleteTestInstituteTime(instituteId, testId);
//                return ResponseEntity.ok(createSuccessResponse("Test timing deleted successfully"));
//            } catch (ResourceNotFoundException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
//            }
//        }
//        
//        private Map<String, Object> createErrorResponse(String message) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", false);
//            response.put("message", message);
//            response.put("timestamp", LocalDateTime.now());
//            return response;
//        }
//        
//        private Map<String, Object> createSuccessResponse(String message) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", message);
//            response.put("timestamp", LocalDateTime.now());
//            return response;
//        }
//    }
    
}
