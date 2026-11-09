package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.AuthMapper;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final AuthRepo repository = mock(AuthRepo.class);
    private final AuthHelper authHelper = mock(AuthHelper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final GetAuthByToken authByToken = mock(GetAuthByToken.class);
    private final JwtHelper jwtHelper = mock(JwtHelper.class);
    private final EmailHelper emailHelper = mock(EmailHelper.class);
    private final UserHelper userHelper = mock(UserHelper.class);
    private final AuthMapper mapper = mock(AuthMapper.class);
    private final AuthService service = new AuthService(repository, authHelper, passwordEncoder, authByToken,
            jwtHelper, emailHelper, userHelper, mapper);

    @Test
    void accountLoginRoleSelectionAndGoogleMethodsWork() throws Exception {
        User user = User.builder().id(1L).roles(List.of(Role.builder().name("PM").build())).build();
        Auth auth = Auth.builder().email("person@example.test").password("encoded").user(user).build();
        when(repository.existsByEmail("person@example.test")).thenReturn(false);
        when(userHelper.createUser(any())).thenReturn(user);
        when(mapper.toAuth(any())).thenReturn(auth);
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(repository.save(auth)).thenReturn(auth);
        when(mapper.toCreateAccountResponse(auth)).thenReturn(CreateAccountResponse.builder()
                .email("person@example.test").build());
        when(authHelper.getAuthenticationByEmail("person@example.test")).thenReturn(auth);
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(authHelper.getRoleNames(auth)).thenReturn(List.of("PM"));
        when(jwtHelper.generateToken(auth, "PM")).thenReturn("token");

        assertEquals("person@example.test", service.createAccount(CreateAccountRequest.builder()
                .email("person@example.test").password("pw").build()).getEmail());
        assertTrue(service.login(AuthRequest.builder().email("person@example.test").password("pw").build())
                .isAuthenticated());
        assertEquals("token", service.selectRole(SelectRoleRequest.builder().email("person@example.test")
                .password("pw").role("PM").build()).getToken());
        when(authHelper.authenticateGoogleCode("google-code")).thenReturn(auth);
        assertTrue(service.loginWithGoogle("google-code").isAuthenticated());
        when(jwtHelper.generateToken(auth, "PM")).thenReturn("google-token");
        assertEquals("google-token", service.selectGoogleRole(SelectGoogleRoleRequest.builder()
                .code("google-code").role("PM").build()).getToken());

        when(authHelper.authenticateGoogleCode("bad-code")).thenThrow(new IllegalStateException());
        assertAppError(AppError.GOOGLE_LOGIN_FAILED,
                () -> service.loginWithGoogle("bad-code"));
        assertAppError(AppError.GOOGLE_LOGIN_FAILED, () -> service.selectGoogleRole(
                SelectGoogleRoleRequest.builder().code("bad-code").role("PM").build()));
    }

    @Test
    void refreshIntrospectAndTheirParseFailuresAreHandled() throws Exception {
        User user = User.builder().roles(List.of(Role.builder().name("PM").build())).build();
        Auth auth = Auth.builder().user(user).build();
        SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),
                new JWTClaimsSet.Builder().subject("person@example.test").claim("role", "PM").build());
        when(jwtHelper.verifyToken("old")).thenReturn(signed);
        when(authHelper.getAuthenticationByEmail("person@example.test")).thenReturn(auth);
        when(jwtHelper.generateToken(auth, "PM")).thenReturn("fresh");
        assertEquals("fresh", service.refreshToken("old").getToken());

        IntrospectResponse introspect = IntrospectResponse.builder().valid(true).build();
        when(jwtHelper.introspect("token")).thenReturn(introspect);
        assertEquals(introspect, service.introspect("token"));
        when(jwtHelper.verifyToken("parse-error")).thenThrow(new ParseException("invalid", 0));
        assertAppError(AppError.FAILED_TO_PARSE_DATA, () -> service.refreshToken("parse-error"));
        when(jwtHelper.introspect("parse-error")).thenThrow(new ParseException("invalid", 0));
        assertAppError(AppError.FAILED_TO_PARSE_DATA, () -> service.introspect("parse-error"));
    }

    @Test
    void passwordRecoveryAndChangePasswordCoverSuccessAndInvalidCode() {
        Auth auth = Auth.builder().email("person@example.test").password("old")
                .forgotPasswordCode("reset-code")
                .forgotPasswordCodeExpiresAt(LocalDateTime.now().plusMinutes(5))
                .user(User.builder().roles(List.of(Role.builder().name("PM").build())).build()).build();
        when(authHelper.getAuthenticationByEmail("person@example.test")).thenReturn(auth);
        when(authHelper.generateActivationCode()).thenReturn("reset-code");
        when(emailHelper.bodyChangePassword("reset-code")).thenReturn("<html/>");
        when(repository.save(auth)).thenReturn(auth);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");
        when(authHelper.getRoleNames(auth)).thenReturn(List.of("PM"));

        service.forgotPassword("person@example.test");
        assertNotNull(auth.getForgotPasswordCodeExpiresAt());
        assertTrue(service.resetPassword(ForgotPasswordRequest.builder().email("person@example.test")
                .forgotPasswordCode("reset-code").password("new").build()).isAuthenticated());
        assertNull(auth.getForgotPasswordCode());

        auth.setForgotPasswordCode("expired");
        auth.setForgotPasswordCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        assertAppError(AppError.INVALID_VERIFICATION_CODE, () -> service.resetPassword(
                ForgotPasswordRequest.builder().email("person@example.test")
                        .forgotPasswordCode("expired").password("new").build()));

        auth.setPassword("old");
        when(authByToken.get()).thenReturn(auth);
        when(passwordEncoder.matches("old", "old")).thenReturn(true);
        service.changePassword(ChangePasswordRequest.builder().oldPassword("old").newPassword("new").build());
        assertEquals("encoded-new", auth.getPassword());
        when(passwordEncoder.matches("wrong", "encoded-new")).thenReturn(false);
        assertAppError(AppError.INCORRECT_PASSWORD, () -> service.changePassword(
                ChangePasswordRequest.builder().oldPassword("wrong").newPassword("new").build()));
    }

    private static void assertAppError(AppError expected, org.junit.jupiter.api.function.Executable action) {
        AppException error = assertThrows(AppException.class, action);
        assertEquals(expected, error.getAppError());
    }
}
