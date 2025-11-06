package com.bixan.revest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Autowired(required = false)
    private EnvironmentConfig environmentConfig;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/actuator", "/health", "/metrics", "/favicon.ico");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip logging if environment config is not available or logging is disabled
        if (environmentConfig == null || !environmentConfig.isRequestLoggingEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (shouldExclude(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1000);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(wrappedRequest);

            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, duration);

            // Important: Copy response content back to the original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldExclude(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n=== INCOMING REQUEST ===\n");
        requestLog.append("Method: ").append(request.getMethod()).append("\n");
        requestLog.append("URI: ").append(request.getRequestURI());

        if (request.getQueryString() != null) {
            requestLog.append("?").append(request.getQueryString());
        }
        requestLog.append("\n");

        requestLog.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        requestLog.append("Content Type: ").append(request.getContentType()).append("\n");
        requestLog.append("Content Length: ").append(request.getContentLength()).append("\n");

        // Log headers
        requestLog.append("Headers:\n");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            // Mask sensitive headers
            if (isSensitiveHeader(headerName)) {
                headerValue = "***MASKED***";
            }
            requestLog.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        });

        // Log request body
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = getContentAsString(content, request.getCharacterEncoding());
            if (shouldMaskBody(request)) {
                body = "***SENSITIVE_DATA_MASKED***";
            }
            requestLog.append("Body:\n").append(body).append("\n");
        }

        requestLog.append("========================");
        log.info(requestLog.toString());
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            long duration) {
        StringBuilder responseLog = new StringBuilder();
        responseLog.append("\n=== OUTGOING RESPONSE ===\n");
        responseLog.append("Method: ").append(request.getMethod()).append("\n");
        responseLog.append("URI: ").append(request.getRequestURI()).append("\n");
        responseLog.append("Status: ").append(response.getStatus()).append("\n");
        responseLog.append("Duration: ").append(duration).append(" ms\n");
        responseLog.append("Content Type: ").append(response.getContentType()).append("\n");
        responseLog.append("Content Length: ").append(response.getContentSize()).append("\n");

        // Log response headers
        responseLog.append("Headers:\n");
        response.getHeaderNames().forEach(headerName -> {
            String headerValue = response.getHeader(headerName);
            responseLog.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        });

        // Log response body
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = getContentAsString(content, response.getCharacterEncoding());
            if (shouldMaskResponseBody(request, response)) {
                body = "***SENSITIVE_DATA_MASKED***";
            }
            responseLog.append("Body:\n").append(body).append("\n");
        }

        responseLog.append("=========================");
        log.info(responseLog.toString());
    }

    private String getContentAsString(byte[] content, String encoding) {
        try {
            return new String(content, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "[ENCODING_ERROR: " + e.getMessage() + "]";
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseHeader = headerName.toLowerCase();
        return lowerCaseHeader.contains("authorization") ||
                lowerCaseHeader.contains("password") ||
                lowerCaseHeader.contains("token") ||
                lowerCaseHeader.contains("secret") ||
                lowerCaseHeader.contains("key");
    }

    private boolean shouldMaskBody(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        String contentType = request.getContentType();

        // Mask authentication endpoints
        if (uri.contains("/auth/") || uri.contains("/login")) {
            return true;
        }

        // Mask if content type suggests sensitive data
        if (contentType != null && contentType.contains("application/json")) {
            // Could add more sophisticated detection based on actual content
            return false; // For now, don't mask JSON unless it's auth
        }

        return false;
    }

    private boolean shouldMaskResponseBody(ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response) {
        String uri = request.getRequestURI();

        // Mask authentication responses
        if (uri.contains("/auth/") || uri.contains("/login")) {
            return true;
        }

        // Mask error responses that might contain sensitive info
        if (response.getStatus() >= 400) {
            return false; // Actually, we want to see error details for debugging
        }

        return false;
    }
}