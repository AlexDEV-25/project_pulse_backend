package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "phase_evm_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseEvmSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phase_id", nullable = false, unique = true)
    private Phase phase;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "bac", nullable = false, precision = 19, scale = 4)
    private BigDecimal bac;

    @Column(name = "pv", nullable = false, precision = 19, scale = 4)
    private BigDecimal pv;

    @Column(name = "ev", nullable = false, precision = 19, scale = 4)
    private BigDecimal ev;

    @Column(name = "ac", nullable = false, precision = 19, scale = 4)
    private BigDecimal ac;
}
