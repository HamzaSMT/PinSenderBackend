package com.monetique.PinSenderV0.tracking;

import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.security.jwt.JwtUtils;
import com.monetique.PinSenderV0.tracking.ItrackingingService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ApiRequestTrackingInterceptor implements HandlerInterceptor {

    @Autowired
    private ItrackingingService trackingService;

    @Autowired
    private JwtUtils jwtUtils;  // Utility class to extract session info from JWT

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Start tracking response time
        request.setAttribute("startTime", Instant.now().toEpochMilli());

        // Optionally capture request body, IP, etc.
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        request.setAttribute("ipAddress", ipAddress);
        request.setAttribute("userAgent", userAgent);

        return true;
    }

    private String maskSensitiveData(String data, List<String> sensitiveFields) {
        for (String field : sensitiveFields) {
            data = data.replaceAll("(?i)\"" + field + "\"\\s*:\\s*\"(.*?)\"", "\"" + field + "\":\"[FILTERED]\"");
        }
        return data;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long responseTimeMs = endTime - startTime;

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        int statusCode = response.getStatus();
        Long sessionId = 0L;

        // Extract JWT token and session ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                token = token.substring(7);  // Strip "Bearer " prefix
                sessionId = jwtUtils.getSessionIdFromJwtToken(token);
            } catch (Exception jwtEx) {

                sessionId = 0L; // Default or handle as needed
            }
        }


        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        // Define sensitive fields to filter
        List<String> sensitiveFields = List.of("password", "Authorization", "token", "logo");

        // Handle multipart requests
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            StringBuilder multipartData = new StringBuilder();
            // Log text fields and mask sensitive data
            Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String maskedValue = maskSensitiveData(Arrays.toString(entry.getValue()), sensitiveFields);
                multipartData.append(entry.getKey()).append("=").append(maskedValue).append("; ");
            }

            // Log file details (skip file content)
            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getMultiFileMap().toSingleValueMap().entrySet()) {
                multipartData.append("File field: ").append(entry.getKey())
                        .append(", File name: ").append(entry.getValue().getOriginalFilename()).append("; ");
            }

            // Capture response body and mask sensitive data
            ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            responseBody = maskSensitiveData(responseBody, sensitiveFields); // Mask sensitive data in response
            wrappedResponse.copyBodyToResponse();

            // Log the multipart request with the masked response body
            trackingService.logRequest(
                    sessionId,
                    requestPath,
                    HttpMethodEnum.valueOf(method),
                    statusCode,
                    responseTimeMs,
                    ipAddress,
                    userAgent,
                    0L,  // Skip request size for multipart
                    wrappedResponse.getContentSize(),
                    ex != null ? ex.getMessage() : null,
                    multipartData.toString(),
                    responseBody
            );
        }
        // Handle regular requests with request/response body logging
        else {
            try {
                ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;
                ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;

                // Capture request and response bodies
                String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
                String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

                // Mask sensitive data
                requestBody = maskSensitiveData(requestBody, sensitiveFields);
                responseBody = maskSensitiveData(responseBody, sensitiveFields);

                wrappedResponse.copyBodyToResponse(); // Ensure response is flushed

                // Log normal request and response bodies with masked sensitive data
                trackingService.logRequest(
                        sessionId,
                        requestPath,
                        HttpMethodEnum.valueOf(method),
                        statusCode,
                        responseTimeMs,
                        ipAddress,
                        userAgent,
                        wrappedRequest.getContentLengthLong(),
                        wrappedResponse.getContentSize(),
                        ex != null ? ex.getMessage() : null,
                        requestBody.isEmpty() ? "No Request Body" : requestBody,
                        responseBody.isEmpty() ? "No Response Body" : responseBody
                );
            } catch (ClassCastException e) {
                System.err.println("Request is not cacheable: " + e.getMessage());
            }
        }
    }


  /*  @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long responseTimeMs = endTime - startTime;

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        int statusCode = response.getStatus();
        Long sessionId = 0L;

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Strip "Bearer " prefix
            sessionId = jwtUtils.getSessionIdFromJwtToken(token);
        }


        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        // Handle multipart requests separately for request body
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            StringBuilder multipartData = new StringBuilder();
            // Log text fields
            Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                multipartData.append(entry.getKey()).append("=");
                for (String value : entry.getValue()) {
                    multipartData.append(value).append(", ");
                }
                multipartData.setLength(multipartData.length() - 2); // Remove last comma
                multipartData.append(";");
            }

            // Log file details, skip file content
            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getMultiFileMap().toSingleValueMap().entrySet()) {
                multipartData.append("File field: ").append(entry.getKey())
                        .append(", File name: ").append(entry.getValue().getOriginalFilename()).append(";");
            }

            // Capture response body
            ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            wrappedResponse.copyBodyToResponse(); // Ensure response body is written back to client

            // Log multipart request with response body
            trackingService.logRequest(
                    sessionId,
                    requestPath,
                    HttpMethodEnum.valueOf(method),
                    statusCode,
                    responseTimeMs,
                    ipAddress,
                    userAgent,
                    0L,  // Skip request size for multipart
                    wrappedResponse.getContentSize(),
                    ex != null ? ex.getMessage() : null,
                    multipartData.toString(),
                    responseBody
            );
        }
        // Handle regular requests with request/response body logging
        else {
            try {
                ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;
                ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;

                // Capture request and response bodies
                String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
                String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

                wrappedResponse.copyBodyToResponse(); // Ensure response is flushed

                if (requestBody.isEmpty()) {
                    requestBody = "No Request Body";
                }
                if (responseBody.isEmpty()) {
                    responseBody = "No Response Body";
                }

                // Log normal request and response bodies
                trackingService.logRequest(
                        sessionId,
                        requestPath,
                        HttpMethodEnum.valueOf(method),
                        statusCode,
                        responseTimeMs,
                        ipAddress,
                        userAgent,
                        wrappedRequest.getContentLengthLong(),
                        wrappedResponse.getContentSize(),
                        ex != null ? ex.getMessage() : null,
                        requestBody,
                        responseBody
                );
            } catch (ClassCastException e) {
                System.err.println("Request is not cacheable: " + e.getMessage());
            }
        }
    }*/

}
