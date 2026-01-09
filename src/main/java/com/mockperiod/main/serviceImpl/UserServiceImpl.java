package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.ResendEmailResponse;
import com.mockperiod.main.dto.UserDto;
import com.mockperiod.main.entities.Plan;
import com.mockperiod.main.entities.ResetPassword;
import com.mockperiod.main.entities.Role;
import com.mockperiod.main.entities.Users;
import com.mockperiod.main.exceptions.CustomException;
import com.mockperiod.main.exceptions.ResourceNotFoundException;
import com.mockperiod.main.exceptions.UserManagementException;
import com.mockperiod.main.repository.ResetPasswordRepository;
import com.mockperiod.main.repository.UserRepository;
import com.mockperiod.main.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ResendEmailService emailService;
	private final ResetPasswordRepository passwordRepository;

	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {
		try {
			log.info("Creating user with email: {}", userDto.getEmail());

			// Check if user already exists
			validateUserUniqueness(userDto.getEmail(), userDto.getPhoneNo());
			
			if(userDto.getRole().equals(Role.STUDENT.toString())) {
				
			   Users intititute = userRepository.findById(userDto.getInstituteId())
					   .orElseThrow(() -> new CustomException("Institute Not found ", HttpStatus.NOT_FOUND));
			   
			   Plan plan = intititute.getPlans();
			   
//			   Long count  = 90L;
			   Long count = userRepository.countStudentsByInstituteId(userDto.getInstituteId());
			   
			   if(plan.equals(Plan.BASIC) && count >= 40) {
				   throw new CustomException("Need to upgrade in order to add more student", HttpStatus.CONFLICT);
			   }
			   if(plan.equals(Plan.STANDARD) && count >= 80) {
				   throw new CustomException("Maximum limit reached for student", HttpStatus.CONFLICT);
			   }
				
			}

			// Create user entity
			Role targetRole = validateAndParseRole(userDto.getRole());
			Users user = buildUserEntity(userDto, targetRole);

			LocalDateTime date = null;

			if (user.getPlans() != null) {
				if (userDto.getPlanExpireDate() == null) {

					date = LocalDateTime.now().plusMonths(1);

				} else {
					date = LocalDateTime.parse(userDto.getPlanExpireDate());
				}

				user.setPlanExpireDate(date);
			}

			Users savedUser = userRepository.save(user);
			log.info("User created successfully with ID: {}", savedUser.getId());

			return mapToDto(savedUser);

		} catch (DataIntegrityViolationException e) {
			log.error("Data integrity violation while creating user: {}", e.getMessage());
			throw new UserManagementException("User with same email or phone already exists");
		} catch (Exception e) {
			log.error("Error creating user: {}", e.getMessage());
			throw new UserManagementException("Failed to create user: " + e.getMessage());
		}
	}

	@Override
	public UserDto getUserById(Long id) {
		log.debug("Fetching user by ID: {}", id);

		Users user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

		return mapToDto(user);
	}

	@Override
	public UserDto getUserByEmail(String email) {
		log.debug("Fetching user by email: {}", email);

		Users user = getUserByEmailOrThrow(email);
		return mapToDto(user);
	}

	@Override
	public List<UserDto> getAllUsers() {
		log.debug("Fetching all users");

		return userRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
	}

	@Override
	public List<UserDto> getUsersByRole(Role role) {
		log.debug("Fetching users by role: {}", role);

		List<Users> users = userRepository.findByRole(role);
		return users.stream().map(this::mapToDto).collect(Collectors.toList());
	}

