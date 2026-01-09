package com.mockperiod.main.serviceImpl;

import com.mockperiod.main.dto.AdminRegistrationRequest;
import com.mockperiod.main.dto.PaymentOrderResponse;
import com.mockperiod.main.dto.PaymentVerificationResponse;
import com.mockperiod.main.entities.PaymentStatus;
import com.mockperiod.main.entities.Plan;
import com.mockperiod.main.entities.Role;
import com.mockperiod.main.entities.Users;
import com.mockperiod.main.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendEmailService emailService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.callback.url}")
    private String callbackUrl;

    // Plan amounts configuration - you can change these in application.properties
    @Value("${plan.amount.basic:100000}") // 1000.00 INR (in paise)
    private Integer basicPlanAmount;
    
    @Value("${plan.amount.standard:200000}") // 2000.00 INR (in paise)
    private Integer standardPlanAmount;
    
    @Value("${plan.amount.premium:300000}") // 3000.00 INR (in paise)
    private String premiumPlanAmount;
    
    @Value("${default.currency:INR}")
    private String defaultCurrency;



    /**
     * Create admin registration with payment
     */
    @Transactional
    public PaymentOrderResponse initiateAdminRegistration(AdminRegistrationRequest request) throws RazorpayException {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }
        
        Plan  plan = Plan.valueOf(request.getPlan());

        // Get amount based on selected plan
        Integer amount = getAmountForPlan(plan);
        
        // Create user entity with PENDING status
        Users user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNo(request.getPhoneNo())
                .password(passwordEncoder.encode(request.getPassword()))
                .instituteName(request.getInstituteName())
                .instituteEmail(request.getInstituteEmail())
                .role(Role.ADMIN) // Always ADMIN for this flow
                .plans(plan)
                .isActive(false) // Not active until payment is successful
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // Create Razorpay order
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", defaultCurrency);
        orderRequest.put("receipt", "admin_reg_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1);
        
        // Add notes to identify the user during callback
        JSONObject notes = new JSONObject();
        notes.put("user_email", user.getEmail());
        notes.put("user_name", user.getName());
        notes.put("purpose", "admin_registration");
        notes.put("plan", request.getPlan());
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);

        
        user.setRazorpayOrderId(order.get("id"));
        userRepository.save(user);

        // Return payment order response
        return PaymentOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(amount)
                .currency(defaultCurrency)
                .razorpayKey(razorpayKeyId)
                .receipt(order.get("receipt"))
                .description("Admin Registration - " + request.getPlan() + " Plan")
                .build();
    }

    /**
     * Verify payment and complete admin registration
     */
    @Transactional
    public PaymentVerificationResponse verifyAndCompletePayment( 
                                                               String razorpayOrderId
                                                               ) {
        try {
           
            
            Users user = userRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("User not found for order: " + razorpayOrderId));

            if (user.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return PaymentVerificationResponse.builder()
                        .success(true)
                        .message("Payment already verified")
//                        .paymentId(razorpayPaymentId)
                        .orderId(razorpayOrderId)
                        .adminUserId(String.valueOf(user.getId()))
                        .email(user.getEmail())
                        .build();
            }

            // Update payment details
//            user.setRazorpayPaymentId(razorpayPaymentId);
            user.setPaymentStatus(PaymentStatus.SUCCESS);
            user.setPaymentDate(LocalDateTime.now());
            
            // Activate user account
            user.setActive(true);
            user.setEmailVerified(true); // Since payment is done, mark email as verified
            
            // Set plan expiry date (30 days from now as default)
            user.setPlanExpireDate(LocalDateTime.now().plusDays(30));
            user.setPlanExpiry(false); // Plan is not expired yet
            
            // Save the updated user
            userRepository.save(user);
            
           

            return PaymentVerificationResponse.builder()
                    .success(true)
                    .message("Payment verified successfully. Admin account created and activated.")
//                    .paymentId(razorpayPaymentId)
                    .orderId(razorpayOrderId)
                    .adminUserId(String.valueOf(user.getId()))
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            // Mark payment as failed
            userRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(user -> {
                user.setPaymentStatus(PaymentStatus.FAILED);
                userRepository.save(user);
            });
            
            
            
            return PaymentVerificationResponse.builder()
                    .success(false)
                    .message("Payment verification failed: " + e.getMessage())
//                    .paymentId(razorpayPaymentId)
                    .orderId(razorpayOrderId)
                    .build();
        }
    }

  
    private Integer getAmountForPlan(Plan plan) {
        switch (plan) {
            case BASIC:
                return basicPlanAmount;
            case STANDARD:
                return standardPlanAmount;
            default:
                throw new IllegalArgumentException("Invalid plan selected");
        }
    }

    /**
     * Check if user has completed payment
     */
    public boolean hasUserCompletedPayment(String email) {
        return userRepository.findByEmailAndPaymentStatus(email, PaymentStatus.SUCCESS)
                .isPresent();
    }

    /**
     * Get user by payment order ID
     */
    public Users getUserByOrderId(String orderId) {
        return userRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("User not found for order: " + orderId));
    }
    
    /**
     * Get plan amounts map
     */
    public Map<String, Integer> getPlanAmounts() {
        Map<String, Integer> amounts = new HashMap<>();
        amounts.put("BASIC", basicPlanAmount);
        amounts.put("STANDARD", standardPlanAmount);
//        amounts.put("PREMIUM", premiumPlanAmount);
        return amounts;
    }
}