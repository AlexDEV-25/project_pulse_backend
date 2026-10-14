package com.example.project_pulse_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AllocationOverloadedDate {
    @Column(name = "overloaded_date", nullable = false)
    private LocalDate date;

    @Column(name = "total_allocation_percentage", nullable = false)
    private Integer totalAllocationPercentage;
}