//    @Override
//    @Transactional
//    public UserDto updateUser(Long id, UserDto userDto) {
//        try {
//            log.info("Updating user with ID: {}", id);
//            
//            Users existingUser = userRepository.findById(id)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
//            
//            // Update allowed fields
//            updateAllowedFields(existingUser, userDto);
//            
//            Users updatedUser = userRepository.save(existingUser);
//            log.info("User updated successfully with ID: {}", updatedUser.getId());
//            
//            return mapToDto(updatedUser);
//            
//        } catch (Exception e) {
//            log.error("Error updating user with ID {}: {}", id, e.getMessage());
//            throw new UserManagementException("Failed to update user: " + e.getMessage());
//        }
//    }

	@Override
	@Transactional
	public UserDto updateUser(Long id, UserDto userDto) {
		try {
			log.info("Updating user with ID: {}", id);

			Users existingUser = userRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

			// Update only the fields that are provided (not null)
			updateProvidedFields(existingUser, userDto);

			Users updatedUser = userRepository.save(existingUser);
			log.info("User updated successfully with ID: {}", updatedUser.getId());

			return mapToDto(updatedUser);

		} catch (Exception e) {
			log.error("Error updating user with ID {}: {}", id, e.getMessage());
			throw new UserManagementException("Failed to update user: " + e.getMessage());
		}
	}

	// Private helper method for partial updates
	private void updateProvidedFields(Users user, UserDto userDto) {
		// Only update name if provided
		if (userDto.getName() != null && !userDto.getName().trim().isEmpty()) {
			user.setName(userDto.getName().trim());
		}
		
		if(userDto.getAvatarUrl() != null) {
			user.setAvatarUrl(userDto.getAvatarUrl());
		}

		// Only update email if provided and different from current
		if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
			String newEmail = userDto.getEmail().trim();
			if (!newEmail.equals(user.getEmail())) {
				// Check if new email is already taken by another user
				if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
					throw new UserManagementException("Email " + newEmail + " is already taken");
				}
				user.setEmail(newEmail);
				user.setEmailVerified(false); // Require re-verification if email changed
			}
		}

		// Only update phone number if provided and different from current
		if (userDto.getPhoneNo() != null && !userDto.getPhoneNo().trim().isEmpty()) {
			String newPhone = userDto.getPhoneNo().trim();
			if (!newPhone.equals(user.getPhoneNo())) {
				// Check if new phone is already taken by another user
				if (userRepository.existsByPhoneNoAndIdNot(newPhone, user.getId())) {
					throw new UserManagementException("Phone number " + newPhone + " is already taken");
				}
				user.setPhoneNo(newPhone);
				user.setPhoneVerified(false); // Require re-verification if phone changed
			}
		}

		// Only update institute name if provided
		if (userDto.getInstituteName() != null) {
			user.setInstituteName(userDto.getInstituteName().trim());
		}

		// Only update role if provided and valid
		if (userDto.getRole() != null && !userDto.getRole().trim().isEmpty()) {
			Role newRole = validateAndParseRole(userDto.getRole());
			user.setRole(newRole);

			// If changing to ADMIN and no plan set, set default BASIC plan
			if (newRole == Role.ADMIN && user.getPlans() == null) {
				user.setPlans(Plan.BASIC);
			}
			// If changing from ADMIN to other role, remove plan
			else if (newRole != Role.ADMIN) {
				user.setPlans(null);
			}
		}

		// Only update plan if provided and user is ADMIN
		if (userDto.getPlans() != null && !userDto.getPlans().trim().isEmpty()) {
			if (user.getRole() != Role.SUPERADMIN) {
				throw new UserManagementException("Only SUPERADMIN users can have plans");
			}
			Plan newPlan = validateAndParsePlan(userDto.getPlans());
			user.setPlans(newPlan);
		}

		if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
//			if (user.getRole() != Role.ADMIN || user.getRole() != Role.SUPERADMIN) {
//				throw new UserManagementException("Only ADMIN users can change the password");
//			}

			user.setPassword(userDto.getPassword());
		}

		// Update active status if provided
		if (userDto.isActive() != user.isActive()) {
			user.setActive(userDto.isActive());
		}

		// Update verification status if provided
		if (userDto.isEmailVerified()) {
			user.setEmailVerified(true);
		}

		if (userDto.isPhoneVerified()) {
			user.setPhoneVerified(true);
		}

		// Update timestamp
