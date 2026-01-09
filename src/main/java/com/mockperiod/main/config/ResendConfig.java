package com.mockperiod.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ResendConfig {
    
    @Value("${resend.api-key}")
    private String apiKey;
    
    @Value("${resend.api-url:https://api.resend.com}")
    private String apiUrl;
    
    @Bean
    public WebClient resendWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
