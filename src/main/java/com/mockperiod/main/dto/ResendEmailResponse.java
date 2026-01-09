package com.mockperiod.main.dto;

import lombok.Data;

@Data
public class ResendEmailResponse {
    private String id;
    private String from;
    private String to;
    private String subject;
    private String html;
    private String text;
    private String createdAt;
}
