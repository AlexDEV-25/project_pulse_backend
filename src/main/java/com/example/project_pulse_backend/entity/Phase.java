package com.example.project_pulse_backend.entity;

import com.example.project_pulse_backend.constant.PhaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Column(name = "phase_name", nullable = false, unique = true)
    private String phaseName;

    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(
            name = "phase_make_up_date",
            joinColumns = @JoinColumn(name = "phase_id")
    )
    @Column(name = "date")
    private List<LocalDate> makeUpDates;

    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(
            name = "phase_off_date",
            joinColumns = @JoinColumn(name = "phase_id")
    )
    @Column(name = "date")
    private List<LocalDate> offDates;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PhaseStatus status;
}
