
package com.mockperiod.main.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mockperiod.main.entities.Language;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class TestDto {

    private Long id;
    private List<Long> instituteIds;
    private String testName;
    private Integer durationMinutes;
    private Double correctMark;
    private Double negativeMark;
    
    private String testStartTime;
    private String testEndTime;
    
    // For exam-wise test
    private Long examId;
    private String examName;
    
    // For subject-wise test
    private Long subjectId;
    private List<Long> chapterIds;
    
    private Set<Long> subjectsIds; // Keep for backward compatibility
    private List<Language> language;
    private String testLanguage;
    
    private List<QuestionDto> questions;
    private QuestionDto questionDto;
    private String questionImageUrl;
    private List<String> optionImagesUrls;

    private transient MultipartFile excelFile;
    private transient MultipartFile question;
    private List<MultipartFile> optionFiles;
    
    private boolean isCorrect;

    // For response
    private List<String> subjectNames;
    private Integer totalQuestions;
    private Double totalMarks;
    
    private List<SubjectSectionDto> subjectGroups;
    private Boolean hasSubjectGroups;
    
    
    private Long filteredSubjectId;
    private String filteredSubjectName;
    private Boolean isSubjectFiltered;
    
    private Long selectedLanguageSubjectId;
    private String selectedLanguageSubjectName;
    private Boolean isLanguageSubjectSelected;
    
    private boolean HasQuestionsForRequestedLanguage;
    
    // Helper method to check if has subject groups
    public Boolean getHasSubjectGroups() {
        return hasSubjectGroups != null ? hasSubjectGroups : false;
    }
    
    private String examType; // EXAM_WISE or SUBJECT_WISE
    private String subjectName; // For subject-wise tests
    private List<String> chapterNames; // For subject-wise tests
    
    
//    private transient List<MultipartFile> optionFiles;
    private transient Map<Integer, MultipartFile> optionFilesMap;
    
    private List<String> instituteNames;
    
 private Map<String, List<QuestionDto>> questionsBySubject;
 
 
    
  

}


