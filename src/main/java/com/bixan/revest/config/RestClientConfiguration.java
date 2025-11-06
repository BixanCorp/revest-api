package com.bixan.revest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@Slf4j
public class RestClientConfiguration {

    @Autowired
    private EnvironmentConfig environmentConfig;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        if (environmentConfig.isRequestLoggingEnabled()) {
            List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
            interceptors.add(new OutgoingRequestLoggingInterceptor());
            restTemplate.setInterceptors(interceptors);
        }

        return restTemplate;
    }

    @Slf4j
    static class OutgoingRequestLoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            // Log outgoing request
            logRequest(request, body);

            // Execute the request
            ClientHttpResponse response = execution.execute(request, body);

            // Log incoming response
            logResponse(response);

            return response;
        }

        private void logRequest(HttpRequest request, byte[] body) {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("\n=== OUTGOING HTTP REQUEST ===\n");
            requestLog.append("Method: ").append(request.getMethod()).append("\n");
            requestLog.append("URI: ").append(request.getURI()).append("\n");

            // Log headers
            requestLog.append("Headers:\n");
            request.getHeaders().forEach((name, values) -> {
                String headerValue = String.join(", ", values);
                // Mask sensitive headers
                if (isSensitiveHeader(name)) {
                    headerValue = "***MASKED***";
                }
                requestLog.append("  ").append(name).append(": ").append(headerValue).append("\n");
            });

            // Log request body
            if (body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                if (shouldMaskBody(request)) {
                    bodyStr = "***SENSITIVE_DATA_MASKED***";
                }
                requestLog.append("Body:\n").append(bodyStr).append("\n");
            }

            requestLog.append("=============================");
            log.info(requestLog.toString());
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("\n=== INCOMING HTTP RESPONSE ===\n");
            responseLog.append("Status: ").append(response.getStatusCode()).append("\n");
            responseLog.append("Status Text: ").append(response.getStatusText()).append("\n");

            // Log headers
            responseLog.append("Headers:\n");
            response.getHeaders().forEach((name, values) -> {
                String headerValue = String.join(", ", values);
                responseLog.append("  ").append(name).append(": ").append(headerValue).append("\n");
            });

            // Log response body (be careful not to consume the stream)
            byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
            if (responseBody.length > 0) {
                String bodyStr = new String(responseBody, StandardCharsets.UTF_8);
                responseLog.append("Body:\n").append(bodyStr).append("\n");
            }

            responseLog.append("==============================");
            log.info(responseLog.toString());
        }

        private boolean isSensitiveHeader(String headerName) {
            String lowerCaseHeader = headerName.toLowerCase();
            return lowerCaseHeader.contains("authorization") ||
                    lowerCaseHeader.contains("password") ||
                    lowerCaseHeader.contains("token") ||
                    lowerCaseHeader.contains("secret") ||
                    lowerCaseHeader.contains("key");
        }

        private boolean shouldMaskBody(HttpRequest request) {
            String uri = request.getURI().toString();
            return uri.contains("/auth/") || uri.contains("/login") || uri.contains("/token");
        }
    }
}