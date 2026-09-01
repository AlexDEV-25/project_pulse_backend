package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AuthenticationRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserByToken {
    private final AuthenticationRepo authenticationRepository;
    private final UserRepo userRepository;

    public User get() {

        SecurityContext context = SecurityContextHolder.getContext();

        if (context == null) {
            return null;
        }

        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();

        if (email == null || email.isBlank() || email.equals("anonymousUser")) {
            return null;
        }

        return authenticationRepository.findByEmail(email).orElseThrow(
                () -> new AppException(AppError.USER_NOT_FOUND)
        ).getUser();
    }
}