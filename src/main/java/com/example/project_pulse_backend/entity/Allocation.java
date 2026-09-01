package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "allocation_percentage", nullable = false)
    private Integer allocationPercentage;

    @ManyToOne
    @JoinColumn(name = "phase_id", nullable = false)
    private Phase phase;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "resource_rate_snapshot")
    private BigDecimal resourceRateSnapshot;

}
