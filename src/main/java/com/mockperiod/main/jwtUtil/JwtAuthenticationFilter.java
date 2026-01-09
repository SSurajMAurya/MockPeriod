package com.mockperiod.main.jwtUtil;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mockperiod.main.serviceImpl.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;




@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Define public URLs that don't require authentication
    private final List<String> PUBLIC_URLS = Arrays.asList(
        "/api/auth/",
        "/swagger-ui/",
        "/v3/api-docs",
        "/v2/api-docs",
        "/swagger-resources/",
        "/webjars/",
        "/api/admin/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        // Check if this is a public URL
        boolean isPublicUrl = PUBLIC_URLS.stream().anyMatch(path::startsWith);
        
        // For public URLs, skip JWT processing entirely
        if (isPublicUrl) {
            logger.debug("Skipping JWT filter for public path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // For protected URLs, process JWT token
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found for protected path: " + path);
            // For protected URLs without token, let Spring Security handle the authentication failure
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String email = null;

        try {
            email = jwtUtil.getUsernameFromToken(jwt);
        } catch (Exception e) {
            logger.error("Error extracting email from token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authenticated user: " + email);
            }
        }
        filterChain.doFilter(request, response);
    }
}