package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.ChapterDto;
import com.mockperiod.main.entities.Chapter;
import com.mockperiod.main.entities.Subjects;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.repository.ChapterRepository;
import com.mockperiod.main.repository.SubjectRepository;
import com.mockperiod.main.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

	private final ChapterRepository chapterRepository;
	private final SubjectRepository subjectRepository;

	@Override
	public ChapterDto createChapter(ChapterDto chapterDto) {
		// Check if subject exists
		Subjects subject = subjectRepository.findById(chapterDto.getSubjectId()).orElseThrow(
				() -> new ResourceNotFoundException("Subject not found with id: " + chapterDto.getSubjectId()));

		Chapter chapter = new Chapter();
		chapter.setName(chapterDto.getName());
		chapter.setDescription(chapterDto.getDescription());
		chapter.setSubject(subject);

		Chapter savedChapter = chapterRepository.save(chapter);
		return mapToDto(savedChapter);
	}

	@Override
	public ChapterDto getChapterById(Long id) {
		Chapter chapter = chapterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));
		return mapToDto(chapter);
	}

	@Override
	public List<ChapterDto> getAllChapters() {
		List<Chapter> chapters = chapterRepository.findAll();
		return chapters.stream().map(this::mapToDto).collect(Collectors.toList());
	}

	@Override
	public ChapterDto updateChapter(Long id, ChapterDto chapterDto) {
		Chapter existingChapter = chapterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

		// Update fields
		existingChapter.setName(chapterDto.getName());
		existingChapter.setDescription(chapterDto.getDescription());

		// Update subject if provided
		if (chapterDto.getSubjectId() != null) {
			Subjects subject = subjectRepository.findById(chapterDto.getSubjectId()).orElseThrow(
					() -> new ResourceNotFoundException("Subject not found with id: " + chapterDto.getSubjectId()));
			existingChapter.setSubject(subject);
		}

		Chapter updatedChapter = chapterRepository.save(existingChapter);
		return mapToDto(updatedChapter);
	}

	@Override
	public void deleteChapter(Long id) {
		Chapter chapter = chapterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));
		chapterRepository.delete(chapter);
	}

	@Override
	public List<ChapterDto> getChaptersBySubjectId(Long subjectId) {
		// Verify subject exists
		if (!subjectRepository.existsById(subjectId)) {
			throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
		}
        
		 Subjects subjects =  subjectRepository.findById(subjectId)
		.orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
		
		List<Chapter> chapters = chapterRepository.findBySubject(subjects);
		return chapters.stream().map(this::mapToDto).collect(Collectors.toList());
	}

	// Helper method to convert Entity to DTO
	private ChapterDto mapToDto(Chapter chapter) {
		return ChapterDto.builder().id(chapter.getId()).name(chapter.getName())
				.subjectId(chapter.getSubject() != null ? chapter.getSubject().getId() : null)
				.description(chapter.getDescription()).build();
	}
}
