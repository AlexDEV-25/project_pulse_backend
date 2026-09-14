package com.example.project_pulse_backend.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class ExchangeTokenRequest {

    @NotBlank(message = "code không được để trống")
    private String code;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String grantType;
}