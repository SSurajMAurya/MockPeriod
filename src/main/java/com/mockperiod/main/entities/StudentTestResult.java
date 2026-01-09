package com.mockperiod.main.entities;
//
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//public class StudentTestResult {
//	
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//	
//	private Long testId;
//	
//	private String testName;
//	
//	private Long studentId;
//	
//	private String studentEmail;
//	
//	private String instituteEmail;
//	
//	private Double noOfCorrectAns;
//	
////	 @Column(columnDefinition = "TEXT")
////	private List<Long> currectquestionIds;
//	
//	private Double noOfInCorrectAns;
//	
//	private Double noOfUnAnsweredQuestion;
//	
////	private List<Long> incurrectquestionIds;
//	
//	private Double totalObtainedMarks;
//	
//	private LocalDate testAttemptedDate;
//	  @ElementCollection
//	  
//	    @Column(name = "question_id")
//	    private List<Long> currectquestionIds;
//	    
//	    @ElementCollection
//	    
//	    @Column(name = "question_id")
//	    private List<Long> incurrectquestionIds;
//	
//	
//	private String remark;
//
//}

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_test_result")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentTestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long testId;
    private String testName;
    private Long studentId;
    private String studentEmail;
    private String instituteEmail;
    private Double noOfCorrectAns;
    private Double noOfInCorrectAns;
    private Double noOfUnAnsweredQuestion;
    private Double totalObtainedMarks;
    private LocalDate testAttemptedDate;
    private String remark;
    private String timeSpent;
    
    private Language attemptedTestLanguage;
    
    @Column(name = "selected_language_subject_id")
    private Long selectedLanguageSubjectId;

    // Correct @ElementCollection usage
    @ElementCollection
    @CollectionTable(
        name = "student_correct_questions",
        joinColumns = @JoinColumn(name = "test_result_id")
    )
    @Column(name = "question_id")
    private List<Long> currectquestionIds;
    
    @ElementCollection
    @CollectionTable(
        name = "student_incorrect_questions", 
        joinColumns = @JoinColumn(name = "test_result_id")
    )
    @Column(name = "question_id")
    private List<Long> incurrectquestionIds;
    
}