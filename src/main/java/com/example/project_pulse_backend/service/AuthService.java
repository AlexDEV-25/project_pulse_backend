package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.CreateBodyEmailHelper;
import com.example.project_pulse_backend.helper.EmailHelper;
import com.example.project_pulse_backend.helper.GetAuthByToken;
import com.example.project_pulse_backend.helper.JwtHelper;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import com.example.project_pulse_backend.repository.httpClient.OutboundIdentityClient;
import com.example.project_pulse_backend.repository.httpClient.OutboundUserClient;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepo authenticationRepo;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final PasswordEncoder passwordEncoder;
    private final GetAuthByToken getAuthByToken;
    private final JwtHelper jwtHelper;
    private final EmailHelper emailHelper;
    private final CreateBodyEmailHelper createBodyEmailHelper;
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

    @PreAuthorize("hasAuthority('CREATE_ACCOUNT')")
    public CreateAccountResponse createAccount(CreateAccountRequest request) {

        if (authenticationRepo.existsByEmail(request.getEmail())) {
            throw AppException.builder().appError(AppError.EMAIL_ALREADY_EXISTS).build();
        }

        User newUser = createUser(request);

        Auth authentication = Auth.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .user(newUser)
                .enabled(true)
                .build();
        Auth newAuth = authenticationRepo.save(authentication);

        return CreateAccountResponse.builder()
                .email(newAuth.getEmail())
                .userName(newAuth.getUser().getUserName())
                .position(newAuth.getUser().getPosition())
                .resourceRate(newAuth.getUser().getResourceRate())
                .build();
    }

    public AuthResponse login(AuthRequest request) {

        Auth authentication = getAuthenticationByEmail(request.getEmail());


        if (!authentication.isEnabled()) {
            throw AppException.builder().appError(AppError.ACCOUNT_LOCKED).build();
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), authentication.getPassword());

        if (!authenticated) {
            throw AppException.builder().appError(AppError.LOGIN_FAILED).build();
        }

        AuthResponse response = new AuthResponse();
        response.setAuthenticated(true);
        String token = jwtHelper.generateToken(authentication);
        response.setToken(token);

        return response;
    }

    public AuthResponse refreshToken(String oldToken) {
        try {
            SignedJWT signToken = jwtHelper.verifyToken(oldToken);

            String email = signToken.getJWTClaimsSet().getSubject();
            Auth authentication = getAuthenticationByEmail(email);
            AuthResponse response = new AuthResponse();
            String token = jwtHelper.generateToken(authentication);
            response.setAuthenticated(true);
            response.setToken(token);

            return response;
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
        Auth authentication = getAuthenticationByEmail(email);
        String forgotPassWordCode = this.generateActivationCode();

        authentication.setForgotPasswordCode(forgotPassWordCode);
        authenticationRepo.save(authentication);

        emailHelper.sendEmail(
                SendEmailRequest.builder()
                        .to(Recipient.builder()
                                .email(authentication.getEmail())
                                .build())
                        .subject("Đổi mật khẩu tài khoản của bạn")
                        .htmlContent(createBodyEmailHelper.bodyChangePassword(authentication.getEmail(), forgotPassWordCode))
                        .build());
    }


    @PreAuthorize("hasAuthority('TOGGLE_ACCOUNT_STATUS')")
    public void toggleAccountStatus(Long id, DisplayRequest request) {
        Auth authentication = authenticationRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.ACCOUNT_NOT_FOUND).build());

        authentication.setEnabled(request.isEnabled());

        Auth result = authenticationRepo.save(authentication);

        if (request.isEnabled()) {
            emailHelper.sendEmail(
                    SendEmailRequest.builder()
                            .to(Recipient.builder()
                                    .email(authentication.getEmail())
                                    .build())
                            .subject("Tài khoản của bạn tạm thời bị khóa")
                            .htmlContent(createBodyEmailHelper.bodyLockAccount())
                            .build());
        } else {
            emailHelper.sendEmail(
                    SendEmailRequest.builder()
                            .to(Recipient.builder()
                                    .email(authentication.getEmail())
                                    .build())
                            .subject("Tài khoản của bạn đã được mở")
                            .htmlContent(createBodyEmailHelper.bodyUnLockAccount())
                            .build());
        }
    }

    public AuthResponse resetPassword(ForgotPasswordRequest request) {
        Auth authentication = getAuthenticationByEmail(request.getEmail());
        if (!authentication.isEnabled()) {
            throw AppException.builder().appError(AppError.ACCOUNT_LOCKED).build();
        }

        if (authentication.getForgotPasswordCode().equals(request.getForgotPasswordCode())) {
            authentication.setForgotPasswordCode(null);
            authentication.setPassword(passwordEncoder.encode(request.getPassword()));
            Auth result = authenticationRepo.save(authentication);
            return AuthResponse.builder()
                    .authenticated(true)
                    .token(jwtHelper.generateToken(result))
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

    public AuthResponse loginWithGoogle(String code) {
        ExchangeTokenRequest request = new ExchangeTokenRequest(code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                GRANT_TYPES);
        AuthResponse response = new AuthResponse();
        try {
            ExchangeTokenResponse exchangeTokenResponse = outboundIdentityClient.exchangeToken(request);
            String bearerToken = "Bearer " + exchangeTokenResponse.getAccessToken();
            OutboundUserResponse userInfo = outboundUserClient.getUserDetails(bearerToken);
            boolean exist = authenticationRepo.existsByEmail(userInfo.getEmail());

            if (!exist) {
                throw AppException.builder().appError(AppError.GOOGLE_LOGIN_FAILED).build();
            } else {
                Auth authentication = getAuthenticationByEmail(userInfo.getEmail());
                String token = jwtHelper.generateToken(authentication);
                response.setToken(token);
                response.setAuthenticated(true);
                return response;
            }
        } catch (Exception e) {
            throw AppException.builder().appError(AppError.GOOGLE_LOGIN_FAILED).build();
        }
    }

    private Auth getAuthenticationByEmail(String email) {
        return authenticationRepo.findByEmail(email).orElseThrow(
                () -> AppException.builder().appError(AppError.ACCOUNT_NOT_FOUND).build());
    }

    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }


    private User createUser(CreateAccountRequest request) {

        List<Role> roles = roleRepo.findAllById(request.getRoles());
        Department department = departmentRepo.findById(request.getDepartmentId())
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());

        User newUser = User.builder()
                .userName(request.getUserName())
                .roles(roles)
                .position(request.getPosition())
                .resourceRate(request.getResourceRate())
                .department(department)
                .build();
        return userRepo.save(newUser);
    }
}

