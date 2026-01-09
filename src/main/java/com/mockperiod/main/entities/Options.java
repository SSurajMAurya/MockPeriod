package com.mockperiod.main.entities;

import java.util.Objects;

//import io.jsonwebtoken.lang.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_options")
@Builder
public class Options {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String optionText; 
    
    @Column(name = "option_image_url")
    private String optionImageUrl; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Questions question;
    
    private Integer optionNumber; 
    private Boolean isCorrect = false;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Options)) return false;
        Options options = (Options) o;
        return Objects.equals(id, options.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    
}
