package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "authentications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "forgot_password_code")
    private String forgotPasswordCode;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "hidden")
    private boolean hidden;
}
