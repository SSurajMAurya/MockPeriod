////package com.mockperiod.main.serviceImpl;
////
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.time.LocalTime;
////import java.time.format.DateTimeFormatter;
////import java.time.format.DateTimeParseException;
////
////import org.springframework.beans.BeanUtils;
////import org.springframework.http.HttpStatus;
////import org.springframework.stereotype.Service;
////
////import com.mockperiod.main.dto.TestInstituteTimeDto;
////import com.mockperiod.main.entities.TestInstituteTime;
////import com.mockperiod.main.exceptions.CustomException;
////import com.mockperiod.main.exceptions.ResourceNotFoundException;
////import com.mockperiod.main.repository.TestInstituteTimeRepository;
////import com.mockperiod.main.repository.TestRepository;
////import com.mockperiod.main.repository.UserRepository;
////import com.mockperiod.main.service.TestInstituteTimeService;
////
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////
////@Slf4j
////@Service
////@RequiredArgsConstructor
////public class TestInstituteTimeServiceImpl implements TestInstituteTimeService {
////    
////    private final TestInstituteTimeRepository testInstituteTimeRepository;
////    private final TestRepository testRepository;
////    private final UserRepository userRepository;
////    
////    // Multiple formatters for different input formats
////    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
////    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
////    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////
////    @Override
////    public TestInstituteTimeDto creaTestInstituteTime(TestInstituteTimeDto testInstituteTimeDto) {
////        try {
////            log.info("Creating test institute timing for testId: {}, instituteId: {}", 
////                    testInstituteTimeDto.getTestId(), testInstituteTimeDto.getIntituteId());
////
////            // Validate test exists
////            testRepository.findById(testInstituteTimeDto.getTestId())
////                .orElseThrow(() -> new ResourceNotFoundException(
////                    "No test found with given Id: " + testInstituteTimeDto.getTestId()));
////
////            // Validate institute exists
////            userRepository.findById(testInstituteTimeDto.getIntituteId())
////                .orElseThrow(() -> new ResourceNotFoundException(
////                    "No institute found with given Id: " + testInstituteTimeDto.getIntituteId()));
////
////            // Parse date times with flexible format handling
////            LocalDateTime startTime = parseFlexibleDateTime(testInstituteTimeDto.getStarDateTime());
////            LocalDateTime endTime = parseFlexibleDateTime(testInstituteTimeDto.getEnDateTime());
////
////            // Validate date logic
////            if (startTime.isAfter(endTime)) {
////                throw new CustomException("Start date time cannot be after end date time", 
////                                        HttpStatus.BAD_REQUEST);
////            }
////
////            if (startTime.isBefore(LocalDateTime.now())) {
////                throw new CustomException("Start date time cannot be in the past", 
////                                        HttpStatus.BAD_REQUEST);
////            }
////
////            // Create and save entity
////            TestInstituteTime testInstituteTime = new TestInstituteTime();
////            testInstituteTime.setIntituteId(testInstituteTimeDto.getIntituteId());
////            testInstituteTime.setTestId(testInstituteTimeDto.getTestId());
////            testInstituteTime.setStarDateTime(startTime);
////            testInstituteTime.setEnDateTime(endTime);
////
////            TestInstituteTime savedInstituteTime = testInstituteTimeRepository.save(testInstituteTime);
////            log.info("Test institute timing created successfully with ID: {}", savedInstituteTime.getId());
////
////            // Convert to DTO
////            TestInstituteTimeDto responseDto = new TestInstituteTimeDto();
////            responseDto.setEnDateTime(savedInstituteTime.getEnDateTime().toString());
////            responseDto.setStarDateTime(savedInstituteTime.getStarDateTime().toString());
////            BeanUtils.copyProperties(savedInstituteTime, responseDto);
////            
////            return responseDto;
////
////        } catch (CustomException | ResourceNotFoundException e) {
////            log.error("Error creating test institute timing: {}", e.getMessage());
////            throw e;
////        } catch (Exception e) {
////            log.error("Unexpected error creating test institute timing: ", e);
////            throw new CustomException("An unexpected error occurred while creating test institute timing: " + e.getMessage(), 
////                                   HttpStatus.INTERNAL_SERVER_ERROR);
////        }
////    }
////
////    /**
////     * Flexible date-time parser that handles both date-time and date-only formats
////     */
////    private LocalDateTime parseFlexibleDateTime(String dateTimeString) {
////        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
////            throw new CustomException("Date time string cannot be null or empty", HttpStatus.BAD_REQUEST);
////        }
////
////        try {
////            // Try parsing as full date-time (ISO format)
////            return LocalDateTime.parse(dateTimeString, ISO_DATE_TIME_FORMATTER);
////        } catch (DateTimeParseException e1) {
////            try {
////                // Try parsing as date-only (ISO format) and set default time
////                LocalDate date = LocalDate.parse(dateTimeString, ISO_DATE_FORMATTER);
////                return LocalDateTime.of(date, LocalTime.MIDNIGHT); // Set to 00:00:00
////            } catch (DateTimeParseException e2) {
////                try {
////                    // Try parsing as date-only (custom format)
////                    LocalDate date = LocalDate.parse(dateTimeString, CUSTOM_DATE_FORMATTER);
////                    return LocalDateTime.of(date, LocalTime.MIDNIGHT);
////                } catch (DateTimeParseException e3) {
////                    throw new CustomException(
////                        "Invalid date format: '" + dateTimeString + "'. " +
////                        "Supported formats: \n" +
////                        "- Full date-time: 2025-11-12T09:00:00 \n" +
////                        "- Date only: 2025-11-12 (will be set to 00:00:00)", 
////                        HttpStatus.BAD_REQUEST
////                    );
////                }
////            }
////        }
////    }
////}
//
//
//
//
//package com.mockperiod.main.serviceImpl;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.Optional;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.mockperiod.main.dto.TestInstituteTimeDto;
//import com.mockperiod.main.entities.TestInstituteTime;
//import com.mockperiod.main.exceptions.CustomException;
//import com.mockperiod.main.exceptions.ResourceNotFoundException;
//import com.mockperiod.main.repository.TestInstituteTimeRepository;
//import com.mockperiod.main.repository.TestRepository;
//import com.mockperiod.main.repository.UserRepository;
//import com.mockperiod.main.service.TestInstituteTimeService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TestInstituteTimeServiceImpl implements TestInstituteTimeService {
//    
//    private final TestInstituteTimeRepository testInstituteTimeRepository;
//    private final TestRepository testRepository;
//    private final UserRepository userRepository;
//    
//    // Multiple formatters for different input formats
//    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private static final DateTimeFormatter DATE_TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
//    private static final DateTimeFormatter TIME_ONLY_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");
//
//    @Override
//    @Transactional
//    public TestInstituteTimeDto creaTestInstituteTime(TestInstituteTimeDto testInstituteTimeDto) {
//        try {
//            log.info("Creating test institute timing for testId: {}, instituteId: {}", 
//                    testInstituteTimeDto.getTestId(), testInstituteTimeDto.getIntituteId());
//
//            // Validate test exists
//            testRepository.findById(testInstituteTimeDto.getTestId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                    "No test found with given Id: " + testInstituteTimeDto.getTestId()));
//
//            // Validate institute exists
//            userRepository.findById(testInstituteTimeDto.getIntituteId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                    "No institute found with given Id: " + testInstituteTimeDto.getIntituteId()));
//
//            // Parse date times with flexible format handling
//            LocalDateTime startTime = parseFlexibleDateTime(testInstituteTimeDto.getStarDateTime(), "start");
//            LocalDateTime endTime = parseFlexibleDateTime(testInstituteTimeDto.getEnDateTime(), "end");
//
//            // Validate date logic
//            validateTimingLogic(startTime, endTime);
//
//            // Check for duplicate/overlapping timings
//            checkForDuplicateTiming(testInstituteTimeDto.getIntituteId(), 
//                                  testInstituteTimeDto.getTestId(), 
//                                  startTime, endTime);
//
//            // Create and save entity
//            TestInstituteTime testInstituteTime = new TestInstituteTime();
//            testInstituteTime.setIntituteId(testInstituteTimeDto.getIntituteId());
//            testInstituteTime.setTestId(testInstituteTimeDto.getTestId());
//            testInstituteTime.setStarDateTime(startTime);
//            testInstituteTime.setEnDateTime(endTime);
//
//            TestInstituteTime savedInstituteTime = testInstituteTimeRepository.save(testInstituteTime);
//            log.info("Test institute timing created successfully with ID: {}", savedInstituteTime.getId());
//
//            // Convert to DTO
//            TestInstituteTimeDto responseDto = new TestInstituteTimeDto();
//            BeanUtils.copyProperties(savedInstituteTime, responseDto);
//            responseDto.setStarDateTime(formatDateTimeForResponse(savedInstituteTime.getStarDateTime()));
//            responseDto.setEnDateTime(formatDateTimeForResponse(savedInstituteTime.getEnDateTime()));
//            
//            return responseDto;
//
//        } catch (DataIntegrityViolationException e) {
//            log.error("Duplicate test institute timing found: {}", e.getMessage());
//            throw new CustomException("A test timing already exists for this institute and time slot. Please choose a different time or update the existing one.", 
//                                   HttpStatus.CONFLICT);
//        } catch (CustomException | ResourceNotFoundException e) {
//            log.error("Error creating test institute timing: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error creating test institute timing: ", e);
//            throw new CustomException("An unexpected error occurred while creating test institute timing: " + e.getMessage(), 
//                                   HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * Flexible date-time parser that handles multiple formats including time
//     */
//    private LocalDateTime parseFlexibleDateTime(String dateTimeString, String fieldName) {
//        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
//            throw new CustomException(fieldName + " date time cannot be null or empty", HttpStatus.BAD_REQUEST);
//        }
//
//        String trimmedString = dateTimeString.trim();
//
//        try {
//            // Try parsing as full date-time (ISO format) - "2025-11-28T14:30:00"
//            return LocalDateTime.parse(trimmedString, ISO_DATE_TIME_FORMATTER);
//        } catch (DateTimeParseException e1) {
//            try {
//                // Try parsing as date-time with space separator - "2025-11-28 14:30:00"
//                return LocalDateTime.parse(trimmedString, DATE_TIME_FORMATTER);
//            } catch (DateTimeParseException e2) {
//                try {
//                    // Try parsing as date-time without seconds - "2025-11-28 14:30"
//                    return LocalDateTime.parse(trimmedString, DATE_TIME_FORMATTER_SHORT);
//                } catch (DateTimeParseException e3) {
//                    // Try parsing as separate date and time components
//                    return parseSeparateDateAndTime(trimmedString, fieldName);
//                }
//            }
//        }
//    }
//
//    /**
//     * Handle cases where date and time might be separate or in different formats
//     */
//    private LocalDateTime parseSeparateDateAndTime(String dateTimeString, String fieldName) {
//        String[] parts = dateTimeString.split(" ");
//        
//        if (parts.length == 1) {
//            // Only date provided - set to start of day for start time, end of day for end time
//            try {
//                LocalDate date = LocalDate.parse(dateTimeString, DATE_ONLY_FORMATTER);
//                if ("start".equals(fieldName)) {
//                    return LocalDateTime.of(date, LocalTime.MIN); // 00:00:00
//                } else {
//                    return LocalDateTime.of(date, LocalTime.MAX); // 23:59:59.999999999
//                }
//            } catch (DateTimeParseException e) {
//                throw createDateTimeFormatException(dateTimeString);
//            }
//        } else if (parts.length == 2) {
//            // Date and time provided separately
//            try {
//                LocalDate date = LocalDate.parse(parts[0], DATE_ONLY_FORMATTER);
//                LocalTime time = parseTimeString(parts[1]);
//                return LocalDateTime.of(date, time);
//            } catch (DateTimeParseException e) {
//                throw createDateTimeFormatException(dateTimeString);
//            }
//        } else {
//            throw createDateTimeFormatException(dateTimeString);
//        }
//    }
//
//    /**
//     * Parse time string in various formats
//     */
//    private LocalTime parseTimeString(String timeString) {
//        try {
//            return LocalTime.parse(timeString, TIME_ONLY_FORMATTER);
//        } catch (DateTimeParseException e1) {
//            try {
//                return LocalTime.parse(timeString, TIME_ONLY_FORMATTER_SHORT);
//            } catch (DateTimeParseException e2) {
//                throw new CustomException("Invalid time format: '" + timeString + "'. Use HH:mm:ss or HH:mm", 
//                                       HttpStatus.BAD_REQUEST);
//            }
//        }
//    }
//
//    /**
//     * Validate timing business logic
//     */
//    private void validateTimingLogic(LocalDateTime startTime, LocalDateTime endTime) {
//        if (startTime.isAfter(endTime)) {
//            throw new CustomException("Start date time cannot be after end date time", 
//                                    HttpStatus.BAD_REQUEST);
//        }
//
//        if (startTime.isBefore(LocalDateTime.now())) {
//            throw new CustomException("Start date time cannot be in the past", 
//                                    HttpStatus.BAD_REQUEST);
//        }
//
//        // Validate that the test duration is reasonable (not too short or too long)
//        long durationInMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
//        if (durationInMinutes < 5) {
//            throw new CustomException("Test duration must be at least 5 minutes", 
//                                    HttpStatus.BAD_REQUEST);
//        }
//
//        if (durationInMinutes > 24 * 60) { // 24 hours
//            throw new CustomException("Test duration cannot exceed 24 hours", 
//                                    HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    /**
//     * Check for duplicate or overlapping timings
//     */
//    private void checkForDuplicateTiming(Long instituteId, Long testId, LocalDateTime startTime, LocalDateTime endTime) {
//        Optional<TestInstituteTime> existingTiming = testInstituteTimeRepository
//            .findByIntituteIdAndTestIdAndTimeRange(instituteId, testId, startTime, endTime);
//        
//        if (existingTiming.isPresent()) {
//            throw new CustomException("A test timing already exists for this institute that overlaps with the specified time slot", 
//                                    HttpStatus.CONFLICT);
//        }
//    }
//
//    /**
//     * Format DateTime for response
//     */
//    private String formatDateTimeForResponse(LocalDateTime dateTime) {
//        return dateTime.format(ISO_DATE_TIME_FORMATTER);
//    }
//
//    /**
//     * Create consistent format exception
//     */
//    private CustomException createDateTimeFormatException(String input) {
//        return new CustomException(
//            "Invalid date format: '" + input + "'. " +
//            "Supported formats: \n" +
//            "- Full date-time: 2025-11-28T14:30:00 or 2025-11-28 14:30:00 \n" +
//            "- Date and time: 2025-11-28 14:30 \n" +
//            "- Date only: 2025-11-28 (will be set to start/end of day)", 
//            HttpStatus.BAD_REQUEST
//        );
//    }
//}



