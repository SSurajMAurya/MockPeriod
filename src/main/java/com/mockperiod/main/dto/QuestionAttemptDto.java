package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionAttemptDto {
    private Long questionId;
    private Long selectedOptionId;
    private Integer timeSpent; // in seconds (optional)
}
