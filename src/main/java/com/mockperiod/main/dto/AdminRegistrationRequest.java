package com.mockperiod.main.dto;

import com.mockperiod.main.entities.Plan;
import lombok.Data;

@Data
public class AdminRegistrationRequest {
    private String name;
    private String email;
    private String phoneNo;
    private String password;
    private String instituteName;
    private String plan; 
    private String instituteEmail;
}