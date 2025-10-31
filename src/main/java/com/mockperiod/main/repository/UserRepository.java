package com.mockperiod.main.repository;


import com.mockperiod.main.entities.Plan;
import com.mockperiod.main.entities.Role;
import com.mockperiod.main.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    
    Optional<Users> findByEmail(String email);
    
    Optional<Users> findByPhoneNo(String phoneNo);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNo(String phoneNo);
    
    List<Users> findByRole(Role role);
    
    List<Users> findByRoleAndInstituteName(Role role, String instituteName);
    
    @Query("SELECT u FROM Users u WHERE u.role = :role AND u.isActive = true")
    List<Users> findActiveUsersByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.role = :role AND u.instituteName = :instituteName")
    long countByRoleAndInstituteName(@Param("role") Role role, @Param("instituteName") String instituteName);
    
    @Query("SELECT u FROM Users u WHERE u.email = :email OR u.phoneNo = :phoneNo")
    List<Users> findByEmailOrPhoneNo(@Param("email") String email, @Param("phoneNo") String phoneNo);
    
    // Check if email exists for other users (excluding current user)
    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    // Check if phone exists for other users (excluding current user)
    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.phoneNo = :phoneNo AND u.id != :id")
    boolean existsByPhoneNoAndIdNot(@Param("phoneNo") String phoneNo, @Param("id") Long id);
}
