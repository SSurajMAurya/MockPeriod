package com.mockperiod.main.service;

import com.mockperiod.main.dto.TestInstituteTimeDto;

public interface TestInstituteTimeService {
	
	 TestInstituteTimeDto createOrUpdateTestInstituteTime(TestInstituteTimeDto testInstituteTimeDto);
	    
	    // Get test institute timing by institute and test
	    TestInstituteTimeDto getTestInstituteTime(Long instituteId, Long testId);
	    
	    // Delete test institute timing
	    void deleteTestInstituteTime(Long instituteId, Long testId);
	    
	    // Check if timing exists
	    boolean existsByInstituteAndTest(Long instituteId, Long testId);

}
