package com.example.project_pulse_backend.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class ExchangeTokenResponse {
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private String scope;
    private String tokenType;
}