package com.monetique.PinSenderV0.security.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
          throws IOException, ServletException {

    // Logging details for the unauthorized access attempt
    String requestMethod = request.getMethod();
    String requestUri = request.getRequestURI();
    String clientIp = request.getRemoteAddr();
    String userAgent = request.getHeader("User-Agent");

    logger.error("Unauthorized access attempt detected");
    logger.error("Request Details: Method - {}, URI - {}, IP - {}, User-Agent - {}", requestMethod, requestUri, clientIp, userAgent);
    logger.error("Error Message: {}", authException.getMessage());

    // Prepare the response body
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    final Map<String, Object> body = new HashMap<>();
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("message", authException.getMessage());
    body.put("path", requestUri);
    body.put("method", requestMethod);
    body.put("clientIp", clientIp);
    body.put("userAgent", userAgent);

    // Serialize the response body to JSON
    final ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), body);
  }

}