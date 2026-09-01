package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    // % thời gian/effort thực tế mà nhân viên dành cho task.
    @Column(name = "effort_percentage", nullable = false)
    private Integer effortPercentage;

    /**
     * % đóng góp của nhân viên vào kết quả cuối cùng của task.
     * Tổng contributionPercentage của các thành viên trong task
     * nên bằng 100% khi task hoàn thành.
     */
    @Column(name = "contribution_percentage", nullable = false)
    private Integer contributionPercentage;

    /**
     * Số ngày thực tế nhân viên đã làm task.
     */
    @Column(name = "worked_days", precision = 10, scale = 2)
    private BigDecimal workedDays;
}