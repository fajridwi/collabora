package com.manajemennilai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.manajemennilai.security.JwtUtils;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        logger.debug("Processing request for path: {}", requestPath);

        // Skip filter for /api/auth/** endpoints
        if (requestPath.startsWith("/api/auth/")) {
            logger.debug("Skipping JWT filter for auth endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Process JWT token
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        logger.debug("Authorization Header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("Extracted Token: {}", token);
            try {
                username = jwtUtils.extractUsername(token);
                logger.debug("Extracted Username from Token: {}", username);
            } catch (Exception e) {
                logger.error("Error extracting username from token: {}", e.getMessage());
            }
        } else {
            logger.debug("No valid Authorization header found");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("SecurityContext empty, continuing with user authentication...");

            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("Loaded user details for: {}", userDetails.getUsername());

                if (jwtUtils.validateToken(token, userDetails)) {
                    logger.debug("Token is valid for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in context for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Failed to authenticate user: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}