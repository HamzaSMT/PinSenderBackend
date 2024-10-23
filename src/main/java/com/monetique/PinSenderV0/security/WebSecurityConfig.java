package com.monetique.PinSenderV0.security;

import com.monetique.PinSenderV0.security.jwt.AuthEntryPointJwt;
import com.monetique.PinSenderV0.security.jwt.AuthTokenFilter;
import com.monetique.PinSenderV0.security.jwt.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf  (csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**",  "/api/test/**").permitAll()
                    .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()
                    .anyRequest().authenticated())
             .cors(cors -> cors.configurationSource(request -> {
              CorsConfiguration corsConfig = new CorsConfiguration();
              corsConfig.setExposedHeaders(Collections.singletonList("Set-Cookie")); // Allow the Set-Cookie header to be visiblecorsConfig.setAllowCredentials(true); // This is essential for cookies
              corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
              corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
              corsConfig.setAllowedHeaders(Arrays.asList("*"));
              corsConfig.setAllowCredentials(true);
              return corsConfig;
            }))
            .headers(headers -> {
              headers.addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"));
              headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'"));
              headers.frameOptions(frameOptions -> frameOptions.deny());
              headers.addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "default-src 'self'"));
              headers.addHeaderWriter(new StaticHeadersWriter("X-WebKit-CSP", "default-src 'self'"));
            });

    // Configure anonymous users behavior
    http.anonymous(anonymousConfigurer -> anonymousConfigurer.disable());

    // Add JWT filter before username/password authentication filter
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

}
