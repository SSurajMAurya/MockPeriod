package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.UserDto;
import com.mockperiod.main.entities.Role;
import com.mockperiod.main.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
		log.info("Received request to create user with role: {}", userDto.getRole());
		UserDto createdUser = userService.createUser(userDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
		log.debug("Received request to get user by ID: {}", id);
		UserDto user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/email/{email}")
	public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
		log.debug("Received request to get user by email: {}", email);
		UserDto user = userService.getUserByEmail(email);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	public ResponseEntity<List<UserDto>> getAllUsers() {
		log.debug("Received request to get all users");
		List<UserDto> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/role/{role}")
	public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable Role role) {
		log.debug("Received request to get users by role: {}", role);
		List<UserDto> users = userService.getUsersByRole(role);
		return ResponseEntity.ok(users);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
		log.info("Received request to update user with ID: {}", id);
		UserDto updatedUser = userService.updateUser(id, userDto);
		return ResponseEntity.ok(updatedUser);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		log.info("Received request to delete user with ID: {}", id);
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/activate")
	public ResponseEntity<UserDto> activateUser(@PathVariable Long id) {
		log.info("Received request to activate user with ID: {}", id);
		UserDto user = userService.activateUser(id);
		return ResponseEntity.ok(user);
	}

	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<UserDto> deactivateUser(@PathVariable Long id) {
		log.info("Received request to deactivate user with ID: {}", id);
		UserDto user = userService.deactivateUser(id);
		return ResponseEntity.ok(user);
	}

	@PatchMapping("/{id}/verify-email")
	public ResponseEntity<Void> verifyEmail(@PathVariable Long id) {
		log.info("Received request to verify email for user ID: {}", id);
		userService.verifyEmail(id);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{id}/verify-phone")
	public ResponseEntity<Void> verifyPhone(@PathVariable Long id) {
		log.info("Received request to verify phone for user ID: {}", id);
		userService.verifyPhone(id);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{id}/plan")
	public ResponseEntity<UserDto> updateUserPlan(@PathVariable Long id, @RequestParam String plan) {
		log.info("Received request to update plan for user ID: {} to {}", id, plan);
		UserDto user = userService.updateUserPlan(id, plan);
		return ResponseEntity.ok(user);
	}

	@PostMapping("/setup")
	public ResponseEntity<UserDto> setupFirstSuperAdmin(@Valid @RequestBody UserDto userDto) {
		log.info("Received request to setup first superadmin");
		UserDto createdUser = userService.createFirstSuperAdmin(userDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

// Test endpoint
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("User API is working! " + java.time.LocalDateTime.now());
	}
	
	@GetMapping("/getAllStudent/{instituteEmail}")
	public ResponseEntity<List<UserDto>> getAllStudentByInstitute(@PathVariable String instituteEmail){
		try {
			
		List<UserDto> students	= userService.getAllStudentByInstitute(instituteEmail);
		
		return ResponseEntity.ok(students);
			
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving the student" + e.getMessage());
		}
	}
	
	
	@GetMapping("/getCount/{id}")
	public ResponseEntity<Long> getCount(@PathVariable Long id){
		try {
			
			
			
		Long count = userService.countByIdandRole(id);
		
		return ResponseEntity.ok(count);
			
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving the Count" + e.getMessage());
		}
	}
	
	@GetMapping("/getCountByRole/{role}")
	public ResponseEntity<Long> getCount(@PathVariable String role){
		try {
			
			Role role2 = Role.valueOf(role);
			
		Long count = userService.countByRole(role2);
		
		return ResponseEntity.ok(count);
			
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving the Count" + e.getMessage());
		}
	}
	
	
	@PostMapping("/send-reset-password-otp")
	public ResponseEntity<String> sendResetPasswordOtp(@RequestParam String email) {
	    userService.sendresetPasswordMail(email);
	    return ResponseEntity.ok("Password reset OTP sent successfully");
	}

	@PostMapping("/verify-otp-and-reset-password")
	public ResponseEntity<String> verifyOtpAndResetPassword(
	        @RequestParam String email,
	        @RequestParam Integer otp,
	        @RequestParam String newPassword) {
	    
	    userService.verifyresetOtp(email, otp, newPassword);
	    return ResponseEntity.ok("Password reset successfully");
	}
	
}
