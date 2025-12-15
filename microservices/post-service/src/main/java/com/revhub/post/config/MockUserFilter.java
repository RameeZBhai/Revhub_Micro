package com.revhub.post.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class MockUserFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String username = request.getHeader("X-User-Name");
        // System.out.println("MockUserFilter: Processing request " +
        // request.getRequestURI());
        if (username != null && !username.isEmpty()) {
            System.out.println("MockUserFilter: Found user header: " + username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            // System.out.println("MockUserFilter: No X-User-Name header found");
        }
        filterChain.doFilter(request, response);
    }
}
