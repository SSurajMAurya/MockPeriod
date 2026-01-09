package com.mockperiod.main.service;

import com.mockperiod.main.dto.UserDto;
import com.mockperiod.main.entities.Role;

import java.util.List;

public interface UserService {

	UserDto createUser(UserDto userDto);

	UserDto getUserById(Long id);

	UserDto getUserByEmail(String email);

	List<UserDto> getAllUsers();

	List<UserDto> getUsersByRole(Role role);

	UserDto updateUser(Long id, UserDto userDto);

	void deleteUser(Long id);

	UserDto activateUser(Long id);

	UserDto deactivateUser(Long id);

	void verifyEmail(Long userId);

	void verifyPhone(Long userId);

	UserDto updateUserPlan(Long userId, String plan);

	UserDto getCurrentUser();

	UserDto createFirstSuperAdmin(UserDto userDto);
	
	List<UserDto> getAllStudentByInstitute(String instituteEmail);
	
	Long countByIdandRole(Long id);
	
	Long countByRole(Role role);
	
	void sendresetPasswordMail(String to);
	
	void verifyresetOtp(String email , Integer otp ,String password);
	
}