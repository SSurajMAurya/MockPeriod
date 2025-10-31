package com.mockperiod.main.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Questions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String questionText; 

	@Column(name = "question_image_url")
	private String questionImageUrl; 

	@ManyToOne
	@JoinColumn(name = "test_id", nullable = false)
	private Tests test;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
	private List<Options> options = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "subject_id")
	private Subjects subject;

	@ManyToOne
	@JoinColumn(name = "chapter_id")
	private Chapter chapter;

	private Integer questionNumber;
	private Integer marks;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
