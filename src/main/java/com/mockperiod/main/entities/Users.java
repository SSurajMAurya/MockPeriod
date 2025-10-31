package com.mockperiod.main.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Users {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String name;
	
	private String email;
	
	private String phoneNo;
	
	private String password;
	
	private String instituteName;
	
	private boolean isActive;
	
	@Enumerated
	private Role role;
	 
	@Enumerated
	private Plan plans;

	private boolean isPhoneVerified;
	
	private boolean isEmailVerified;
}
