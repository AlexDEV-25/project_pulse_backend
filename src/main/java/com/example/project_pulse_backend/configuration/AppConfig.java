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
                                        DepartmentRepo departmentRepository, UserRepo userRepository, AuthRepo authRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            permissionsSave(permissionRepository);
            rolesSave(roleRepository, permissionRepository);
            departmentSave(departmentRepository);
            userSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder);
        };
    }

    private void permissionsSave(PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissionRepository, "CREATE_ACCOUNT", "Admin thực hiện:  Tạo tài khoản");
        checkPermissionAndSave(permissionRepository, "TOGGLE_ACCOUNT_STATUS",
                "Admin thực hiện:  Khóa / mở khóa tài khoản");
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
        checkPermissionAndSave(permissionRepository, "CREATE_PHASE", "PM thực hiện:  tạo phase cho dự án");
        checkPermissionAndSave(permissionRepository, "UPDATE_PHASE", "PM thực hiện:  sửa phase dự án");
        checkPermissionAndSave(permissionRepository, "DELETE_PHASE", "PM thực hiện:  xóa phase dự án");
        checkPermissionAndSave(permissionRepository, "GET_ALL_PHASES",
                "ADMIN thực hiện:  lấy toàn bộ phase của 1 dự án");
        checkPermissionAndSave(permissionRepository, "GET_PHASE_BY_ID",
                "ADMIN thực hiện:  lấy chi tiết 1 phase của 1 dự án");
        checkPermissionAndSave(permissionRepository, "GET_ALL_PHASES_FOR_PM",
                "PM thực hiện:  lấy toàn bộ phase của 1 dự án cho pm");
        checkPermissionAndSave(permissionRepository, "GET_PHASE_BY_ID_FOR_PM",
                "PM thực hiện:  lấy chi tiết 1 phase của 1 dự án cho pm");
        checkPermissionAndSave(permissionRepository, "GET_EVM_ADMIN", "Admin thực hiện: xem chỉ số EVM");
        checkPermissionAndSave(permissionRepository, "GET_EVM_PM", "PM thực hiện: xem chỉ số EVM");
        checkPermissionAndSave(permissionRepository, "UPDATE_PHASE_STATUS",
                "Admin thực hiện: thay đổi trạng thái phase");
        checkPermissionAndSave(permissionRepository, "UPDATE_PHASE_STATUS_FOR_PM",
                "PM thực hiện: thay đổi trạng thái phase của dự án mình quản lý");
        checkPermissionAndSave(permissionRepository, "CREATE_PROJECT_MEMBER", "PM thực hiện: thêm thành viên dự án");
        checkPermissionAndSave(permissionRepository, "UPDATE_PROJECT_MEMBER", "PM thực hiện: sửa thành viên dự án");
        checkPermissionAndSave(permissionRepository, "DELETE_PROJECT_MEMBER", "PM thực hiện: xóa thành viên dự án");
        checkPermissionAndSave(permissionRepository, "GET_MY_PROJECTS",
                "Employee thực hiện: xem project đã được duyệt tham gia");
        checkPermissionAndSave(permissionRepository, "UPDATE_PROJECT_MEMBER_STATUS",
                "ADMIN thực hiện: thay đổi trạng thái thành viên dự án");
        checkPermissionAndSave(permissionRepository, "CREATE_TASK", "PM thực hiện: tạo task cho dự án");
        checkPermissionAndSave(permissionRepository, "UPDATE_TASK", "PM thực hiện: cập nhật task của dự án");
        checkPermissionAndSave(permissionRepository, "DELETE_TASK", "PM thực hiện: xóa task của dự án");
        checkPermissionAndSave(permissionRepository, "GET_TASKS_BY_PROJECT",
                "PM thực hiện: lấy danh sách task của dự án");
        checkPermissionAndSave(permissionRepository, "GET_TASKS_BY_EMPLOYEE",
                "PM thực hiện: lấy danh sách task của nhân viên");
        checkPermissionAndSave(permissionRepository, "GET_TASK_BY_ID_FOR_PM", "PM thực hiện: xem chi tiết task");
        checkPermissionAndSave(permissionRepository, "GET_MY_ASSIGNED_TASKS",
                "Employee thực hiện: xem danh sách task được giao");
        checkPermissionAndSave(permissionRepository, "GET_MY_ASSIGNED_TASK_BY_ID",
                "Employee thực hiện: xem chi tiết task được giao");
        checkPermissionAndSave(permissionRepository, "GET_MY_ALLOCATIONS",
                "Employee thực hiện: xem phân bổ công việc của mình");
        checkPermissionAndSave(permissionRepository, "UPDATE_TASK_ASSIGNMENT",
                "PM thực hiện: cập nhật tỷ lệ đóng góp của nhân viên trong task");
        checkPermissionAndSave(permissionRepository, "CREATE_TASK_NOTE", "Employee thực hiện: tạo task note");
        checkPermissionAndSave(permissionRepository, "UPDATE_TASK_NOTE", "Employee thực hiện: sửa task note của mình");
        checkPermissionAndSave(permissionRepository, "DELETE_TASK_NOTE", "Employee thực hiện: xóa task note của mình");
        checkPermissionAndSave(permissionRepository, "GET_TASK_NOTES_FOR_PM", "PM thực hiện: xem toàn bộ task note");
        checkPermissionAndSave(permissionRepository, "CONFIRM_TASK_NOTES", "PM thực hiện: xác nhận task note");
        checkPermissionAndSave(permissionRepository, "GET_TASK_COSTS_BY_TASK_ADMIN",
                "Admin thực hiện: xem chi phí task");
        checkPermissionAndSave(permissionRepository, "GET_TASK_COSTS_BY_TASK_FOR_PM", "PM thực hiện: xem chi phí task");
        checkPermissionAndSave(permissionRepository, "GET_TASK_COSTS_BY_EMPLOYEE_ADMIN",
                "Admin thực hiện: xem chi phí task của nhân viên");
        checkPermissionAndSave(permissionRepository, "GET_TASK_COSTS_BY_EMPLOYEE_FOR_PM",
                "PM thực hiện: xem chi phí task của nhân viên");
        checkPermissionAndSave(permissionRepository, "GET_EARNINGS_BY_PROJECT_ADMIN",
                "Admin thực hiện: xem giao dịch earn của dự án");
        checkPermissionAndSave(permissionRepository, "GET_EARNINGS_BY_PROJECT_FOR_PM",
                "PM thực hiện: xem giao dịch earn của dự án");
        checkPermissionAndSave(permissionRepository, "GET_EARNINGS_BY_PROJECT_FOR_EMPLOYEE",
                "Employee thực hiện: xem giao dịch earn của dự án mình tham gia");
        checkPermissionAndSave(permissionRepository, "TRANSFER_EARN_FOR_PROJECT_ADMIN",
                "Admin thực hiện: chuyển earn giữa các nhân viên trong dự án");
        checkPermissionAndSave(permissionRepository, "TRANSFER_EARN_FOR_PROJECT_PM",
                "PM thực hiện: chuyển earn giữa các nhân viên trong dự án");
        checkPermissionAndSave(permissionRepository, "REVERSE_EARN_FOR_PROJECT_ADMIN",
                "Admin thực hiện: reverse earn trong dự án");
        checkPermissionAndSave(permissionRepository, "REVERSE_EARN_FOR_PROJECT_PM",
                "PM thực hiện: reverse earn trong dự án");
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

    private void userSave(UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository,
                          RoleRepo roleRepository, PasswordEncoder passwordEncoder) {
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "admin@gmail.com", "Password@12345", List.of("ADMIN", "PM", "EMPLOYEE"), "Admin", "Admin",
                BigDecimal.valueOf(2), "admin");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "pm1@gmail.com", "Password@12345", List.of("PM", "EMPLOYEE"), "PM", "PM1", BigDecimal.valueOf(1.5), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee1@gmail.com", "Password@12345", List.of("EMPLOYEE"), "Employee1", "junior",
                BigDecimal.valueOf(1), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee2@gmail.com", "Password@12345", List.of("EMPLOYEE"), "Employee2", "intern",
                BigDecimal.valueOf(0.5), "DEV");
    }

    private void checkUserAndSave(UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository,
                                  RoleRepo roleRepository, PasswordEncoder passwordEncoder,
                                  String email, String password, List<String> roles,
                                  String userName, String position, BigDecimal resourceRate, String departmentName) {

        if (authRepository.existsByEmail(email)) {
            return;
        }

        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder().email(email).password(password)
                .roles(roles).userName(userName).position(position).resourceRate(resourceRate)
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

    private User createUser(CreateAccountRequest request, UserRepo userRepository, DepartmentRepo departmentRepository,
                            RoleRepo roleRepository) {

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
