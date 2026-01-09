package com.mockperiod.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentOrderResponse {
    private String orderId;
    private Integer amount;
    private String currency;
    private String razorpayKey;
    private String receipt;
    private String description;
}
