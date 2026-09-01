package com.example.project_pulse_backend.configuration;


import com.example.project_pulse_backend.dto.response.IntrospectResponse;
import com.example.project_pulse_backend.helper.JwtHelper;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.secretKey}")
    private String signerKey;
    private final JwtHelper jwtHelper;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            IntrospectResponse response = jwtHelper.introspect(token);
            if (!response.isValid())
                throw new JwtException("Token invalid");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
        }

        return nimbusJwtDecoder.decode(token);
    }

}
