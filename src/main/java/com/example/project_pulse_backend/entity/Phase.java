package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "phase_name", nullable = false)
    private String phaseName;

    @Column(name = "workdays", nullable = false)
    private Integer workdays;
}
