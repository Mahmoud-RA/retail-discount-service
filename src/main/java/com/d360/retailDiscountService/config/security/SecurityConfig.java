package com.d360.retailDiscountService.config.security;

import com.d360.retailDiscountService.config.constants.SecurityConstants;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.d360.retailDiscountService.config.constants.SecurityConstants.ERROR_PATH;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.client-id}")
    private String clientId;

    @Value("${app.security.client-secret}")
    private String clientSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.HEALTH_CHECK_PATH).permitAll()
                        .requestMatchers(ERROR_PATH).permitAll()
                        .requestMatchers(SecurityConstants.BILLS_API_PATH).authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(new ApiKeyAuthenticationFilter(clientId, clientSecret), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Client ID or Secret")))
                .build();
    }
}