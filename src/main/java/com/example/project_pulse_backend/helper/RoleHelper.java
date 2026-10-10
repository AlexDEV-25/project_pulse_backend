package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Objects;

public class RoleHelper {

    public static boolean checkIsAdmin(User user) {
        return hasCurrentRole(user, "ADMIN");
    }

    public static boolean checkIsPm(User user) {
        return hasCurrentRole(user, "PM");
    }

    public static boolean checkIsEmployee(User user) {
        return hasCurrentRole(user, "EMPLOYEE");
    }

    private static boolean hasCurrentRole(User user, String roleName) {
        if (user == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {
            return false;
        }

        if (!roleName.equals(jwtAuthentication.getToken().getClaimAsString("role"))) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + roleName)::equals);
    }

    public static boolean userHasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> Objects.equals(role.getName(), roleName));
    }
}