//        user.setUpdatedAt(java.time.LocalDateTime.now());
	}

	@Override
	@Transactional
	public void deleteUser(Long id) {
		log.info("Deleting user with ID: {}", id);

		Users user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

		userRepository.delete(user);
		log.info("User deleted successfully with ID: {}", id);
	}

	@Override
	@Transactional
	public UserDto activateUser(Long id) {
		log.info("Activating user with ID: {}", id);

		Users user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

		user.setActive(true);
		Users activatedUser = userRepository.save(user);

		log.info("User activated successfully with ID: {}", id);
		return mapToDto(activatedUser);
	}

	@Override
	@Transactional
	public UserDto deactivateUser(Long id) {
		log.info("Deactivating user with ID: {}", id);

		Users user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

		user.setActive(false);
		Users deactivatedUser = userRepository.save(user);

		log.info("User deactivated successfully with ID: {}", id);
		return mapToDto(deactivatedUser);
	}

	@Override
	@Transactional
	public void verifyEmail(Long userId) {
		log.info("Verifying email for user ID: {}", userId);

		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

		user.setEmailVerified(true);
		userRepository.save(user);
		log.info("Email verified for user ID: {}", userId);
	}

	@Override
	@Transactional
	public void verifyPhone(Long userId) {
		log.info("Verifying phone for user ID: {}", userId);

		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

		user.setPhoneVerified(true);
		userRepository.save(user);
		log.info("Phone verified for user ID: {}", userId);
	}

	@Override
	@Transactional
	public UserDto updateUserPlan(Long userId, String plan) {
		log.info("Updating plan for user ID: {} to {}", userId, plan);

		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

		Plan newPlan = validateAndParsePlan(plan);
		user.setPlans(newPlan);

		Users updatedUser = userRepository.save(user);
		log.info("Plan updated successfully for user ID: {}", userId);
		return mapToDto(updatedUser);
	}

	@Override
	public UserDto getCurrentUser() {
		// For now, return a dummy user or implement later with Spring Security
		throw new UserManagementException("Current user feature not implemented yet");
	}

	@Override
	@Transactional
	public UserDto createFirstSuperAdmin(UserDto userDto) {
		log.info("Creating first superadmin user");

		if (!"SUPERADMIN".equals(userDto.getRole())) {
			throw new UserManagementException("First user must be SUPERADMIN");
		}

		long userCount = userRepository.count();
		if (userCount > 0) {
			throw new UserManagementException("Superadmin already exists. Please login to create new users.");
		}

		validateUserUniqueness(userDto.getEmail(), userDto.getPhoneNo());

		Users user = buildUserEntity(userDto, Role.SUPERADMIN);
		Users savedUser = userRepository.save(user);

		log.info("First superadmin created successfully with ID: {}", savedUser.getId());
		return mapToDto(savedUser);
	}

	// Private helper methods
	private Users getUserByEmailOrThrow(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
	}

	private void validateUserUniqueness(String email, String phoneNo) {
		if (userRepository.existsByEmail(email)) {
			throw new UserManagementException("User with email " + email + " already exists");
		}

		if (userRepository.existsByPhoneNo(phoneNo)) {
			throw new UserManagementException("User with phone number " + phoneNo + " already exists");
		}

		List<Users> existingUsers = userRepository.findByEmailOrPhoneNo(email, phoneNo);
		if (!existingUsers.isEmpty()) {
			throw new UserManagementException("User with same email or phone already exists");
		}
	}

