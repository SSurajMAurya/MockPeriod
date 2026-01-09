//package com.mockperiod.main.serviceImpl;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import com.mockperiod.main.dto.CloudinaryUploadResponse;
//
//import lombok.extern.slf4j.Slf4j;
//
//
//@Service
//@Slf4j
//public class CloudinaryService {
//
//    private final Cloudinary cloudinary;
//    
////    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
//
//    public CloudinaryService(Cloudinary cloudinary) {
//        this.cloudinary = cloudinary;
//    }
//
//    /**
//     * Upload a file to Cloudinary
//     */
//    public CloudinaryUploadResponse uploadFile(MultipartFile file, String folder) {
//        try {
//            // Validate file
//            if (file.isEmpty()) {
//                throw new IllegalArgumentException("File is empty");
//            }
//
//            // Convert MultipartFile to File
//            File uploadedFile = convertMultiPartToFile(file);
//            
//            // Prepare upload parameters
//            Map<String, Object> uploadParams = new HashMap<>();
//            uploadParams.put("folder", folder);
//            uploadParams.put("resource_type", "auto"); // auto-detect image, video, etc.
//            uploadParams.put("quality", "auto:good"); // optimize quality
//            uploadParams.put("fetch_format", "auto"); // auto-format
//            
//            // Upload to Cloudinary
//            Map<?, ?> uploadResult = cloudinary.uploader().upload(uploadedFile, uploadParams);
//            
//            // Clean up temporary file
//            uploadedFile.delete();
//            
//            // Convert response to DTO
//            return mapUploadResultToResponse(uploadResult);
//            
//        } catch (Exception e) {
//            log.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Upload multiple files
//     */
//    public List<CloudinaryUploadResponse> uploadMultipleFiles(MultipartFile[] files, String folder) {
//        return Arrays.stream(files)
//                .map(file -> uploadFile(file, folder))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Delete file from Cloudinary
//     */
//    public boolean deleteFile(String publicId) {
//        try {
//            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
//            return "ok".equals(result.get("result"));
//        } catch (Exception e) {
//            log.error("Error deleting file from Cloudinary: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//
//    /**
//     * Update/replace a file
//     */
//    public CloudinaryUploadResponse updateFile(String publicId, MultipartFile newFile, String folder) {
//        try {
//            // First delete the old file
//            deleteFile(publicId);
//            
//            // Then upload the new file
//            return uploadFile(newFile, folder);
//            
//        } catch (Exception e) {
//            log.error("Error updating file in Cloudinary: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to update file: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Generate signed URL for direct upload from frontend
//     */
//    public Map<String, String> generateUploadSignature(String folder) {
//        try {
//            long timestamp = System.currentTimeMillis() / 1000;
//            
//            Map<String, Object> params = new HashMap<>();
//            params.put("folder", folder);
//            params.put("timestamp", timestamp);
//            
//            String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);
//            
//            Map<String, String> result = new HashMap<>();
//            result.put("signature", signature);
//            result.put("timestamp", String.valueOf(timestamp));
//            result.put("api_key", cloudinary.config.apiKey);
//            result.put("cloud_name", cloudinary.config.cloudName);
//            result.put("folder", folder);
//            
//            return result;
//        } catch (Exception e) {
//            log.error("Error generating upload signature: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to generate upload signature", e);
//        }
//    }
//
//    /**
//     * Convert MultipartFile to File
//     */
//    private File convertMultiPartToFile(MultipartFile file) throws IOException {
//        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
//        FileOutputStream fos = new FileOutputStream(convFile);
//        fos.write(file.getBytes());
//        fos.close();
//        return convFile;
//    }
//
//    /**
//     * Map Cloudinary upload result to response DTO
//     */
//    private CloudinaryUploadResponse mapUploadResultToResponse(Map<?, ?> uploadResult) {
//        CloudinaryUploadResponse response = new CloudinaryUploadResponse();
//        response.setPublicId((String) uploadResult.get("public_id"));
//        response.setUrl((String) uploadResult.get("url"));
//        response.setSecureUrl((String) uploadResult.get("secure_url"));
//        response.setFormat((String) uploadResult.get("format"));
//        response.setBytes((Long) uploadResult.get("bytes"));
//        
//        if (uploadResult.get("width") != null) {
//            response.setWidth(Integer.parseInt(uploadResult.get("width").toString()));
//        }
//        if (uploadResult.get("height") != null) {
//            response.setHeight(Integer.parseInt(uploadResult.get("height").toString()));
//        }
//        
//        response.setResourceType((String) uploadResult.get("resource_type"));
//        return response;
//    }
//
//    /**
//     * Validate file type
//     */
//    public boolean isValidImageFile(MultipartFile file) {
//        String contentType = file.getContentType();
//        return contentType != null && 
//               (contentType.startsWith("image/") || 
//                contentType.equals("application/octet-stream"));
//    }
//
//    /**
//     * Validate file size (max 10MB)
//     */
//    public boolean isValidFileSize(MultipartFile file) {
//        return file.getSize() <= 10 * 1024 * 1024; // 10MB
//    }
//}



