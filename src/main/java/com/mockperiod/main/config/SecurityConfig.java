package com.mockperiod.main.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mockperiod.main.jwtUtil.JwtAccessDeniedHandler;
import com.mockperiod.main.jwtUtil.JwtAuthenticationEntryPoint;
import com.mockperiod.main.jwtUtil.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private UserDetailsService userDetailsService;

	@Value("${allowed.origins}")
	private String allowedOrigins;
	private final String[] PUBLIC_URL = { "/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
			"/swagger-resources/**", "/v3/api-docs/**", "/v2/api-docs/**", "/api/admin/**", "/api/swagger-ui.html",
			"/api/swagger-ui/**", "/api/v3/api-docs/**", "/api/v2/api-docs/**", "/api/webjars/**",
			"/api/swagger-resources/**", "/api/configuration/**" };

	@Bean
	public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
		return new JwtAuthenticationEntryPoint();
	}

	@Bean
	public AccessDeniedHandler jwtAccessDeniedHandler() {
		return new JwtAccessDeniedHandler();
	}

	// FIX: Rename this method to match the actual filter class
	@Bean
	public OncePerRequestFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

//        @Bean
//        public CorsConfigurationSource corsConfigurationSource() {
//            CorsConfiguration configuration = new CorsConfiguration();
//            
//            if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
//                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
//            } else {
//                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
//            }
//            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//            configuration.setAllowedHeaders(Arrays.asList("*"));
//            configuration.setAllowCredentials(true);
//            configuration.setMaxAge(3600L);
//
//            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//            source.registerCorsConfiguration("/**", configuration);
//            return source;
//        }

	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http

				.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(request -> {
					CorsConfiguration corsConfig = new CorsConfiguration();
					// corsConfig.setAllowedOrigins(Arrays.asList("http://3.92.79.14:3000")); //
					// Customize this as needed
//	            corsConfig.setAllowedOrigins(Arrays.asList("*")); // Customize this as needed
					corsConfig.setAllowedOriginPatterns(List.of("*"));
					corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
					corsConfig.setAllowedHeaders(Arrays.asList("*"));
					corsConfig.setAllowCredentials(true);
					corsConfig.setMaxAge(3600L); // Cache the preflight response for 3600 seconds
					return corsConfig;
				}))

				.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URL).permitAll().requestMatchers("/chapters")
						.authenticated().requestMatchers("/exams").authenticated().requestMatchers("/notifications")
						.authenticated().requestMatchers("/options").authenticated().requestMatchers("/questions")
						.authenticated().requestMatchers("/subjects").authenticated().requestMatchers("/tests")
						.authenticated().requestMatchers("/test-institute-timings").authenticated()
						.requestMatchers("/users").authenticated().anyRequest().permitAll())
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint())
						.accessDeniedHandler(jwtAccessDeniedHandler()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// FIX: Use the corrected bean name
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}