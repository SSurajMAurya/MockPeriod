package com.mockperiod.main.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String name;
    private String role;
    private Long id;
    private Long instituteId;
    private String instituteEmail;
    private String plan;
    private String expiryDate;
    private String paymentStatus;
    
    public JwtResponse(String token, String email,
    		String name, String role, Long id , Long instituteId ,
    		String instituteEmail ,String plan , String expiryDate,String paymentStatus) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
        this.id = id;
        this.instituteId = instituteId;
        this.instituteEmail = instituteEmail;
        this.plan = plan;
        this.expiryDate = expiryDate;
        this.paymentStatus = paymentStatus;
    }
}