//	private Users buildUserEntity(UserDto userDto, Role role) {
//		return Users.builder().name(userDto.getName()).email(userDto.getEmail()).phoneNo(userDto.getPhoneNo())
//				.password(passwordEncoder.encode(userDto.getPassword()))
//				.instituteName(userDto.getInstituteName())
//				.instituteId(userDto.getIntituteId())
//				.instituteEmail(userDto.getInstituteEmail())
//				.isActive(true).role(role).plans(role == Role.ADMIN ? Plan.BASIC : null).isPhoneVerified(true)
//				.isEmailVerified(true).build();
//	}
	
	private Users buildUserEntity(UserDto userDto, Role role) {
	    Users.UsersBuilder builder = Users.builder()
	            .name(userDto.getName())
	            .email(userDto.getEmail())
	            .phoneNo(userDto.getPhoneNo())
	            .password(passwordEncoder.encode(userDto.getPassword()))
	            .instituteName(userDto.getInstituteName())
	            .isActive(true)
	            .role(role)
	            .plans(role == Role.ADMIN ? Plan.BASIC : null)
	            .isPhoneVerified(true)
	            .isEmailVerified(true);

	    // Conditionally set instituteEmail if present
	    if (userDto.getInstituteEmail() != null) {
	        builder.instituteEmail(userDto.getInstituteEmail());
	    }

	    // Conditionally set instituteId if present
	    if (userDto.getInstituteId() != null) {
	        builder.instituteId(userDto.getInstituteId());
	    }

	    return builder.build();
	}

	private Role validateAndParseRole(String roleStr) {
		try {
			return Role.valueOf(roleStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new UserManagementException("Invalid role: " + roleStr);
		}
	}

	private Plan validateAndParsePlan(String planStr) {
		try {
			return Plan.valueOf(planStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new UserManagementException("Invalid plan: " + planStr);
		}
	}

	private void updateAllowedFields(Users user, UserDto userDto) {
		if (userDto.getName() != null) {
			user.setName(userDto.getName());
		}
		if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
			user.setEmail(userDto.getEmail());
			user.setEmailVerified(false); // Require re-verification if email changed
		}
		if (userDto.getPhoneNo() != null) {
			user.setPhoneNo(userDto.getPhoneNo());
		}
		if (userDto.getInstituteName() != null) {
			user.setInstituteName(userDto.getInstituteName());
		}
		if (userDto.getRole() != null) {
			user.setRole(validateAndParseRole(userDto.getRole()));
		}
	}

//	private UserDto mapToDto(Users user) {
//		return UserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).phoneNo(user.getPhoneNo())
//				.instituteName(user.getInstituteName()).isActive(user.isActive()).role(user.getRole().name())
//				.plans(user.getPlans() != null ? user.getPlans().name() : null).isPhoneVerified(user.isPhoneVerified())
//				.isEmailVerified(user.isEmailVerified()).password(user.getPassword())
//				.planExpireDate(user.getPlanExpireDate().toString())
//				.build();
//	}
	
	private UserDto mapToDto(Users user) {
	    return UserDto.builder()
	            .id(user.getId())
	            .name(user.getName())
	            .email(user.getEmail())
	            .phoneNo(user.getPhoneNo())
	            .instituteName(user.getInstituteName())
	            .instituteEmail(user.getInstituteEmail() != null ? user.getInstituteEmail() : null)
	            .instituteId(user.getInstituteId() != null ? user.getInstituteId() : null)
	            .isActive(user.isActive())
	            .role(user.getRole().name())
	            .plans(user.getPlans() != null ? user.getPlans().name() : null)
	            .isPhoneVerified(user.isPhoneVerified())
	            .isEmailVerified(user.isEmailVerified())
	            .password(user.getPassword())
	            .planExpireDate(user.getPlanExpireDate() != null ? user.getPlanExpireDate().toString() : null)
	            .build();
	}

	@Override
	public List<UserDto> getAllStudentByInstitute(String instituteEmail) {
		
	Users institute	= userRepository.findByEmail(instituteEmail).
		      orElseThrow(() -> new CustomException("Institute not found with email: "+instituteEmail, HttpStatus.NOT_FOUND));
	
	
	List<Users> studentList = userRepository.findByInstituteEmail(instituteEmail);
	
	if(studentList == null || studentList.isEmpty()) {
		throw new CustomException("No Student found for Institute: "+institute.getName(), HttpStatus.NOT_FOUND);
	}
	
	return studentList.stream().map((student) -> mapToDto(student)).collect(Collectors.toList());
	
	
	}

	@Override
	public Long countByIdandRole(Long id) {
		
		return userRepository.countStudentsByInstituteId(id); 
	}

	@Override
	public Long countByRole(Role role) {
		return userRepository.countByRole(role); 
	}

//	@Override
//	public void sendresetPasswordMail(String to) {
//		
//		userRepository.findByEmail(to).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
//		
//		Integer otp = (int) (Math.random() * 900000) + 100000;
//		
//		ResetPassword resetPassword = new ResetPassword();
//		resetPassword.setEmail(to);
//		resetPassword.setOtp(otp);
//		resetPassword.setExpirtedAt(LocalDateTime.now().plusMinutes(5));
//			
//		String subject = "your password reset code is : "+ otp;
//		
//		emailService.sendResetEmail(to, otp).block();
//		
//		passwordRepository.save(resetPassword);
//		
//	}
	
	@Override
	public void sendresetPasswordMail(String to) {
	    
	    userRepository.findByEmail(to).orElseThrow(() -> 
	        new CustomException("User not found", HttpStatus.NOT_FOUND));
	    
	    Integer otp = (int) (Math.random() * 900000) + 100000;
	    
	    ResetPassword resetPassword = new ResetPassword();
	    resetPassword.setEmail(to);
	    resetPassword.setOtp(otp);
	    resetPassword.setExpirtedAt(LocalDateTime.now().plusMinutes(5));
	    
	    try {
	        // Send email and wait for response
	        ResendEmailResponse response = emailService.sendResetEmailSync(to, otp);
	        
	        if (response != null && response.getId() != null) {
	            // Save OTP to database
	            passwordRepository.save(resetPassword);
	            log.info("Reset password OTP sent to {}: {}", to, response.getId());
	        } else {
	            throw new CustomException("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	        
	    } catch (CustomException e) {
	        throw e;
	    } catch (Exception e) {
	        log.error("Failed to send reset password email to {}: {}", to, e.getMessage(), e);
	        throw new CustomException("Failed to send email. Please try again.", 
	                                 HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	@Override
	public void verifyresetOtp(String email , Integer otp ,String password) {
		
		 Users users =  userRepository.findByEmail(email).orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
		
		ResetPassword resetPassword =  passwordRepository.findByEmail(email).orElseThrow(() -> new CustomException("Otp not found for User :"+email, HttpStatus.NOT_FOUND));
		
		if(resetPassword.getOtp().equals(otp) && !LocalDateTime.now().isAfter(resetPassword.getExpirtedAt())){
			
			users.setPassword(passwordEncoder.encode(password));
			
			userRepository.save(users);
			resetPassword.setOtp(null);
			resetPassword.setExpirtedAt(null);
			resetPassword.setEmail(null);
			
			passwordRepository.save(resetPassword);
			
		}
		else {
			throw new CustomException("Inavlid otp or expired otp", HttpStatus.ALREADY_REPORTED);
		}
		
		
	}
}