package com.mockperiod.main.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mockperiod.main.dto.TestInstituteTimeDto;
import com.mockperiod.main.entities.TestInstituteTime;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.TestInstituteTimeRepository;
import com.mockperiod.main.repository.TestRepository;
import com.mockperiod.main.repository.UserRepository;
import com.mockperiod.main.service.TestInstituteTimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestInstituteTimeServiceImpl implements TestInstituteTimeService {
    
    private final TestInstituteTimeRepository testInstituteTimeRepository;
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    
    // Multiple formatters for different input formats
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public TestInstituteTimeDto createOrUpdateTestInstituteTime(TestInstituteTimeDto testInstituteTimeDto) {
        try {
            log.info("Creating/Updating test institute timing for testId: {}, instituteId: {}", 
                    testInstituteTimeDto.getTestId(), testInstituteTimeDto.getIntituteId());

            // Validate test exists
            testRepository.findById(testInstituteTimeDto.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No test found with given Id: " + testInstituteTimeDto.getTestId()));

            // Validate institute exists
            userRepository.findById(testInstituteTimeDto.getIntituteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No institute found with given Id: " + testInstituteTimeDto.getIntituteId()));

            // Parse date times with flexible format handling
            LocalDateTime startTime = parseFlexibleDateTime(testInstituteTimeDto.getStarDateTime(), "start");
            LocalDateTime endTime = parseFlexibleDateTime(testInstituteTimeDto.getEnDateTime(), "end");

            // Validate date logic
            validateTimingLogic(startTime, endTime);

            // Check if timing already exists for this test and institute
            Optional<TestInstituteTime> existingTiming = testInstituteTimeRepository
                .findByIntituteIdAndTestId(testInstituteTimeDto.getIntituteId(), testInstituteTimeDto.getTestId());

            TestInstituteTime testInstituteTime;
            boolean isUpdate = false;

            if (existingTiming.isPresent()) {
                // Update existing timing
                log.info("Updating existing test institute timing with ID: {}", existingTiming.get().getId());
                testInstituteTime = existingTiming.get();
                isUpdate = true;
                
                // Check for overlapping timings with other records (excluding current one)
                checkForOverlappingTimings(existingTiming.get().getId(), 
                                         testInstituteTimeDto.getIntituteId(), 
                                         testInstituteTimeDto.getTestId(), 
                                         startTime, endTime);
            } else {
                // Create new timing
                log.info("Creating new test institute timing");
                testInstituteTime = new TestInstituteTime();
                
                // Check for overlapping timings for new record
                checkForOverlappingTimings(null, 
                                         testInstituteTimeDto.getIntituteId(), 
                                         testInstituteTimeDto.getTestId(), 
                                         startTime, endTime);
            }

            // Set properties
            testInstituteTime.setIntituteId(testInstituteTimeDto.getIntituteId());
            testInstituteTime.setTestId(testInstituteTimeDto.getTestId());
            testInstituteTime.setStarDateTime(startTime);
            testInstituteTime.setEnDateTime(endTime);

            TestInstituteTime savedInstituteTime = testInstituteTimeRepository.save(testInstituteTime);
            log.info("Test institute timing {} successfully with ID: {}", 
                    isUpdate ? "updated" : "created", savedInstituteTime.getId());

            // Convert to DTO
            TestInstituteTimeDto responseDto = new TestInstituteTimeDto();
            BeanUtils.copyProperties(savedInstituteTime, responseDto);
            responseDto.setStarDateTime(formatDateTimeForResponse(savedInstituteTime.getStarDateTime()));
            responseDto.setEnDateTime(formatDateTimeForResponse(savedInstituteTime.getEnDateTime()));
            
            return responseDto;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving test institute timing: {}", e.getMessage());
            throw new CustomException("A test timing conflict occurred. Please check the provided data.", 
                                   HttpStatus.CONFLICT);
        } catch (CustomException | ResourceNotFoundException e) {
            log.error("Error creating/updating test institute timing: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating/updating test institute timing: ", e);
            throw new CustomException("An unexpected error occurred while saving test institute timing: " + e.getMessage(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TestInstituteTimeDto getTestInstituteTime(Long instituteId, Long testId) {
        try {
            log.info("Fetching test institute timing for instituteId: {}, testId: {}", instituteId, testId);

            TestInstituteTime testInstituteTime = testInstituteTimeRepository
                .findByIntituteIdAndTestId(instituteId, testId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No test timing found for instituteId: " + instituteId + " and testId: " + testId));

            TestInstituteTimeDto responseDto = new TestInstituteTimeDto();
            BeanUtils.copyProperties(testInstituteTime, responseDto);
            responseDto.setStarDateTime(formatDateTimeForResponse(testInstituteTime.getStarDateTime()));
            responseDto.setEnDateTime(formatDateTimeForResponse(testInstituteTime.getEnDateTime()));

            log.info("Successfully fetched test institute timing with ID: {}", testInstituteTime.getId());
            return responseDto;

        } catch (ResourceNotFoundException e) {
            log.error("Test institute timing not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching test institute timing: ", e);
            throw new CustomException("An unexpected error occurred while fetching test institute timing: " + e.getMessage(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteTestInstituteTime(Long instituteId, Long testId) {
        try {
            log.info("Deleting test institute timing for instituteId: {}, testId: {}", instituteId, testId);

            TestInstituteTime testInstituteTime = testInstituteTimeRepository
                .findByIntituteIdAndTestId(instituteId, testId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No test timing found for instituteId: " + instituteId + " and testId: " + testId));

            testInstituteTimeRepository.delete(testInstituteTime);
            log.info("Successfully deleted test institute timing with ID: {}", testInstituteTime.getId());

        } catch (ResourceNotFoundException e) {
            log.error("Test institute timing not found for deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting test institute timing: ", e);
            throw new CustomException("An unexpected error occurred while deleting test institute timing: " + e.getMessage(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByInstituteAndTest(Long instituteId, Long testId) {
        return testInstituteTimeRepository.existsByIntituteIdAndTestId(instituteId, testId);
    }

    /**
     * Flexible date-time parser that handles multiple formats including time
     */
    private LocalDateTime parseFlexibleDateTime(String dateTimeString, String fieldName) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            throw new CustomException(fieldName + " date time cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        String trimmedString = dateTimeString.trim();

        try {
            // Try parsing as full date-time (ISO format) - "2025-11-28T14:30:00"
            return LocalDateTime.parse(trimmedString, ISO_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing as date-time with space separator - "2025-11-28 14:30:00"
                return LocalDateTime.parse(trimmedString, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e2) {
                try {
                    // Try parsing as date-time without seconds - "2025-11-28 14:30"
                    return LocalDateTime.parse(trimmedString, DATE_TIME_FORMATTER_SHORT);
                } catch (DateTimeParseException e3) {
                    // Try parsing as separate date and time components
                    return parseSeparateDateAndTime(trimmedString, fieldName);
                }
            }
        }
    }

    /**
     * Handle cases where date and time might be separate or in different formats
     */
    private LocalDateTime parseSeparateDateAndTime(String dateTimeString, String fieldName) {
        String[] parts = dateTimeString.split(" ");
        
        if (parts.length == 1) {
            // Only date provided - set to start of day for start time, end of day for end time
            try {
                LocalDate date = LocalDate.parse(dateTimeString, DATE_ONLY_FORMATTER);
                if ("start".equals(fieldName)) {
                    return LocalDateTime.of(date, LocalTime.MIN); // 00:00:00
                } else {
                    return LocalDateTime.of(date, LocalTime.MAX); // 23:59:59.999999999
                }
            } catch (DateTimeParseException e) {
                throw createDateTimeFormatException(dateTimeString);
            }
        } else if (parts.length == 2) {
            // Date and time provided separately
            try {
                LocalDate date = LocalDate.parse(parts[0], DATE_ONLY_FORMATTER);
                LocalTime time = parseTimeString(parts[1]);
                return LocalDateTime.of(date, time);
            } catch (DateTimeParseException e) {
                throw createDateTimeFormatException(dateTimeString);
            }
        } else {
            throw createDateTimeFormatException(dateTimeString);
        }
    }

    /**
     * Parse time string in various formats
     */
    private LocalTime parseTimeString(String timeString) {
        try {
            return LocalTime.parse(timeString, TIME_ONLY_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, TIME_ONLY_FORMATTER_SHORT);
            } catch (DateTimeParseException e2) {
                throw new CustomException("Invalid time format: '" + timeString + "'. Use HH:mm:ss or HH:mm", 
                                       HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * Validate timing business logic
     */
    private void validateTimingLogic(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new CustomException("Start date time cannot be after end date time", 
                                    HttpStatus.BAD_REQUEST);
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new CustomException("Start date time cannot be in the past", 
                                    HttpStatus.BAD_REQUEST);
        }

        // Validate that the test duration is reasonable (not too short or too long)
        long durationInMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationInMinutes < 5) {
            throw new CustomException("Test duration must be at least 5 minutes", 
                                    HttpStatus.BAD_REQUEST);
        }

        if (durationInMinutes > 24 * 60) { // 24 hours
            throw new CustomException("Test duration cannot exceed 24 hours", 
                                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Check for overlapping timings (excluding the current record if updating)
     */
    private void checkForOverlappingTimings(Long excludeId, Long instituteId, Long testId, 
                                          LocalDateTime startTime, LocalDateTime endTime) {
        Optional<TestInstituteTime> overlappingTiming = testInstituteTimeRepository
            .findOverlappingTiming(excludeId, instituteId, testId, startTime, endTime);
        
        if (overlappingTiming.isPresent()) {
            throw new CustomException(
                "A test timing already exists for this institute that overlaps with the specified time slot. " +
                "Please choose a different time range.", 
                HttpStatus.CONFLICT
            );
        }
    }

    /**
     * Format DateTime for response
     */
    private String formatDateTimeForResponse(LocalDateTime dateTime) {
        return dateTime.format(ISO_DATE_TIME_FORMATTER);
    }

    /**
     * Create consistent format exception
     */
    private CustomException createDateTimeFormatException(String input) {
        return new CustomException(
            "Invalid date format: '" + input + "'. " +
            "Supported formats: \n" +
            "- Full date-time: 2025-11-28T14:30:00 or 2025-11-28 14:30:00 \n" +
            "- Date and time: 2025-11-28 14:30 \n" +
            "- Date only: 2025-11-28 (will be set to start/end of day)", 
            HttpStatus.BAD_REQUEST
        );
    }
}