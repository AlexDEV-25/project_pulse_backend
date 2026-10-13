package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.AuthResponse;
import com.example.project_pulse_backend.dto.response.CreateAccountResponse;
import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepo authenticationRepo;
    private final AuthHelper authHelper;
    private final PasswordEncoder passwordEncoder;
    private final GetAuthByToken getAuthByToken;
    private final JwtHelper jwtHelper;
    private final EmailHelper emailHelper;
    private final UserHelper userHelper;

    @PreAuthorize("hasAuthority('CREATE_ACCOUNT')")
    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        if (authenticationRepo.existsByEmail(request.getEmail())) {
            throw AppException.builder().appError(AppError.EMAIL_ALREADY_EXISTS).build();
        }

        User newUser = userHelper.createUser(request);

        Auth authentication = Auth.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .user(newUser)
                .build();

        Auth newAuth = authenticationRepo.save(authentication);

        return CreateAccountResponse.builder()
                .email(newAuth.getEmail())
                .userName(newAuth.getUser().getUserName())
                .position(newAuth.getUser().getPosition())
                .resourceRate(newAuth.getUser().getResourceRate())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        Auth authentication = authHelper.getAuthenticationByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), authentication.getPassword())) {
            throw AppException.builder().appError(AppError.LOGIN_FAILED).build();
        }

        return AuthResponse.builder()
                .authenticated(true)
                .roles(authHelper.getRoleNames(authentication))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse selectRole(SelectRoleRequest request) {
        Auth authentication = authHelper.getAuthenticationByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), authentication.getPassword())) {
            throw AppException.builder().appError(AppError.LOGIN_FAILED).build();
        }

        return createRoleTokenResponse(authentication, request.getRole());
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String oldToken) {
        try {
            SignedJWT signToken = jwtHelper.verifyToken(oldToken);

            String email = signToken.getJWTClaimsSet().getSubject();
            if (email == null || email.isBlank()) {
                throw AppException.builder().appError(AppError.INVALID_TOKEN).build();
            }

            Auth authentication = authHelper.getAuthenticationByEmail(email);
            String selectedRole = signToken.getJWTClaimsSet().getStringClaim("role");
            return createRoleTokenResponse(authentication, selectedRole);
        } catch (ParseException e) {
            throw AppException.builder().appError(AppError.FAILED_TO_PARSE_DATA).build();
        } catch (JOSEException e) {
            throw AppException.builder().appError(AppError.JOSE_PROCESSING_ERROR).build();
        }

    }

    public IntrospectResponse introspect(String token) {
        try {
            return jwtHelper.introspect(token);
        } catch (ParseException e) {
            throw AppException.builder().appError(AppError.FAILED_TO_PARSE_DATA).build();
        } catch (JOSEException e) {
            throw AppException.builder().appError(AppError.JOSE_PROCESSING_ERROR).build();
        }

    }

    public void forgotPassword(String email) {
        Auth authentication = authHelper.getAuthenticationByEmail(email);
        String forgotPassWordCode = authHelper.generateActivationCode();
        emailHelper.sendEmail(
                SendEmailRequest.builder()
                        .to(Recipient.builder()
                                .email(authentication.getEmail())
                                .build())
                        .subject("Đổi mật khẩu tài khoản của bạn")
                        .htmlContent(emailHelper.bodyChangePassword(forgotPassWordCode))
                        .build());
        authentication.setForgotPasswordCode(forgotPassWordCode);
        authentication.setForgotPasswordCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        authenticationRepo.save(authentication);
    }

    @Transactional
    public AuthResponse resetPassword(ForgotPasswordRequest request) {
        Auth authentication = authHelper.getAuthenticationByEmail(request.getEmail());

        if (authentication.getForgotPasswordCode() != null
                && authentication.getForgotPasswordCode().equals(request.getForgotPasswordCode())
                && authentication.getForgotPasswordCodeExpiresAt() != null
                && authentication.getForgotPasswordCodeExpiresAt().isAfter(LocalDateTime.now())) {
            authentication.setForgotPasswordCode(null);
            authentication.setForgotPasswordCodeExpiresAt(null);
            authentication.setPassword(passwordEncoder.encode(request.getPassword()));
            Auth result = authenticationRepo.save(authentication);
            return AuthResponse.builder()
                    .authenticated(true)
                    .roles(authHelper.getRoleNames(result))
                    .build();
        } else {
            throw AppException.builder().appError(AppError.INVALID_VERIFICATION_CODE).build();
        }
    }

    @PreAuthorize("hasAuthority('CHANGE_PASSWORD')")
    public void changePassword(ChangePasswordRequest request) {
        Auth auth = getAuthByToken.get();
        boolean isMatch = passwordEncoder.matches(
                request.getOldPassword(),
                auth.getPassword()
        );
        if (isMatch) {
            auth.setPassword(passwordEncoder.encode(request.getNewPassword()));
            authenticationRepo.save(auth);
        } else {
            throw AppException.builder().appError(AppError.INCORRECT_PASSWORD).build();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse loginWithGoogle(String code) {
        Auth authentication;
        try {
            authentication = authHelper.authenticateGoogleCode(code);
        } catch (Exception e) {
            throw AppException.builder().appError(AppError.GOOGLE_LOGIN_FAILED).build();
        }
        return AuthResponse.builder()
                .authenticated(true)
                .roles(authHelper.getRoleNames(authentication))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse selectGoogleRole(SelectGoogleRoleRequest request) {
        Auth authentication;
        try {
            authentication = authHelper.authenticateGoogleCode(request.getCode());
        } catch (Exception e) {
            throw AppException.builder().appError(AppError.GOOGLE_LOGIN_FAILED).build();
        }
        return createRoleTokenResponse(authentication, request.getRole());
    }


    private AuthResponse createRoleTokenResponse(Auth authentication, String role) {
        if (authentication.getUser().getRoles().stream()
                .noneMatch(assignedRole -> assignedRole.getName().equals(role))) {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }

        return AuthResponse.builder()
                .authenticated(true)
                .role(role)
                .token(jwtHelper.generateToken(authentication, role))
                .roles(authHelper.getRoleNames(authentication))
                .build();
    }

}
