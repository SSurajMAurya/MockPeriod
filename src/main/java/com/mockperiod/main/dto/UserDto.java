package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

	private long id;

	private String name;

	private String email;

	private String phoneNo;

	private String password;

	private String instituteName;

	private boolean isActive;

	private String role;

	private String plans;

	private String instituteEmail;

	private Long instituteId;

	private boolean isPhoneVerified;

	private boolean isEmailVerified;
	
	private String planExpireDate;
	
	private String avatarUrl;

}
