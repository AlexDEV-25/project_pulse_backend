package com.example.project_pulse_backend.entity;

import com.example.project_pulse_backend.constant.ProjectMemberStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "project_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_user",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectMemberStatus status;
}