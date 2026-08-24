package com.example.project_pulse_backend.entity;

import com.example.project_pulse_backend.constant.AllocationStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "resource_rate_snapshot")
    private float resourceRateSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false)
    private AllocationStatus allocationStatus;

}
