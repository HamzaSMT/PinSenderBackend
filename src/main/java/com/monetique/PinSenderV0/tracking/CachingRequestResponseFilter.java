package com.monetique.PinSenderV0.tracking;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class CachingRequestResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Wrap the request and response for caching
        HttpServletRequest wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        HttpServletResponse wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        // Proceed with the filter chain
        chain.doFilter(wrappedRequest, wrappedResponse);

        // Ensure the response is copied to the output stream
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) wrappedResponse;
        responseWrapper.copyBodyToResponse();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
