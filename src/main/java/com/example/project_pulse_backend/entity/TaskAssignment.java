package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "task_assignment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_user",
                        columnNames = {"task_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // % đóng góp của nhân viên vào kết quả cuối cùng của task.
    @Column(name = "contribution_percentage", nullable = false)
    private Integer contributionPercentage;
}