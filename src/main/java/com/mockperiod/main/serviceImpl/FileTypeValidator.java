package com.mockperiod.main.serviceImpl;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.mockperiod.main.service.FileType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//Validator implementation
public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {
	private List<String> allowedTypes;

	@Override
	public void initialize(FileType constraintAnnotation) {
		this.allowedTypes = Arrays.asList(constraintAnnotation.allowed());
	}

	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
		if (file == null || file.isEmpty()) {
			return true; // Use @NotNull for required validation
		}

		String fileExtension = getFileExtension(file.getOriginalFilename());
		return allowedTypes.contains(fileExtension.toLowerCase());
	}

	private String getFileExtension(String filename) {
		if (filename == null || filename.lastIndexOf(".") == -1) {
			return "";
		}
		return filename.substring(filename.lastIndexOf(".") + 1);
	}

}
