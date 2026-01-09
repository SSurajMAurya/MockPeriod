package com.mockperiod.main.service;

import com.mockperiod.main.dto.SubjectDto;
import com.mockperiod.main.dto.SubjectFEDto;

import java.util.List;

public interface SubjectService {
	SubjectDto createSubject(SubjectDto subjectDto);

	SubjectDto getSubjectById(Long id);

	List<SubjectDto> getAllSubjects();

	SubjectDto updateSubject(Long id, SubjectDto subjectDto);

	void deleteSubject(Long id);

	List<SubjectDto> getSubjectsByExamId(Long examId);
	
	List<SubjectFEDto> getAllSubjectByChapter();
//
//	SubjectDto addSubjectToExam(Long subjectId, Long examId);
//
//	SubjectDto removeSubjectFromExam(Long subjectId, Long examId);
}
