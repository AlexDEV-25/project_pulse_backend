package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "authentications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "forgot_password_code")
    private String forgotPasswordCode;

    @Column(name = "forgot_password_code_expires_at")
    private LocalDateTime forgotPasswordCodeExpiresAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
