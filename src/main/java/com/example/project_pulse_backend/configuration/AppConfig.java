package com.example.project_pulse_backend.configuration;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    ApplicationRunner applicationRunner(RoleRepo roleRepository, PermissionRepo permissionRepository,
                                        DepartmentRepo departmentRepository, UserRepo userRepository, AuthRepo authRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            permissionsSave(permissionRepository);
            rolesSave(roleRepository, permissionRepository);
            departmentSave(departmentRepository);
            userSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder);
        };
    }

    private void permissionsSave(PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissionRepository, "CREATE_ACCOUNT", "Admin thực hiện:  Tạo tài khoản");
        checkPermissionAndSave(permissionRepository, "TOGGLE_ACCOUNT_STATUS", "Admin thực hiện:  Khóa / mở khóa tài khoản");
        checkPermissionAndSave(permissionRepository, "CHANGE_PASSWORD", "Employee thực hiện:  đổi mật khẩu");
        checkPermissionAndSave(permissionRepository, "CREATE_DEPARTMENT", "Admin thực hiện:  Tạo phòng ban");
        checkPermissionAndSave(permissionRepository, "UPDATE_DEPARTMENT", "Admin thực hiện: update phòng ban");
        checkPermissionAndSave(permissionRepository, "DELETE_DEPARTMENT", "Admin thực hiện:  xóa phòng ban");
        checkPermissionAndSave(permissionRepository, "GET_ALL_USERS", "Admin thực hiện:  lấy toàn bộ nhân viên");
        checkPermissionAndSave(permissionRepository, "GET_ALL_USERS_FOR_PM", "PM thực hiện:  lấy toàn bộ nhân viên");
        checkPermissionAndSave(permissionRepository, "GET_MY_INFO", "Employee thực hiện:  lấy thông tin của mình");
        checkPermissionAndSave(permissionRepository, "UPDATE_AVATAR", "Employee thực hiện:  đổi ảnh đại diện");
        checkPermissionAndSave(permissionRepository, "UPDATE_USER", "Admin thực hiện:  update thông tin nhân viên");
        checkPermissionAndSave(permissionRepository, "CREATE_PROJECT", "Admin thực hiện:  Tạo project");
        checkPermissionAndSave(permissionRepository, "UPDATE_PROJECT", "Admin thực hiện:  update project");
        checkPermissionAndSave(permissionRepository, "DELETE_PROJECT", "Admin thực hiện:  delete project");
        checkPermissionAndSave(permissionRepository, "GET_ALL_PROJECTS", "Admin thực hiện:  lấy toàn bộ project");
        checkPermissionAndSave(permissionRepository, "GET_ALL_PROJECTS_FOR_PM", "PM thực hiện:  lấy toàn bộ project");
        checkPermissionAndSave(permissionRepository, "GET_PROJECT_BY_ID", "Admin thực hiện:  lấy project theo id");
        checkPermissionAndSave(permissionRepository, "GET_PROJECT_BY_ID_FOR_PM", "PM thực hiện:  lấy project theo id");
    }

    private void rolesSave(RoleRepo roleRepository, PermissionRepo permissionRepository) {
        checkRoleAndSave(roleRepository, "ADMIN", "quản trị viên",
                permissionRepository.findByDescriptionContainingIgnoreCase("Admin thực hiện"));
        checkRoleAndSave(roleRepository, "PM", "quản lý dự án",
                permissionRepository.findByDescriptionContainingIgnoreCase("PM thực hiện"));
        checkRoleAndSave(roleRepository, "EMPLOYEE", "nhân viên",
                permissionRepository.findByDescriptionContainingIgnoreCase("Employee thực hiện"));
    }

    private void departmentSave(DepartmentRepo departmentRepository) {
        checkDepartmentAndSave(departmentRepository, "Admin");
        checkDepartmentAndSave(departmentRepository, "Test");
        checkDepartmentAndSave(departmentRepository, "DEV");
        checkDepartmentAndSave(departmentRepository, "BA");
        checkDepartmentAndSave(departmentRepository, "QC");
    }

    private void userSave(UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository, RoleRepo roleRepository, PasswordEncoder passwordEncoder) {
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "admin@gmail.com", "Admin@12345", List.of("ADMIN", "PM", "EMPLOYEE"), "Admin", "Admin", BigDecimal.valueOf(2), "admin");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "pm1@gmail.com", "PM@12345", List.of("PM", "EMPLOYEE"), "PM", "PM1", BigDecimal.valueOf(1.5), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee1@gmail.com", "Employee@12345", List.of("EMPLOYEE"), "Employee1", "junior", BigDecimal.valueOf(1), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee2@gmail.com", "Employee@12345", List.of("EMPLOYEE"), "Employee2", "intern", BigDecimal.valueOf(0.5), "DEV");
    }

    private void checkUserAndSave(UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository,
                                  RoleRepo roleRepository, PasswordEncoder passwordEncoder,
                                  String email, String password, List<String> roles,
                                  String userName, String position, BigDecimal resourceRate, String departmentName) {

        if (authRepository.existsByEmail(email)) {
            throw AppException.builder().appError(AppError.EMAIL_ALREADY_EXISTS).build();
        }

        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder().
                email(email).password(password).roles(roles).userName(userName).position(position).resourceRate(resourceRate)
                .departmentId(getDepartmentIdByName(departmentRepository, departmentName)).build();

        User newUser = createUser(createAccountRequest, userRepository, departmentRepository, roleRepository);

        Auth authentication = Auth.builder()
                .email(createAccountRequest.getEmail())
                .password(passwordEncoder.encode(createAccountRequest.getPassword()))
                .user(newUser).enabled(true).build();

        authRepository.save(authentication);

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
        } else {
            Role entity = roleRepository.findByName(role)
                    .orElseThrow(() -> AppException.builder().appError(AppError.ROLE_NOT_FOUND).build());

            boolean same = new HashSet<>(permissions).equals(new HashSet<>(entity.getPermissions()));
            if (!same) {
                entity.setPermissions(permissions);
                roleRepository.save(entity);
            }
        }
    }

    private void checkDepartmentAndSave(DepartmentRepo departmentRepository, String departmentName) {

        if (departmentRepository.findByDepartmentName(departmentName).isEmpty()) {
            departmentRepository.save(Department.builder().departmentName(departmentName).hidden(false).build());
        }
    }

    private User createUser(CreateAccountRequest request, UserRepo userRepository, DepartmentRepo departmentRepository, RoleRepo roleRepository) {

        List<Role> roles = roleRepository.findAllById(request.getRoles());
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());

        User newUser = User.builder()
                .userName(request.getUserName())
                .roles(roles)
                .position(request.getPosition())
                .resourceRate(request.getResourceRate())
                .department(department)
                .build();
        return userRepository.save(newUser);
    }

    private Long getDepartmentIdByName(DepartmentRepo departmentRepository, String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build())
                .getId();
    }
}
