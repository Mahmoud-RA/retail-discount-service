package com.d360.retailDiscountService.config.security;

import com.d360.retailDiscountService.config.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import static com.d360.retailDiscountService.config.constants.SecurityConstants.ERROR_PATH;
import static com.d360.retailDiscountService.config.constants.SecurityConstants.ROLE_ADMIN;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String clientId;
    private final String clientSecret;

    public ApiKeyAuthenticationFilter(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestClientId = request.getHeader(SecurityConstants.HEADER_CLIENT_ID);
        String requestClientSecret = request.getHeader(SecurityConstants.HEADER_CLIENT_SECRET);

        if (clientId.equals(requestClientId) && clientSecret.equals(requestClientSecret)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    clientId, null, Collections.singletonList(new SimpleGrantedAuthority(ROLE_ADMIN)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConstants.HEALTH_CHECK_PATH.equals(request.getServletPath()) || ERROR_PATH.equals(request.getServletPath());
    }
}