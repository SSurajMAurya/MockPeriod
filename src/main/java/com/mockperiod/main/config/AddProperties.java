package com.mockperiod.main.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AddProperties {
    
    private final File file = new File();
    
    @Data
    public static class File {
        private String uploadDir = "uploads";
        private long maxFileSize = 5242880; // 5MB
        private List<String> allowedExtensions = Arrays.asList("png", "jpg", "jpeg", "pdf", "doc", "docx");
    }
}
