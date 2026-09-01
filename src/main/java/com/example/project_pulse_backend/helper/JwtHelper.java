package com.example.project_pulse_backend.helper;


import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.entity.Authentication;
import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
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
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

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

        Date epx = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!epx.after(new Date())) {
            throw AppException.builder().appError(AppError.TOKEN_EXPIRED).build();
        }
        if (!verified) {
            throw AppException.builder().appError(AppError.INVALID_TOKEN).build();
        }

        return signedJWT;
    }

    public String generateToken(Authentication authentication) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()//
                .subject(authentication.getEmail())//
                .issuer("moimoi.com")//
                .claim("scope", buildScope(authentication.getUser()))//
                .jwtID(UUID.randomUUID().toString())//
                .issueTime(new Date())//
                .expirationTime(Date.from(Instant.now().plus(EXPIRATION_TIME, ChronoUnit.SECONDS))).build();

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

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            stringJoiner.add("ROLE_" + role.getName());
            List<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                stringJoiner.add(permission.getName());
            }
        }
        return stringJoiner.toString();
    }
}