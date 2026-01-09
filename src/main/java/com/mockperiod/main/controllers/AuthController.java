package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.JwtResponse;
import com.mockperiod.main.dto.LoginRequest;
//import com.mockperiod.main.dto.RegisterRequest;
import com.mockperiod.main.dto.UserDto;
import com.mockperiod.main.entities.PaymentStatus;
import com.mockperiod.main.entities.Plan;
import com.mockperiod.main.entities.Role;
import com.mockperiod.main.entities.Users;
import com.mockperiod.main.jwtUtil.JwtUtils;
import com.mockperiod.main.repository.UserRepository;
//import com.mockperiod.main.util.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        Users user = Users.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .phoneNo(registerRequest.getPhoneNo())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .instituteName(registerRequest.getInstituteName())
                .role(Role.valueOf(registerRequest.getRole()))
                .plans(Plan.valueOf(registerRequest.getPlans()))
                .instituteEmail(registerRequest.getInstituteEmail())
                .isActive(true)
                .isEmailVerified(true) // Set to false, require email verification
                .isPhoneVerified(true)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully! Please verify your email.");
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            String jwt = jwtUtil.generateToken(loginRequest.getEmail());
//            
//            Users user = (Users) authentication.getPrincipal();
//            
//            return ResponseEntity.ok(new JwtResponse(
//               "Bearer "+ jwt, 
//                user.getEmail(), 
//                user.getName(), 
//               user.getRole().toString(),
//               user.getId(),
//               user.getInstituteId() != null ? user.getInstituteId() : null;
//               user.getInstituteEmail(),
//               user.getPlans().toString(),
//               user.getPlanExpireDate().toString()
//               
//            		));
//            
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error: Invalid credentials!");
//        }
//    }
    
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(loginRequest.getEmail());
            
            Users user = (Users) authentication.getPrincipal();
            
            return ResponseEntity.ok(new JwtResponse(
                "Bearer "+ jwt, 
                user.getEmail(), 
                user.getName(), 
                user.getRole().toString(),
                user.getId(),
                user.getInstituteId() != null ? user.getInstituteId() : null,  
                user.getInstituteEmail() != null ? user.getInstituteEmail() : null, 
                user.getPlans() != null ? user.getPlans().toString() : null, 
                user.getPlanExpireDate() != null ? user.getPlanExpireDate().toString() : null,
                user.getPaymentStatus() !=  null ? user.getPaymentStatus().toString() : null
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid credentials!");
        }
    }
    
}
