package com.mockperiod.main.repository;


import com.mockperiod.main.entities.TestRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRankingRepository extends JpaRepository<TestRanking, Long> {
    
    List<TestRanking> findByTestIdOrderByTotalMarksObtainedDescSubmissionTimestampAsc(Long testId);
    
    Optional<TestRanking> findByTestIdAndStudentId(Long testId, Long studentId);
    
    @Query("SELECT tr FROM TestRanking tr WHERE tr.testId = :testId ORDER BY tr.rank ASC LIMIT :limit")
    List<TestRanking> findTopByTestIdOrderByRankAsc(@Param("testId") Long testId, @Param("limit") int limit);
    
    boolean existsByTestIdAndStudentId(Long testId, Long studentId);
    
    List<TestRanking> findByTestIdOrderByRankAsc(Long testId);
    
    List<TestRanking> findByTestId(Long testId);
    
   
    
   
}
