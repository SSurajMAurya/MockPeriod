package com.mockperiod.main.utility;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationResult {
	
	private boolean valid;
    private List<String> errors;

//    public ValidationResult(boolean valid, List<String> errors) {
//        this.valid = valid;
//        this.errors = errors != null ? errors : new ArrayList<>();
//    }

}
