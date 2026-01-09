package com.mockperiod.main.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Exam {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String examName;

	private String description; 

	@OneToMany(mappedBy = "exam")
	private List<Tests> tests = new ArrayList<>();
	
//	@OneToMany(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
//	 @JoinTable(
//	            name = "exam_subjects",
//	            joinColumns = @JoinColumn(name = "exam_id")
//	        )
//	private List<Subjects> subjects = new ArrayList<Subjects>();
	
	
	 @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	    @JoinTable(
	        name = "exam_subjects",
	        joinColumns = @JoinColumn(name = "exam_id"),
	        inverseJoinColumns = @JoinColumn(name = "subject_id")
	    )
	    private List<Subjects> subjects = new ArrayList<>();
	 
	 private String examImageUrl;

}
