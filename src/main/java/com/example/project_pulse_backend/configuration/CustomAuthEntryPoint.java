package com.example.project_pulse_backend.configuration;

import com.example.project_pulse_backend.dto.response.APIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        APIResponse<String> apiResponse = new APIResponse<>();
        apiResponse.setCode(1111);
        apiResponse.setMessage("UNAUTHENTICATED");

        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
