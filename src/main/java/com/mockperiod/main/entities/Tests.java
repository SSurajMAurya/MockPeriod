package com.mockperiod.main.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Tests {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String testName;

	private Integer durationMinutes;

	private Double correctMark;
	private Double negativeMark;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Subjects> subjects = new ArrayList<Subjects>();

	@Enumerated
	private Language language;

	@ManyToOne
	@JoinColumn(name = "exam_id", nullable = false)
	private Domains exam;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Questions> questions = new ArrayList<Questions>();

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
