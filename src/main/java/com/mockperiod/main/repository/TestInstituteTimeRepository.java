package com.mockperiod.main.repository;


import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mockperiod.main.entities.TestInstituteTime;
 
public interface TestInstituteTimeRepository extends JpaRepository<TestInstituteTime, Long> {
	
	
	 @Query("SELECT t FROM TestInstituteTime t WHERE " +
	           "t.testId = :testId AND " +
	           "t.intituteId = :instituteId AND " +
	           "t.starDateTime <= :currentTime AND " +
	           "t.enDateTime >= :currentTime")
	    Optional<TestInstituteTime> findActiveTestForInstitute(
	            @Param("testId") Long testId,
	            @Param("instituteId") Long instituteId,
	            @Param("currentTime") LocalDateTime currentTime);
	    
	    /**
	     * Alternative method using database current timestamp
	     */
	    @Query("SELECT t FROM TestInstituteTime t WHERE " +
	           "t.testId = :testId AND " +
	           "t.intituteId = :instituteId AND " +
	           "t.starDateTime <= CURRENT_TIMESTAMP AND " +
	           "t.enDateTime >= CURRENT_TIMESTAMP")
	    Optional<TestInstituteTime> findCurrentlyActiveTestForInstitute(
	            @Param("testId") Long testId,
	            @Param("instituteId") Long instituteId);
	    
	    
//	   Optional<TestInstituteTime> findByTestIdAndInstituteId(Long testId , Long instituteId);
	    
	    @Query("SELECT t FROM TestInstituteTime t WHERE t.testId = :testId AND t.intituteId = :instituteId")
	    Optional<TestInstituteTime> findByTestIdAndInstituteId(@Param("testId") Long testId, 
	                                                          @Param("instituteId") Long instituteId);
	    
	    
	    @Query("SELECT t FROM TestInstituteTime t WHERE " +
	            "t.intituteId = :instituteId AND t.testId = :testId AND " +
	            "(t.starDateTime BETWEEN :startTime AND :endTime OR " +
	            "t.enDateTime BETWEEN :startTime AND :endTime OR " +
	            ":startTime BETWEEN t.starDateTime AND t.enDateTime)")
	     Optional<TestInstituteTime> findByIntituteIdAndTestIdAndTimeRange(
	             @Param("instituteId") Long instituteId,
	             @Param("testId") Long testId,
	             @Param("startTime") LocalDateTime startTime,
	             @Param("endTime") LocalDateTime endTime);
	    
	    
	    
	    
	    // Find by institute and test (for checking existence and updates)
	    Optional<TestInstituteTime> findByIntituteIdAndTestId(Long instituteId, Long testId);
	    
	    // Check if exists by institute and test
	    boolean existsByIntituteIdAndTestId(Long instituteId, Long testId);
	    
	    // Find overlapping timings (excluding current record for updates)
	    @Query("SELECT t FROM TestInstituteTime t WHERE " +
	           "t.intituteId = :instituteId AND " +
	           "t.testId = :testId AND " +
	           "(:excludeId IS NULL OR t.id != :excludeId) AND " +
	           "((t.starDateTime BETWEEN :startTime AND :endTime) OR " +
	           "(t.enDateTime BETWEEN :startTime AND :endTime) OR " +
	           "(t.starDateTime <= :startTime AND t.enDateTime >= :endTime))")
	    Optional<TestInstituteTime> findOverlappingTiming(@Param("excludeId") Long excludeId,
	                                                     @Param("instituteId") Long instituteId,
	                                                     @Param("testId") Long testId,
	                                                     @Param("startTime") LocalDateTime startTime,
	                                                     @Param("endTime") LocalDateTime endTime);

}
