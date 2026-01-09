package com.mockperiod.main.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_rankings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRanking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rank_position")
	private Integer rank;

	@Column(name = "test_id", nullable = false)
	private Long testId;

	@Column(name = "student_id", nullable = false)
	private Long studentId;

	private String studentName;

	@Column(name = "time_spent")
	private String timeSpent;

	@Enumerated(EnumType.STRING)
	@Column(name = "exam_type")
	private ExamType examType;

	@Column(name = "total_marks_obtained")
	private Double totalMarksObtained;

	@Column(name = "no_of_correct_answers")
	private Integer noOfCorrectAnswers;

	@Column(name = "submission_timestamp")
	private LocalDateTime submissionTimestamp;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	private boolean isTied;

	// For ranking comparison
	@Transient
	public int compareByMarksAndTime(TestRanking other) {
		// First compare by marks (descending)
		int marksComparison = Double.compare(other.totalMarksObtained, this.totalMarksObtained);
		if (marksComparison != 0) {
			return marksComparison;
		}
		// If marks are equal, compare by submission time (earlier is better)
		return this.submissionTimestamp.compareTo(other.submissionTimestamp);
	}
}