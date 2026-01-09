//package com.mockperiod.main.controllers;
//
//import com.mockperiod.main.dto.PaymentOrderResponse;
//import com.mockperiod.main.dto.PaymentVerificationResponse;
//import com.mockperiod.main.entities.Role;
//import com.mockperiod.main.entities.Users;
//import com.mockperiod.main.exceptions.CustomException;
//import com.mockperiod.main.repository.UserRepository;
////import com.mockperiod.main.service.PaymentService;
//import com.mockperiod.main.service.UserService;
//import com.mockperiod.main.serviceImpl.PaymentService;
//import com.razorpay.RazorpayException;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/admin/registration")
//public class AdminRegistrationController {
//
//    private final PaymentService paymentService;
//    private final UserService userService;
//    private final UserRepository userRepository;
//
//    public AdminRegistrationController(PaymentService paymentService, UserService userService , UserRepository userRepository) {
//        this.paymentService = paymentService;
//        this.userService = userService;
//        this.userRepository = userRepository;
//    }
//
//   
//    @PostMapping("/initiate")
//    public ResponseEntity<PaymentOrderResponse> initiateAdminRegistration(@RequestBody Users userDetails) {
//        try {
//            // Check if user already exists
//            if (userRepository.existsByEmail(userDetails.getEmail())) {
//                throw new CustomException("User with this email already exists" , HttpStatus.NOT_FOUND);
//            }
//
//            // Set user as admin role
//            userDetails.setRole(Role.ADMIN);
//            userDetails.setActive(false); // Inactive until payment is verified
//
//            // Create payment order (amount should be configurable)
//            String amount = "50000"; // 500 INR in paise
//            String currency = "INR";
//            
//            PaymentOrderResponse paymentOrder = paymentService.createPaymentOrder(userDetails, amount, currency);
//            
//            return ResponseEntity.ok(paymentOrder);
//
//        } catch (RazorpayException e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//    /**
//     * Step 2: Verify payment and activate user account
//     */
//    @PostMapping("/verify-payment")
//    public ResponseEntity<PaymentVerificationResponse> verifyPayment(
//            @RequestBody Map<String, String> paymentData) {
//        
//        String razorpayPaymentId = paymentData.get("razorpay_payment_id");
//        String razorpayOrderId = paymentData.get("razorpay_order_id");
//        String razorpaySignature = paymentData.get("razorpay_signature");
//
//        PaymentVerificationResponse response = paymentService.verifyPayment(
//                razorpayPaymentId, razorpayOrderId, razorpaySignature);
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Check payment status
//     */
//    @GetMapping("/payment-status/{orderId}")
//    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String orderId) {
//        try {
//            Users user = paymentService.getUserByOrderId(orderId);
//            
//            return ResponseEntity.ok(Map.of(
//                    "orderId", orderId,
//                    "paymentStatus", user.getPaymentStatus(),
//                    "userActive", user.isActive(),
//                    "email", user.getEmail()
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//}


package com.mockperiod.main.controllers;

import com.mockperiod.main.dto.AdminRegistrationRequest;
import com.mockperiod.main.dto.PaymentOrderResponse;
import com.mockperiod.main.dto.PaymentVerificationResponse;
import com.mockperiod.main.serviceImpl.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment") 
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate-admin-registration")
    public ResponseEntity<PaymentOrderResponse> initiateAdminRegistration(
            @Valid @RequestBody AdminRegistrationRequest request) throws RazorpayException {
        PaymentOrderResponse response = paymentService.initiateAdminRegistration(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(
           
            @RequestParam String razorpay_order_id
          ) {
        PaymentVerificationResponse response = paymentService.verifyAndCompletePayment(
                 razorpay_order_id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plan-amounts")
    public ResponseEntity<Map<String, Integer>> getPlanAmounts() {
        Map<String, Integer> amounts = paymentService.getPlanAmounts();
        return ResponseEntity.ok(amounts);
    } 

    @GetMapping("/check-payment-status/{email}")
    public ResponseEntity<Boolean> checkPaymentStatus(@PathVariable String email) {
        boolean hasPaid = paymentService.hasUserCompletedPayment(email);
        return ResponseEntity.ok(hasPaid);
    }
}
