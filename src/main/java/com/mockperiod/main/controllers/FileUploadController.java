//package com.mockperiod.main.controllers;
//
//@RestController
//@RequestMapping("/api/upload")
//@CrossOrigin(origins = "*")
//public class FileUploadController {
//
//    private final CloudinaryService cloudinaryService;
//
//    public FileUploadController(CloudinaryService cloudinaryService) {
//        this.cloudinaryService = cloudinaryService;
//    }
//
//    @PostMapping("/image")
//    public ResponseEntity<FileUploadResponse> uploadImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "folder", defaultValue = "general") String folder) {
//        
//        try {
//            // Validate file
//            if (!cloudinaryService.isValidImageFile(file)) {
//                return ResponseEntity.badRequest()
//                    .body(FileUploadResponse.error("Invalid file type. Only images are allowed."));
//            }
//
//            if (!cloudinaryService.isValidFileSize(file)) {
//                return ResponseEntity.badRequest()
//                    .body(FileUploadResponse.error("File size too large. Maximum size is 10MB."));
//            }
//
//            // Upload file
//            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadFile(file, folder);
//            
//            return ResponseEntity.ok()
//                .body(FileUploadResponse.success("File uploaded successfully", uploadResponse));
//                
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(FileUploadResponse.error("Upload failed: " + e.getMessage()));
//        }
//    }
//
//    @PostMapping("/multiple")
//    public ResponseEntity<FileUploadResponse> uploadMultipleImages(
//            @RequestParam("files") MultipartFile[] files,
//            @RequestParam(value = "folder", defaultValue = "general") String folder) {
//        
//        try {
//            // Validate all files
//            for (MultipartFile file : files) {
//                if (!cloudinaryService.isValidImageFile(file)) {
//                    return ResponseEntity.badRequest()
//                        .body(FileUploadResponse.error("Invalid file type in one or more files."));
//                }
//                if (!cloudinaryService.isValidFileSize(file)) {
//                    return ResponseEntity.badRequest()
//                        .body(FileUploadResponse.error("One or more files exceed size limit."));
//                }
//            }
//
//            // Upload files
//            List<CloudinaryUploadResponse> uploadResponses = 
//                cloudinaryService.uploadMultipleFiles(files, folder);
//            
//            // You can modify this to return list of responses
//            return ResponseEntity.ok()
//                .body(FileUploadResponse.success(
//                    files.length + " files uploaded successfully", 
//                    uploadResponses.get(0) // returning first one for simplicity
//                ));
//                
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(FileUploadResponse.error("Upload failed: " + e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{publicId}")
//    public ResponseEntity<FileUploadResponse> deleteImage(@PathVariable String publicId) {
//        try {
//            boolean deleted = cloudinaryService.deleteFile(publicId);
//            
//            if (deleted) {
//                return ResponseEntity.ok()
//                    .body(FileUploadResponse.success("File deleted successfully", null));
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(FileUploadResponse.error("Failed to delete file"));
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(FileUploadResponse.error("Delete failed: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/signature")
//    public ResponseEntity<Map<String, String>> getUploadSignature(
//            @RequestParam(value = "folder", defaultValue = "general") String folder) {
//        
//        try {
//            Map<String, String> signature = cloudinaryService.generateUploadSignature(folder);
//            return ResponseEntity.ok(signature);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", "Failed to generate signature"));
//        }
//    }
//}
