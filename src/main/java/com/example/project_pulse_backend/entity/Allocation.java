package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "member_id", nullable = false)
    private ProjectMember member;

    @ElementCollection
    @CollectionTable(
            name = "allocation_overloaded_dates",
            joinColumns = @JoinColumn(name = "allocation_id")
    )
    @Column(name = "overloaded_date", nullable = false)
    @Builder.Default
    private List<LocalDate> overloadedDates = new ArrayList<>();

    // resourceRateSnapshot =  User.resourceRate tại thời điểm phân bổ (bất biến trong suốt phase)
    @Column(name = "resource_rate_snapshot")
    private BigDecimal resourceRateSnapshot;

}
