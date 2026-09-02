package com.example.project_pulse_backend.configuration;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.repository.PermissionRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    ApplicationRunner applicationRunner(RoleRepo roleRepository, PermissionRepo permissionRepository) {
        return args -> {
            permissionsSave(permissionRepository);
            rolesSave(roleRepository, permissionRepository);
        };
    }

    private void permissionsSave(PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissionRepository, "CREAT_ACCOUNT", "Admin thực hiện:  Tạo tài khoản");
        checkPermissionAndSave(permissionRepository, "LOCK_ACCOUNT", "Admin thực hiện:  Khóa tài khoản");
    }

    private void rolesSave(RoleRepo roleRepository, PermissionRepo permissionRepository) {
        checkRoleAndSave(roleRepository, "ADMIN", "quản trị viên",
                permissionRepository.findByDescriptionContainingIgnoreCase("Admin thực hiện"));
        checkRoleAndSave(roleRepository, "PM", "quản lý dự án",
                permissionRepository.findByDescriptionContainingIgnoreCase("PM thực hiện"));
        checkRoleAndSave(roleRepository, "EMPLOYEE", "nhân viên",
                permissionRepository.findByDescriptionContainingIgnoreCase("EMPLOYEE thực hiện"));
    }

    private void checkPermissionAndSave(PermissionRepo permissionRepository, String permission,
                                        String description) {

        if (permissionRepository.findByName(permission).isEmpty()) {
            permissionRepository.save(Permission.builder().name(permission).description(description).build());
        }
    }

    private void checkRoleAndSave(RoleRepo roleRepository, String role, String description,
                                  List<Permission> permissions) {

        if (roleRepository.findByName(role).isEmpty()) {
            roleRepository.save(Role.builder().name(role).description(description).permissions(permissions).build());
        }
    }
}
