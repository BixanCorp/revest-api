package com.bixan.revest.auth.interceptor;

import com.bixan.revest.auth.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SessionValidationInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionService sessionService;

    // Endpoints that don't require session validation
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/logout",
            "/auth/validate-session", // Allow checking for session without requiring one
            "/auth/health",
            "/auth/validate",
            "/api/users", // POST for user creation during signup
            "/health",
            "/error");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Always allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            log.debug("Allowing OPTIONS preflight request for: {}", requestPath);
            return true;
        }

        // Skip validation for excluded paths
        if (isExcludedPath(requestPath, method)) {
            log.debug("Skipping session validation for excluded path: {} {}", method, requestPath);
            return true;
        }

        log.debug("Validating session for: {} {}", method, requestPath);

        // Validate session
        SessionService.SessionValidationResult validationResult = sessionService.validateSession(request, response);

        if (!validationResult.isValid()) {
            log.debug("Session validation failed for {} {}: {}", method, requestPath,
                    validationResult.getErrorMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"success\":false,\"message\":\"Unauthorized: %s\"}",
                    validationResult.getErrorMessage()));
            return false;
        }

        // Add user ID to request attributes for use in controllers
        request.setAttribute("userId", validationResult.getUserId());
        request.setAttribute("firebaseToken", validationResult.getFirebaseToken());

        log.debug("Session validation successful for user: {}", validationResult.getUserId());
        return true;
    }

    private boolean isExcludedPath(String requestPath, String method) {
        // Check if path is in excluded list
        if (EXCLUDED_PATHS.contains(requestPath)) {
            return true;
        }

        // Special case: POST /api/users is allowed for user creation during signup
        if ("POST".equals(method) && "/api/users".equals(requestPath)) {
            return true;
        }

        // Check for patterns (e.g., health checks, static resources)
        if (requestPath.startsWith("/health") ||
                requestPath.startsWith("/error") ||
                requestPath.startsWith("/static/") ||
                requestPath.contains("/webjars/")) {
            return true;
        }

        return false;
    }
}