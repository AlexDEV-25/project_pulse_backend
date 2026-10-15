package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "allocations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_phase_member",
                        columnNames = {"phase_id", "member_id"}
                )
        }
)
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
    @BatchSize(size = 100)
    @CollectionTable(
            name = "allocation_overloaded_dates",
            joinColumns = @JoinColumn(name = "allocation_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_allocation_overloaded_date",
                    columnNames = {"allocation_id", "overloaded_date"}
            )
    )
    @Builder.Default
    private List<AllocationOverloadedDate> overloadedDates = new ArrayList<>();

    // resourceRateSnapshot =  User.resourceRate tại thời điểm phân bổ (bất biến trong suốt phase)
    @Column(name = "resource_rate_snapshot")
    private BigDecimal resourceRateSnapshot;

}