package com.mockperiod.main.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mockperiod.main.dto.CloudinaryUploadResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadResponse uploadFile(MultipartFile file, String folder) {
        try {
            log.info("Uploading file: {} to folder: {}", file.getOriginalFilename(), folder);
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    ));
            
            log.info("Upload result: {}", uploadResult);
            return mapUploadResultToResponse(uploadResult);
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public boolean isValidFileSize(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024; // 10MB
        return file.getSize() <= maxSize;
    }

    public void deleteFile(String imageUrl) {
        try {
            if (imageUrl != null) {
                // Extract public ID from URL
                String publicId = extractPublicIdFromUrl(imageUrl);
                if (publicId != null) {
                    Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("File deleted successfully: {}", result);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
            // Don't throw exception for deletion failures
        }
    }

    private CloudinaryUploadResponse mapUploadResultToResponse(Map<?, ?> uploadResult) {
        try {
            log.debug("Mapping upload result: {}", uploadResult);
            
            // Safely handle different number types (Integer/Long)
            Object bytesObj = uploadResult.get("bytes");
            Long bytes = null;
            if (bytesObj != null) {
                if (bytesObj instanceof Integer) {
                    bytes = ((Integer) bytesObj).longValue();
                } else if (bytesObj instanceof Long) {
                    bytes = (Long) bytesObj;
                }
            }

            // Safely handle created_at timestamp
            Object createdAtObj = uploadResult.get("created_at");
            String createdAt = null;
            if (createdAtObj != null) {
                createdAt = createdAtObj.toString();
            }

            return CloudinaryUploadResponse.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .version(getSafeString(uploadResult.get("version")))
                    .signature(getSafeString(uploadResult.get("signature")))
                    .width(getSafeInteger(uploadResult.get("width")))
                    .height(getSafeInteger(uploadResult.get("height")))
                    .format(getSafeString(uploadResult.get("format")))
                    .resourceType(getSafeString(uploadResult.get("resource_type")))
                    .bytes(bytes)
                    .type(getSafeString(uploadResult.get("type")))
                    .url(getSafeString(uploadResult.get("url")))
                    .secureUrl(getSafeString(uploadResult.get("secure_url")))
                    .createdAt(createdAt)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error mapping upload result: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process upload response: " + e.getMessage(), e);
        }
    }

    // Helper methods to safely handle type conversions
    private String getSafeString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Integer getSafeInteger(Object value) {
        if (value == null) return null;
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse integer from string: {}", value);
                return null;
            }
        }
        return null;
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/cloudname/image/upload/v1234567/public_id.jpg
            if (imageUrl.contains("/upload/")) {
                String[] parts = imageUrl.split("/upload/");
                if (parts.length > 1) {
                    String afterUpload = parts[1];
                    // Remove version part if exists (v1234567/)
                    if (afterUpload.contains("/v")) {
                        afterUpload = afterUpload.substring(afterUpload.indexOf("/v") + 1);
                        afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
                    }
                    // Remove file extension
                    if (afterUpload.contains(".")) {
                        afterUpload = afterUpload.substring(0, afterUpload.lastIndexOf("."));
                    }
                    return afterUpload;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract public ID from URL: {}", imageUrl);
            return null;
        }
    }
}