
package com.mockperiod.main.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "tests")
public class Tests {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String testName;
	private Integer durationMinutes;
	private Double correctMark;
	private Double negativeMark;

	// For subject-wise tests
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id")
	private Subjects subject;

	// For chapters in subject-wise tests
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "test_chapters", joinColumns = @JoinColumn(name = "test_id"), inverseJoinColumns = @JoinColumn(name = "chapter_id"))
	@Builder.Default
	private List<Chapter> chapters = new ArrayList<>();

	// For exam-wise tests - multiple subjects
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "tests_subjects", joinColumns = @JoinColumn(name = "test_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
	@Builder.Default
	private Set<Subjects> subjects = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "test_languages", joinColumns = @JoinColumn(name = "test_id"))
	@Column(name = "language")
	@Builder.Default
	private List<Language> language = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "test_institutes", joinColumns = @JoinColumn(name = "test_id"), inverseJoinColumns = @JoinColumn(name = "institute_id"))
	@Builder.Default
	private List<Users> institutes = new ArrayList<>();

	// For exam-wise tests
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	@Nullable()
	private Exam exam;

//    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @Builder.Default
//    private List<Questions> questions = new ArrayList<>();

	@OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Builder.Default
	private Set<Questions> questions = new HashSet<>();

	@Enumerated(EnumType.STRING)
	private ExamType examType;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private Integer totalQuestion;
	private Double totalMarks;

	// Helper methods
	public boolean isExamWise() {
		return ExamType.EXAM_WISE.equals(this.examType);
	}

	public boolean isSubjectWise() {
		return ExamType.SUBJECT_WISE.equals(this.examType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tests tests = (Tests) o;
		return Objects.equals(id, tests.id);
	}
}
