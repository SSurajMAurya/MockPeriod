package com.mockperiod.main.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_options")
public class Options {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String optionText; 
    
    @Column(name = "option_image_url")
    private String optionImageUrl; 
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Questions question;
    
    private Integer optionNumber; 
    private Boolean isCorrect = false;
    
    
}
