package com.mockperiod.main.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestListDTO {

	private Long id;
    private String title;
    private String examName;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
}
