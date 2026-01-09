package com.mockperiod.main.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//import io.jsonwebtoken.lang.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_id")
	private Tests test;

//    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Options> options = new ArrayList<>();
	
	 @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	    private Set<Options> options = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "subject_id")
	private Subjects subject;

	@ManyToOne
	@JoinColumn(name = "chapter_id")
	private Chapter chapter;
	
	@Enumerated(EnumType.STRING)
	private Language language;

	private Integer questionNumber;
	private Double marks;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof Questions)) return false;
	        Questions questions = (Questions) o;
	        return Objects.equals(id, questions.id);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(id);
	    }

}
