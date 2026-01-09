package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryUploadResponse {
    private String publicId;
    private String version;
    private String signature;
    private Integer width;
    private Integer height;
    private String format;
    private String resourceType;
    private Long bytes;  // Changed to Long to handle both Integer and Long
    private String type;
    private String url;
    private String secureUrl;
    private String createdAt;
}