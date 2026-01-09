package com.mockperiod.main.service;

import com.mockperiod.main.dto.ChapterDto;
import java.util.List;

public interface ChapterService {
	ChapterDto createChapter(ChapterDto chapterDto);

	ChapterDto getChapterById(Long id);

	List<ChapterDto> getAllChapters();

	ChapterDto updateChapter(Long id, ChapterDto chapterDto);

	void deleteChapter(Long id);

	List<ChapterDto> getChaptersBySubjectId(Long subjectId);
}
