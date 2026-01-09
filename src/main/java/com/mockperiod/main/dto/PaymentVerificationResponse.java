package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVerificationResponse {
    private boolean success;
    private String message;
    private String paymentId;
    private String orderId;
    private String adminUserId;
    private String email;
}
