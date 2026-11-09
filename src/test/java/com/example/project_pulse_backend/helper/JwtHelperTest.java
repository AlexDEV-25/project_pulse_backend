package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.nimbusds.jwt.SignedJWT;
import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtHelperTest {
    private static final String SIGNER_KEY =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @Test
    void generateToken_shouldGrantOnlySelectedRoleAuthorities() throws Exception {
        Role admin = Role.builder().name("ADMIN")
                .permissions(List.of(Permission.builder().name("CREATE_ACCOUNT").build())).build();
        Role pm = Role.builder().name("PM")
                .permissions(List.of(Permission.builder().name("CREATE_TASK").build())).build();
        Role employee = Role.builder().name("EMPLOYEE")
                .permissions(List.of(Permission.builder().name("CREATE_TASK_NOTE").build())).build();
        Auth auth = Auth.builder().email("user@example.com")
                .user(User.builder().roles(List.of(admin, pm, employee)).build()).build();

        JwtHelper jwtHelper = new JwtHelper();
        ReflectionTestUtils.setField(jwtHelper, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");
        ReflectionTestUtils.setField(jwtHelper, "EXPIRATION_TIME", 3600L);

        SignedJWT token = SignedJWT.parse(jwtHelper.generateToken(auth, "PM"));

        assertEquals("PM", token.getJWTClaimsSet().getStringClaim("role"));
        String scope = token.getJWTClaimsSet().getStringClaim("scope");
        assertTrue(scope.contains("ROLE_PM"));
        assertTrue(scope.contains("CREATE_TASK"));
        assertFalse(scope.contains("ROLE_ADMIN"));
        assertFalse(scope.contains("CREATE_ACCOUNT"));
        assertFalse(scope.contains("ROLE_EMPLOYEE"));
        assertFalse(scope.contains("CREATE_TASK_NOTE"));
    }

    @Test
    void generateToken_withoutSelectedRole_shouldHaveNoAuthorities() throws Exception {
        Role admin = Role.builder().name("ADMIN")
                .permissions(List.of(Permission.builder().name("CREATE_ACCOUNT").build())).build();
        Auth auth = Auth.builder().email("user@example.com")
                .user(User.builder().roles(List.of(admin)).build()).build();
        JwtHelper jwtHelper = new JwtHelper();
        ReflectionTestUtils.setField(jwtHelper, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");
        ReflectionTestUtils.setField(jwtHelper, "EXPIRATION_TIME", 3600L);

        SignedJWT token = SignedJWT.parse(jwtHelper.generateToken(auth, null));

        assertEquals("", token.getJWTClaimsSet().getStringClaim("scope"));
        assertFalse(token.getJWTClaimsSet().getClaims().containsKey("role"));
    }

    @Test
    void verifiesAndIntrospectsSignedTokensAndRejectsInvalidOrExpiredTokens() throws Exception {
        JwtHelper helper = new JwtHelper();
        ReflectionTestUtils.setField(helper, "SIGNER_KEY", SIGNER_KEY);
        ReflectionTestUtils.setField(helper, "EXPIRATION_TIME", 3600L);
        Auth auth = Auth.builder().email("user@example.com")
                .user(User.builder().roles(List.of(Role.builder().name("PM").build())).build()).build();
        String validToken = helper.generateToken(auth, "PM");

        assertTrue(helper.verifyToken(validToken).verify(new com.nimbusds.jose.crypto.MACVerifier(
                SIGNER_KEY.getBytes())));
        IntrospectResponse valid = helper.introspect(validToken);
        assertTrue(valid.isValid());

        String[] segments = validToken.split("\\.");
        segments[2] = (segments[2].startsWith("a") ? "b" : "a") + segments[2].substring(1);
        String invalidSignature = String.join(".", segments);
        assertFalse(helper.introspect(invalidSignature).isValid());
        assertThrows(AppException.class, () -> helper.verifyToken(invalidSignature));

        SignedJWT expired = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512),
                new JWTClaimsSet.Builder().subject("user@example.com")
                        .expirationTime(Date.from(Instant.now().minusSeconds(10))).build());
        expired.sign(new MACSigner(SIGNER_KEY.getBytes()));
        assertThrows(AppException.class, () -> helper.verifyToken(expired.serialize()));
    }
}
