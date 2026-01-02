package com.rushcrew.product.global.security.filter;

import com.rushcrew.product.global.security.model.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        // internal 경로는 JWT 검사 없이 바로 통과시키기
        if (requestURI.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        String email = request.getHeader(USER_NAME_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);

        if (userId == null || email == null || role == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(Long.valueOf(userId), email, role),
                null,
                List.of(() -> "ROLE_" + role)
            );

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}

