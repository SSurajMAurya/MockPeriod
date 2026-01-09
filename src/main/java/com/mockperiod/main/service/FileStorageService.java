package com.mockperiod.main.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    String storeFile(MultipartFile file, String folder);
    
    String storeFile(MultipartFile file, String folder, String fileName);
    
    Resource loadFileAsResource(String filePath);
    
    void deleteFile(String filePath);
    
    boolean fileExists(String filePath);
    
    String getFileExtension(String fileName);
    
    String generateFileName(String originalFileName, String prefix);
    
    void validateFile(MultipartFile file, List<String> allowedExtensions, long maxSize);
}
