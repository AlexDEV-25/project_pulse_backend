package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.ExchangeTokenRequest;
import com.example.project_pulse_backend.dto.response.ExchangeTokenResponse;
import com.example.project_pulse_backend.dto.response.OutboundUserResponse;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.example.project_pulse_backend.repository.httpClient.OutboundIdentityClient;
import com.example.project_pulse_backend.repository.httpClient.OutboundUserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHelper {
    private final AuthRepo authRepo;
    private final OutboundIdentityClient outboundIdentityClient;
    private final OutboundUserClient outboundUserClient;

    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @Value("${outbound.identity.grant-types}")
    protected String GRANT_TYPES;

    public Auth getAuthenticationByEmail(String email) {
        return authRepo.findByEmail(email).orElseThrow(
                () -> AppException.builder().appError(AppError.ACCOUNT_NOT_FOUND).build());
    }

    public String generateActivationCode() {
        return UUID.randomUUID().toString();
    }

    public Auth authenticateGoogleCode(String code) {
        ExchangeTokenRequest request = new ExchangeTokenRequest(code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                GRANT_TYPES);
        ExchangeTokenResponse exchangeTokenResponse = outboundIdentityClient.exchangeToken(request);
        String bearerToken = "Bearer " + exchangeTokenResponse.getAccessToken();
        OutboundUserResponse userInfo = outboundUserClient.getUserDetails(bearerToken);
        if (!authRepo.existsByEmail(userInfo.getEmail())) {
            throw AppException.builder().appError(AppError.GOOGLE_LOGIN_FAILED).build();
        }
        return getAuthenticationByEmail(userInfo.getEmail());
    }

    public List<String> getRoleNames(Auth authentication) {
        if (authentication.getUser() == null || authentication.getUser().getRoles() == null) {
            return List.of();
        }
        return authentication.getUser().getRoles().stream()
                .map(Role::getName).toList();
    }
    
}
