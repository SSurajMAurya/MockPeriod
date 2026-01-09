package com.mockperiod.main.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.stream.Collectors;

@RestController
public class DebugController {

    @Autowired
    private ApplicationContext context;

    @GetMapping("/debug")
    public String debug() {
        try {
            RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
            String allEndpoints = mapping.getHandlerMethods().keySet().stream()
                    .map(mappingInfo -> mappingInfo.getPatternsCondition().getPatterns())
                    .flatMap(patterns -> patterns.stream())
                    .sorted()
                    .collect(Collectors.joining("<br>"));
            
            return "<h1>All Registered Endpoints:</h1>" + 
                   allEndpoints +
                   "<h1>Test These Swagger URLs:</h1>" +
                   "<a href='/swagger-ui/index.html'>/swagger-ui/index.html</a><br>" +
                   "<a href='/swagger-ui.html'>/swagger-ui.html</a><br>" +
                   "<a href='/swagger-ui/'>/swagger-ui/</a><br>" +
                   "<a href='/v3/api-docs'>/v3/api-docs</a><br>" +
                   "<a href='/api-docs'>/api-docs</a>";
                   
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
