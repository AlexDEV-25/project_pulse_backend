package com.example.project_pulse_backend.configuration;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.repository.RoleRepo;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void jwtAuthenticationConverterShouldLimitLegacyTokenAuthoritiesToSelectedRole() {
        RoleRepo roleRepo = mock(RoleRepo.class);
        Role pmRole = Role.builder().name("PM")
                .permissions(List.of(Permission.builder().name("CREATE_TASK").build()))
                .build();
        when(roleRepo.findByName("PM")).thenReturn(Optional.of(pmRole));
        Jwt jwt = Jwt.withTokenValue("legacy-broad-token")
                .header("alg", "HS256")
                .claim("sub", "user@example.com")
                .claim("roles", List.of("ADMIN", "PM", "EMPLOYEE"))
                .claim("role", "PM")
                .claim("scope", "ROLE_ADMIN CREATE_ACCOUNT ROLE_PM CREATE_TASK ROLE_EMPLOYEE CREATE_TASK_NOTE")
                .build();

        var authentication = new SecurityConfig(mock(CustomJwtDecoder.class))
                .jwtAuthenticationConverter(roleRepo)
                .convert(jwt);

        assertEquals(List.of(
                new SimpleGrantedAuthority("ROLE_PM"),
                new SimpleGrantedAuthority("CREATE_TASK")), List.copyOf(authentication.getAuthorities()));
    }

    @Test
    void jwtAuthenticationConverterShouldGrantNoRoleBeforeSelection() {
        RoleRepo roleRepo = mock(RoleRepo.class);
        Jwt jwt = Jwt.withTokenValue("unselected-token")
                .header("alg", "HS256")
                .claim("sub", "user@example.com")
                .claim("roles", List.of("ADMIN", "PM"))
                .claim("scope", "ROLE_ADMIN CREATE_ACCOUNT ROLE_PM CREATE_TASK")
                .build();

        var authentication = new SecurityConfig(mock(CustomJwtDecoder.class))
                .jwtAuthenticationConverter(roleRepo)
                .convert(jwt);

        assertEquals(List.of(), List.copyOf(authentication.getAuthorities()));
    }
}
