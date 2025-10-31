package com.mockperiod.main.utility;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkUploadResponse {
	 private int totalRecords;
	    private int successful;
	    private int failed;
	    private List<String> errors;
	    private LocalDateTime processedAt;

}
