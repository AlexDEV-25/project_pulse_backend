package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleHelperTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkIsRole_shouldOnlyMatchSelectedRole() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("sub", "test@example.com")
                .claim("roles", List.of("ADMIN", "PM", "EMPLOYEE"))
                .claim("role", "PM")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_PM"),
                new SimpleGrantedAuthority("CREATE_PROJECT")
        )));

        User user = new User();
        assertTrue(RoleHelper.checkIsPm(user));
        assertFalse(RoleHelper.checkIsAdmin(user));
        assertFalse(RoleHelper.checkIsEmployee(user));
    }

    @Test
    void checkIsRole_shouldRejectTokensWithoutSelectedRoleClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("sub", "test@example.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        )));

        assertFalse(RoleHelper.checkIsAdmin(new User()));
    }

    @Test
    void userHasRoleChecksTheUsersAssignedRoles() {
        assertNotNull(new RoleHelper());
        User user = User.builder().roles(List.of(
                com.example.project_pulse_backend.entity.Role.builder().name("PM").build())).build();

        assertTrue(RoleHelper.userHasRole(user, "PM"));
        assertFalse(RoleHelper.userHasRole(user, "ADMIN"));
    }
}
