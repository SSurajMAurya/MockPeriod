package com.mockperiod.main.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "MockPeriod - User Module API",
        version = "1.0",
        description = """
            Comprehensive API documentation for MockPeriod's User Management Module.
            <br><br>
            <strong>Authentication:</strong> Use <code>Bearer Token</code> (JWT) for secured endpoints.
            """,
        contact = @Contact(
            name = "MockPeriod Support",
            email = "support@mockperiod.com",
            url = "https://mockperiod.com/support"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    		servers = {
    			    @Server(url = "http://localhost:8080/api", description = "Local Development"),
    			    @Server(url = "https://dev.mockperiod.com/api", description = "Development Server"),
    			    @Server(url = "https://api.mockperiod.com/api", description = "Production Server")
    			},
    security = @SecurityRequirement(name = "bearerAuth") 
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "JWT Authorization header using the Bearer scheme. Example: 'Bearer {token}'"
)
public class SwaggerConfig {
    // Configuration is complete with annotations
}