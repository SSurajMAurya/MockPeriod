package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestStatusResponse {
    private Long testId;
    private Long instituteId;
    private Long studentId; // Added field
    private boolean isOpen;
    private String status;
    private String message;
    private LocalDateTime currentTime;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;
    private Long timeRemainingMinutes;
    private String timeRemainingFormatted;
    
    // Status constants for reference
    public static class Status {
        public static final String OPEN = "OPEN";
        public static final String SCHEDULED = "SCHEDULED";
        public static final String ENDED = "ENDED";
        public static final String NOT_ASSIGNED = "NOT_ASSIGNED";
        public static final String NO_TIMING_SET = "NO_TIMING_SET";
        public static final String ALREADY_ATTEMPTED = "ALREADY_ATTEMPTED";
    }
}
