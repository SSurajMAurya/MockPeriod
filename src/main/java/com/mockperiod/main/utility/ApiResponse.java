package com.mockperiod.main.utility;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;
    private PageInfo pageInfo;

    // Success static methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    // Error static methods
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // For paginated responses
    public static <T> ApiResponse<PageResponse<T>> paginated(String message, Page<T> page) {
        PageInfo pageInfo = PageInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();

        PageResponse<T> pageResponse = PageResponse.<T>builder()
                .content(page.getContent())
                .pageInfo(pageInfo)
                .build();

        return ApiResponse.<PageResponse<T>>builder()
                .success(true)
                .message(message)
                .data(pageResponse)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
