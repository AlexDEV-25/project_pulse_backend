package com.example.project_pulse_backend.helper;


import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.exception.AppException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtHelper {

    @Value("${jwt.expirationTime}")
    protected long EXPIRATION_TIME;
    @Value("${jwt.secretKey}")
    private String SIGNER_KEY;

    public IntrospectResponse introspect(String token) throws JOSEException, ParseException {
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }
        IntrospectResponse response = new IntrospectResponse();
        response.setValid(isValid);
        return response;
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException, AppException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (expirationTime == null) {
            throw AppException.builder().appError(AppError.INVALID_TOKEN).build();
        }
        if (!expirationTime.after(new Date())) {
            throw AppException.builder().appError(AppError.TOKEN_EXPIRED).build();
        }
        if (!verified) {
            throw AppException.builder().appError(AppError.INVALID_TOKEN).build();
        }

        return signedJWT;
    }

    public String generateToken(Auth authentication, String roleName) {
        if (authentication.getUser() == null || authentication.getUser().getRoles() == null
                || authentication.getUser().getRoles().isEmpty()) {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }

        List<Role> roles = authentication.getUser().getRoles();
        if (roleName != null && roles.stream().noneMatch(role -> Objects.equals(role.getName(), roleName))) {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }
        List<String> roleNames = roles.stream().map(Role::getName).distinct().toList();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()//
                .subject(authentication.getEmail())//
                .issuer("moimoi.com")//
                .claim("roles", roleNames)//
                .claim("scope", buildScope(roles, roleName))//
                .jwtID(UUID.randomUUID().toString())//
                .issueTime(new Date())//
                .expirationTime(Date.from(Instant.now().plus(EXPIRATION_TIME, ChronoUnit.SECONDS)));
        if (roleName != null) {
            claims.claim("role", roleName);
        }
        JWTClaimsSet jwtClaimsSet = claims.build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw AppException.builder().appError(AppError.INVALID_SECRET_KEY_LENGTH).build();
        } catch (JOSEException e) {
            throw AppException.builder().appError(AppError.JOSE_PROCESSING_ERROR).build();
        }
    }

    private String buildScope(List<Role> roles, String roleName) {
        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        if (roleName == null) {
            return "";
        }

        Role selectedRole = roles.stream()
                .filter(role -> Objects.equals(role.getName(), roleName))
                .findFirst()
                .orElseThrow(() -> AppException.builder().appError(AppError.ACCESS_DENIED).build());
        authorities.add("ROLE_" + selectedRole.getName());
        List<Permission> permissions = selectedRole.getPermissions();
        if (permissions != null) {
            for (Permission permission : permissions) {
                authorities.add(permission.getName());
            }
        }
        return String.join(" ", authorities);
    }
}