package com.mockperiod.main.jwtUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mockperiod.main.entities.Users;
import com.mockperiod.main.exceptions.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtils {
    
    // Use a proper secret key - in production, inject this from application properties
    private final String secret = "yourSuperSecretKeyThatIsAtLeast32CharactersLong123!";
    private final SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    
    private static final long JWT_TOKEN_VALIDITY = TimeUnit.HOURS.toMillis(12); // 12 hours
    private static final String ISSUER = "mockperiod-app";

    public String generateToken(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return generateToken(principal);
    }

    public String generateToken(UserDetails principal) {
        try {
            Users user = (Users) principal;
            String subject = user.getEmail();
            Long userId = user.getId();
            
            if (!user.isActive()) {
                log.error("User not active, so cannot generate a token");
                throw new CustomException("User account is not active", org.springframework.http.HttpStatus.FORBIDDEN);
            }
            
            String role = user.getRole().toString();

            if (subject == null || subject.isEmpty()) {
                throw new CustomException("User email cannot be null or empty", org.springframework.http.HttpStatus.BAD_REQUEST);
            }

            return Jwts.builder()
                    .setSubject(subject)
                    .claim("userId", userId)
                    .claim("role", role)
                    .setIssuer(ISSUER)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
                    
        } catch (Exception e) {
            log.error("Error: couldn't generate token", e);
            throw new CustomException("Could not generate token: " + e.getMessage(), 
                                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Generate Token using only Email (for special cases)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT claims", e);
            throw new CustomException("Invalid token: " + e.getMessage(), 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.error("Token expired while getting username");
            throw new CustomException("Token expired. Please log in again.", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Error getting username from token: {}", e.getMessage());
            throw new CustomException("Invalid token: " + e.getMessage(), 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("Error extracting User Id from token: {}", e.getMessage());
            throw new CustomException("Could not extract user ID from token", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role from token: {}", e.getMessage());
            throw new CustomException("Could not extract role from token", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean isValidToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            
            // Check if token is expired
            if (expiration.before(new Date())) {
                log.warn("Token has expired");
                return false;
            }
            
            // Additional validation checks can be added here
            // For example, check if the token is in a revocation list
            
            return true;
            
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            return isValidToken(token);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature!");
            throw new CustomException("Invalid token signature", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token!");
            throw new CustomException("Invalid token format", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired!");
            throw new CustomException("Token expired. Please log in again.", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported!");
            throw new CustomException("Unsupported token", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty!");
            throw new CustomException("Token claims are empty", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("An unexpected error occurred during token validation!", e);
            throw new CustomException("Token validation failed", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            log.error("Token expired while getting expiration date");
            throw new CustomException("Token expired", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Error getting expiration date from token: {}", e.getMessage());
            throw new CustomException("Could not get token expiration", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    public Date getIssuedAtDateFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getIssuedAt();
        } catch (Exception e) {
            log.error("Error getting issued at date from token: {}", e.getMessage());
            throw new CustomException("Could not get token issue date", 
                                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    // Helper method to check if token will expire within a certain time
    public boolean willTokenExpireSoon(String token, long minutes) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long timeUntilExpiry = expiration.getTime() - System.currentTimeMillis();
            return timeUntilExpiry <= TimeUnit.MINUTES.toMillis(minutes);
        } catch (Exception e) {
            log.error("Error checking token expiration soon: {}", e.getMessage());
            return true; // Assume it's expiring soon if we can't check
        }
    }

    // Method to get all claims as a convenient method
    public JwtInfo getJwtInfo(String token) {
        Claims claims = getClaims(token);
        return JwtInfo.builder()
                .username(claims.getSubject())
                .userId(claims.get("userId", Long.class))
                .role(claims.get("role", String.class))
                .issuedAt(claims.getIssuedAt())
                .expiration(claims.getExpiration())
                .issuer(claims.getIssuer())
                .build();
    }

    // Inner class to hold JWT information
    @lombok.Builder
    @lombok.Data
    public static class JwtInfo {
        private String username;
        private Long userId;
        private String role;
        private Date issuedAt;
        private Date expiration;
        private String issuer;
    }
}