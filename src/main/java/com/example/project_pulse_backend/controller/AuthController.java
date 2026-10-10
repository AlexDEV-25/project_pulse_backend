package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.AuthResponse;
import com.example.project_pulse_backend.dto.response.CreateAccountResponse;
import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authenticationService;

    @PostMapping("/admin")
    public APIResponse<CreateAccountResponse> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        return APIResponse.<CreateAccountResponse>builder()
                .result(authenticationService.createAccount(request)).build();
    }

    @PostMapping("/log-in")
    APIResponse<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.login(request)).build();
    }

    @PostMapping("/select-role")
    APIResponse<AuthResponse> selectRole(@RequestBody @Valid SelectRoleRequest request) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.selectRole(request)).build();
    }

    @PostMapping("/log-in-google")
    APIResponse<AuthResponse> loginWithGoogle(@RequestParam @Valid String code) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.loginWithGoogle(code)).build();
    }

    @PostMapping("/select-role/google")
    APIResponse<AuthResponse> selectGoogleRole(@RequestBody @Valid SelectGoogleRoleRequest request) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.selectGoogleRole(request)).build();
    }

    @PostMapping("/forgot-password")
    public APIResponse<Void> forgotPassword(@RequestParam @Valid String email) {
        authenticationService.forgotPassword(email);
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    public APIResponse<AuthResponse> resetPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.resetPassword(request)).build();
    }

    @PostMapping("/refresh-token")
    APIResponse<AuthResponse> refreshToken(@RequestBody @Valid TokenRequest request) {
        return APIResponse.<AuthResponse>builder()
                .result(authenticationService.refreshToken(request.getToken())).build();
    }

    @PostMapping("/introspect")
    APIResponse<IntrospectResponse> introspect(@RequestBody @Valid TokenRequest request) {
        return APIResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request.getToken())).build();
    }

    @PutMapping("change-password")
    public APIResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return APIResponse.<Void>builder().build();
    }

}
