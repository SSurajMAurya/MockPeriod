package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.ChapterDto;
import com.mockperiod.main.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

	private final ChapterService chapterService;

	@PostMapping
	public ResponseEntity<ChapterDto> createChapter(@Valid @RequestBody ChapterDto chapterDto) {
		ChapterDto createdChapter = chapterService.createChapter(chapterDto);
		return new ResponseEntity<>(createdChapter, HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ChapterDto> getChapterById(@PathVariable Long id) {
		ChapterDto chapterDto = chapterService.getChapterById(id);
		return ResponseEntity.ok(chapterDto);
	}

	@GetMapping
	public ResponseEntity<List<ChapterDto>> getAllChapters() {
		List<ChapterDto> chapters = chapterService.getAllChapters();
		return ResponseEntity.ok(chapters);
	}

	@GetMapping("/subject/{subjectId}")
	public ResponseEntity<List<ChapterDto>> getChaptersBySubjectId(@PathVariable Long subjectId) {
		List<ChapterDto> chapters = chapterService.getChaptersBySubjectId(subjectId);
		return ResponseEntity.ok(chapters);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ChapterDto> updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterDto chapterDto) {
		ChapterDto updatedChapter = chapterService.updateChapter(id, chapterDto);
		return ResponseEntity.ok(updatedChapter);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
		chapterService.deleteChapter(id);
		return ResponseEntity.noContent().build();
	}
}