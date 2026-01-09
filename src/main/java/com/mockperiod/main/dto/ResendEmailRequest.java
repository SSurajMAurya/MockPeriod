package com.mockperiod.main.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResendEmailRequest {
    private String from;
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String html;
    private String text;
    private List<Attachment> attachments;
    
    @Data
    public static class Attachment {
        private String filename;
        private byte[] content;
        private String path;
    }
}

