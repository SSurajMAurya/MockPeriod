package com.mockperiod.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mockperiod.main.entities.ResetPassword;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
	
	Optional<ResetPassword> findByEmail(String email);

}
