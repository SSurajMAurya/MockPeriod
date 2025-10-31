package com.mockperiod.main.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.multipart.MultipartFile;

import com.mockperiod.main.serviceImpl.FileTypeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.util.Arrays;
import java.util.List;


@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileTypeValidator.class)
public @interface FileType {
    String message() default "Invalid file type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] allowed() default {"png", "jpg", "jpeg", "pdf"};
}

