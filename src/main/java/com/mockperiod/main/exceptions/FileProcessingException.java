package com.mockperiod.main.exceptions;

/**
 * Exception thrown when file processing operations fail.
 * This can include file upload, parsing, validation, or any file-related operations.
 */
public class FileProcessingException extends RuntimeException {
    
    /**
     * Constructs a new FileProcessingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public FileProcessingException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new FileProcessingException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new FileProcessingException for unsupported file types.
     *
     * @param fileName the name of the file that caused the exception
     * @param supportedTypes the supported file types
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException unsupportedFileType(String fileName, String... supportedTypes) {
        String supported = String.join(", ", supportedTypes);
        String message = String.format("Unsupported file type for '%s'. Supported types: %s", fileName, supported);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for file size exceeded.
     *
     * @param fileName the name of the file that caused the exception
     * @param actualSize the actual file size
     * @param maxSize the maximum allowed file size
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException fileSizeExceeded(String fileName, long actualSize, long maxSize) {
        String message = String.format("File '%s' size (%d bytes) exceeds maximum allowed size (%d bytes)", 
                fileName, actualSize, maxSize);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for file parsing errors.
     *
     * @param fileName the name of the file that caused the exception
     * @param fileType the type of file being parsed (e.g., "Excel", "CSV")
     * @param details additional details about the parsing error
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException parsingError(String fileName, String fileType, String details) {
        String message = String.format("Failed to parse %s file '%s': %s", fileType, fileName, details);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for file upload failures.
     *
     * @param fileName the name of the file that failed to upload
     * @param reason the reason for the upload failure
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException uploadFailed(String fileName, String reason) {
        String message = String.format("Failed to upload file '%s': %s", fileName, reason);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for file corruption.
     *
     * @param fileName the name of the corrupted file
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException corruptedFile(String fileName) {
        String message = String.format("File '%s' appears to be corrupted or in an invalid format", fileName);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for empty files.
     *
     * @param fileName the name of the empty file
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException emptyFile(String fileName) {
        String message = String.format("File '%s' is empty", fileName);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for missing required files.
     *
     * @param fileField the name of the file field that is required
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException missingFile(String fileField) {
        String message = String.format("Required file '%s' is missing", fileField);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for Excel-specific errors.
     *
     * @param fileName the name of the Excel file
     * @param sheetName the name of the sheet causing the error
     * @param details additional details about the Excel error
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException excelError(String fileName, String sheetName, String details) {
        String message = String.format("Excel processing error in file '%s', sheet '%s': %s", 
                fileName, sheetName, details);
        return new FileProcessingException(message);
    }
    
    /**
     * Constructs a new FileProcessingException for image processing errors.
     *
     * @param fileName the name of the image file
     * @param details additional details about the image processing error
     * @return a FileProcessingException with a formatted message
     */
    public static FileProcessingException imageProcessingError(String fileName, String details) {
        String message = String.format("Image processing error for file '%s': %s", fileName, details);
        return new FileProcessingException(message);
    }
}
