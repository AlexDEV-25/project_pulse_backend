package com.example.project_pulse_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_name"), inverseJoinColumns = @JoinColumn(name = "permission_name"))
    private List<Permission> permissions;
}
