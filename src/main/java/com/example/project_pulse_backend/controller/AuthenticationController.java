package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.CreateAccountResponse;
import com.example.project_pulse_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping
    public APIResponse<CreateAccountResponse> create(@RequestBody @Valid CreateAccountRequest request) {
        return APIResponse.<CreateAccountResponse>builder()
                .result(authenticationService.createAccount(request)).build();
    }
}
