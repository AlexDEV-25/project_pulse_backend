package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // số giờ làm thực tế của nhân viên cho task trong ngày.
    @Column(name = "working_hours", nullable = false)
    private Integer workingHours;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}