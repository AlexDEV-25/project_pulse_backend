package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "task_cost_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCostTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = false)
    private User executor;

    @Column(name = "points", nullable = false)
    private BigDecimal points;

    @Column(name = "workdays", nullable = false)
    private Integer workdays;

    @Lob
    @Column(name = "reason")
    private String reason;

}
