package com.monetique.PinSenderV0.security.jwt;

import java.io.IOException;

import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.tracking.ItrackingingService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;
  @Autowired
  ItrackingingService trackingingService;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    try {
      // Extract JWT from the request
      String jwt = parseJwt(request);

      // If JWT is present and valid
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        logger.info("username from token is "+ username);
        Long sessionId = jwtUtils.getSessionIdFromJwtToken(jwt); // Extract sessionId from JWT
        // Check if the session is valid (i.e., not logged out)
        UserSession session = trackingingService.getSessionById(sessionId);
        if (session != null && session.getLogoutTime() != null) {
          // Session is logged out, reject the request
          logger.info("Session ID is invalid: " + sessionId);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write("Session ID is invalid");
          return; // Stop further processing
        }

        // If session is valid, authenticate the user
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("JWT Token is expired");
      return; // Stop further processing
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Authentication error");
      return; // Stop further processing
    }

    // Continue with the filter chain if everything is valid
    filterChain.doFilter(request, response);
  }






  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}