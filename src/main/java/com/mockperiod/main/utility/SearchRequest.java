package com.mockperiod.main.utility;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequest {
	
	private String keyword;
    private List<Long> examIds;
//    private List<SubjectCategory> subjectCategories;
    private List<Long> subjectIds;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean hasImages;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";

}
