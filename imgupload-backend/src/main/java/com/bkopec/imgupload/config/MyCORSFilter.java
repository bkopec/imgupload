package com.bkopec.imgupload.config; // Adjust package as needed

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*; // Use jakarta.servlet for Spring Boot 3+
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this filter runs first
public class MyCORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        System.out.println("CORS filter running...");
    // Set CORS headers for all responses
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); // Echoes the origin
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, HEAD"); // Added PUT, HEAD for completeness
        response.setHeader("Access-Control-Max-Age", "3600");
        // IMPORTANT: Add all custom headers your frontend might send, like 'Authorization' for tokens
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, Authorization");

        // Handle OPTIONS preflight requests
        // If the request method is OPTIONS, just return 200 OK after setting headers
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK); // Or HttpServletResponse.SC_NO_CONTENT (204)
            return; // Terminate the request here for preflights
        }

        // For all other requests (GET, POST, etc.), continue the filter chain
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization logic if needed
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}