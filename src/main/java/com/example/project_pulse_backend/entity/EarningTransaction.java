package com.example.project_pulse_backend.entity;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "earning_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_transaction_id")
    private EarningTransaction referenceTransaction;

    // points =  contributionPercentage * Task.taskPoint
    @Column(nullable = false)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "earning_transaction_type", nullable = false)
    private EarningTransactionType earningTransactionType;

    @Lob
    @Column(name = "reason")
    private String reason;

}
