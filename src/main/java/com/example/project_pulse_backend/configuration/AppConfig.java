package com.example.project_pulse_backend.configuration;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {
    @Value("${app.bootstrap.demo-users.enabled:false}")
    private boolean seedDemoUsers;

    @Value("${app.bootstrap.demo-users.password}")
    private String demoUserPassword;

    @Bean
    ApplicationRunner applicationRunner(RoleRepo roleRepository, PermissionRepo permissionRepository,
                                        DepartmentRepo departmentRepository, UserRepo userRepository, AuthRepo authRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            List<Permission> permissions = permissionsSave(permissionRepository);
            rolesSave(roleRepository, permissions);
            departmentSave(departmentRepository);
            if (seedDemoUsers) {
                userSave(userRepository, authRepository, departmentRepository, roleRepository,
                        passwordEncoder, demoUserPassword);
            }
        };
    }

    private List<Permission> permissionsSave(PermissionRepo permissionRepository) {
        List<Permission> permissions = new ArrayList<>();
        addUserPermissions(permissions, permissionRepository);
        addDepartmentPermissions(permissions, permissionRepository);
        addProjectPermissions(permissions, permissionRepository);
        addPhasePermissions(permissions, permissionRepository);
        addEvmPermissions(permissions, permissionRepository);
        addProjectMemberPermissions(permissions, permissionRepository);
        addTaskPermissions(permissions, permissionRepository);
        addTaskNotePermissions(permissions, permissionRepository);
        addTaskCostPermissions(permissions, permissionRepository);
        addEarningPermissions(permissions, permissionRepository);
        addAllocationPermissions(permissions, permissionRepository);
        addStatisticsPermissions(permissions, permissionRepository);
        return permissions;
    }

    private void addUserPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_ACCOUNT",
                "Admin thực hiện: Tạo tài khoản");
        checkPermissionAndSave(permissions, permissionRepository, "CHANGE_PASSWORD",
                "Người dùng thực hiện: Đổi mật khẩu");
        checkPermissionAndSave(permissions, permissionRepository, "GET_USERS",
                "Admin,PM thực hiện: Lấy toàn bộ nhân viên");
        checkPermissionAndSave(permissions, permissionRepository, "GET_USER_BY_ID",
                "Admin,PM thực hiện: Lấy nhân viên theo id");
        checkPermissionAndSave(permissions, permissionRepository, "GET_MY_INFO",
                "Người dùng thực hiện: Lấy thông tin của mình");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_AVATAR",
                "Người dùng thực hiện: Đổi ảnh đại diện");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_USER",
                "Admin thực hiện: Cập nhật thông tin nhân viên");
        checkPermissionAndSave(permissions, permissionRepository, "GET_STATISTICS_USERS",
                "Admin thực hiện: Lấy thổng kê nhân viên");

    }

    private void addDepartmentPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_DEPARTMENT",
                "Admin thực hiện: Tạo phòng ban");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_DEPARTMENT",
                "Admin thực hiện: Cập nhật phòng ban");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_DEPARTMENT",
                "Admin thực hiện: Xóa phòng ban");
        checkPermissionAndSave(permissions, permissionRepository, "GET_STATISTICS_DEPARTMENTS",
                "Admin thực hiện: Lấy thống kê phòng ban");
    }

    private void addProjectPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_PROJECT",
                "Admin thực hiện: Tạo dự án");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_PROJECT",
                "Admin thực hiện: Cập nhật dự án");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_PROJECT",
                "Admin thực hiện: Xóa dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECTS",
                """
                        Admin thực hiện: lấy toàn bộ dự án trong hệ thống,
                        PM thực hiện lấy toàn bộ dự án mình quản lý,
                        Employee thực hiện lấy toàn bộ dự án mình tham gia
                        """
        );
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECT_BY_ID",
                """
                        Admin thực hiện: lấy chi tiết 1 dự án trong hệ thống,
                        PM thực hiện lấy chi tiết 1 dự án mình quản lý,
                        Employee thực hiện lấy chi tiết 1 dự án mình tham gia
                        """
        );
    }

    private void addPhasePermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_PHASE",
                "PM thực hiện: Tạo phase cho dự án");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_PHASE",
                "PM thực hiện: Cập nhật phase dự án");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_PHASE",
                "PM thực hiện: Xóa phase dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PHASES_BY_PROJECT",
                "Admin,PM thực hiện: Lấy toàn bộ phase của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PHASE_BY_ID",
                "Admin,PM thực hiện: Lấy phase theo id");
    }

    private void addEvmPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "GET_EVM_BY_PHASE",
                "Admin,PM thực hiện: Xem chỉ số EVM theo phase");
        checkPermissionAndSave(permissions, permissionRepository, "GET_EVM_BY_PROJECT",
                "Admin,PM thực hiện: Xem chỉ số EVM theo dự án");
    }

    private void addProjectMemberPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_PROJECT_MEMBER",
                "PM thực hiện: Thêm thành viên dự án");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_PROJECT_MEMBER",
                "PM thực hiện: Xóa thành viên dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECT_MEMBERS_BY_PROJECT",
                "Admin,PM thực hiện: Lấy thành viên dự án");
    }

    private void addTaskPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_TASK",
                "PM thực hiện: Tạo task cho dự án");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_TASK",
                "PM thực hiện: Cập nhật task của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_TASK",
                "PM thực hiện: Xóa task của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_TASKS_BY_PROJECT",
                "Admin,PM,Employee thực hiện: Lấy danh sách task của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_TASK_BY_ID",
                "Admin,PM,Employee thực hiện: Xem chi tiết task");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_MY_TASK_STATUS_BY_ID",
                "Employee thực hiện: Xác nhận đã nhận task");

    }

    private void addTaskNotePermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_TASK_NOTE",
                "Employee thực hiện: Tạo task note");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_TASK_NOTE",
                "Employee thực hiện: Sửa task note của mình");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_TASK_NOTE",
                "Employee thực hiện: Xóa task note của mình");
        checkPermissionAndSave(permissions, permissionRepository, "GET_TASK_NOTES_BY_TASK",
                "Admin,PM,Employee thực hiện: Xem task note của task");
        checkPermissionAndSave(permissions, permissionRepository, "CONFIRM_TASK_NOTES",
                "PM thực hiện: Xác nhận task note");
    }

    private void addTaskCostPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "GET_TASK_COSTS_BY_PROJECT",
                "Admin,PM,Employee thực hiện: Xem chi phí task của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_TASK_COSTS_BY_ID",
                "Admin,PM,Employee thực hiện: Xem chi tiết chi phí task");
    }

    private void addEarningPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "GET_EARNINGS_BY_PROJECT",
                "Admin,PM,Employee thực hiện: Xem giao dịch earn của dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_EARNINGS_BY_ID",
                "Admin,PM,Employee thực hiện: Xem chi tiết giao dịch earn");
        checkPermissionAndSave(permissions, permissionRepository, "TRANSFER_EARN",
                "PM thực hiện: Chuyển earn giữa các nhân viên trong dự án");
    }

    private void addAllocationPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "CREATE_ALLOCATION",
                "PM thực hiện: Tạo phân bổ nhân sự");
        checkPermissionAndSave(permissions, permissionRepository, "UPDATE_ALLOCATION",
                "PM thực hiện: Cập nhật phân bổ nhân sự");
        checkPermissionAndSave(permissions, permissionRepository, "DELETE_ALLOCATION",
                "PM thực hiện: Xóa phân bổ nhân sự");
        checkPermissionAndSave(permissions, permissionRepository, "GET_ALLOCATIONS_BY_PROJECT",
                "Admin,PM,Employee xem danh sách phân bổ nhân lực trong dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_ALLOCATIONS_BY_ID",
                "Admin,PM,Employee xem chi tiết phân bổ nhân lực trong dự án");
    }

    private void addStatisticsPermissions(List<Permission> permissions, PermissionRepo permissionRepository) {
        checkPermissionAndSave(permissions, permissionRepository, "GET_STATISTICS_SUMMARY",
                "Admin thực hiện; PM thực hiện: Xem thống kê tổng quan");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECT_STATUS_STATISTICS",
                "Admin thực hiện; PM thực hiện: Xem thống kê trạng thái dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECT_OVERVIEW_STATISTICS",
                "Admin thực hiện; PM thực hiện: Xem tổng quan dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_PROJECT_EV_AC_STATISTICS",
                "Admin thực hiện; PM thực hiện: Xem thống kê EV và AC theo dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_IN_PROGRESS_PROJECT_STATISTICS",
                "Admin thực hiện; PM thực hiện: Xem thống kê dự án đang tiến hành");
        checkPermissionAndSave(permissions, permissionRepository, "GET_EMPLOYEE_STATISTICS_SUMMARY",
                "Employee thực hiện: Xem thống kê cá nhân");
        checkPermissionAndSave(permissions, permissionRepository, "GET_EMPLOYEE_PROJECT_POINT_STATISTICS",
                "Employee thực hiện: Xem point theo dự án");
        checkPermissionAndSave(permissions, permissionRepository, "GET_EMPLOYEE_TASK_STATISTICS",
                "Employee thực hiện: Xem thống kê task cá nhân");
    }

    private void rolesSave(RoleRepo roleRepository, List<Permission> permissions) {
        checkRoleAndSave(roleRepository, "ADMIN", "quản trị viên",
                findPermissionsByNames(permissions,
                        "CREATE_ACCOUNT", "CHANGE_PASSWORD", "GET_USERS", "GET_USER_BY_ID",
                        "GET_MY_INFO", "UPDATE_AVATAR", "GET_STATISTICS_USERS", "UPDATE_USER", "CREATE_DEPARTMENT",
                        "UPDATE_DEPARTMENT", "DELETE_DEPARTMENT", "GET_STATISTICS_DEPARTMENTS", "CREATE_PROJECT",
                        "UPDATE_PROJECT", "DELETE_PROJECT", "GET_PROJECTS", "GET_PROJECT_BY_ID",
                        "GET_PHASES_BY_PROJECT", "GET_PHASE_BY_ID", "GET_EVM_BY_PHASE",
                        "GET_EVM_BY_PROJECT", "GET_PROJECT_MEMBERS_BY_PROJECT",
                        "GET_TASKS_BY_PROJECT", "GET_TASK_BY_ID",
                        "GET_TASK_NOTES_BY_TASK", "GET_TASK_COSTS_BY_PROJECT", "GET_TASK_COSTS_BY_ID",
                        "GET_EARNINGS_BY_PROJECT", "GET_EARNINGS_BY_ID", "GET_ALLOCATIONS_BY_PROJECT",
                        "GET_ALLOCATIONS_BY_ID",
                        "GET_STATISTICS_SUMMARY", "GET_PROJECT_STATUS_STATISTICS",
                        "GET_PROJECT_OVERVIEW_STATISTICS", "GET_PROJECT_EV_AC_STATISTICS",
                        "GET_IN_PROGRESS_PROJECT_STATISTICS"));
        checkRoleAndSave(roleRepository, "PM", "quản lý dự án",
                findPermissionsByNames(permissions,
                        "CHANGE_PASSWORD", "GET_USERS", "GET_USER_BY_ID", "GET_MY_INFO", "UPDATE_AVATAR",
                        "GET_PROJECTS", "GET_PROJECT_BY_ID",
                        "CREATE_PHASE", "UPDATE_PHASE", "DELETE_PHASE", "GET_PHASES_BY_PROJECT",
                        "GET_PHASE_BY_ID", "GET_EVM_BY_PHASE", "GET_EVM_BY_PROJECT",
                        "CREATE_PROJECT_MEMBER", "DELETE_PROJECT_MEMBER", "GET_PROJECT_MEMBERS_BY_PROJECT",
                        "CREATE_TASK", "UPDATE_TASK", "DELETE_TASK", "GET_TASKS_BY_PROJECT", "GET_TASK_BY_ID",
                        "GET_TASK_NOTES_BY_TASK", "CONFIRM_TASK_NOTES",
                        "GET_TASK_COSTS_BY_PROJECT", "GET_TASK_COSTS_BY_ID", "GET_EARNINGS_BY_PROJECT",
                        "GET_EARNINGS_BY_ID", "TRANSFER_EARN", "GET_ALLOCATIONS_BY_PROJECT", "GET_ALLOCATIONS_BY_ID",
                        "CREATE_ALLOCATION", "UPDATE_ALLOCATION", "DELETE_ALLOCATION",
                        "GET_STATISTICS_SUMMARY", "GET_PROJECT_STATUS_STATISTICS",
                        "GET_PROJECT_OVERVIEW_STATISTICS", "GET_PROJECT_EV_AC_STATISTICS",
                        "GET_IN_PROGRESS_PROJECT_STATISTICS"));
        checkRoleAndSave(roleRepository, "EMPLOYEE", "nhân viên",
                findPermissionsByNames(permissions,
                        "CHANGE_PASSWORD", "GET_MY_INFO", "UPDATE_AVATAR",
                        "GET_TASKS_BY_PROJECT", "GET_ALLOCATIONS_BY_PROJECT", "GET_ALLOCATIONS_BY_ID",
                        "UPDATE_MY_TASK_STATUS_BY_ID", "CREATE_TASK_NOTE",
                        "UPDATE_TASK_NOTE", "DELETE_TASK_NOTE", "GET_TASK_NOTES_BY_TASK",
                        "GET_TASK_COSTS_BY_PROJECT", "GET_TASK_COSTS_BY_ID", "GET_EARNINGS_BY_PROJECT",
                        "GET_EARNINGS_BY_ID", "GET_EMPLOYEE_STATISTICS_SUMMARY",
                        "GET_EMPLOYEE_PROJECT_POINT_STATISTICS", "GET_EMPLOYEE_TASK_STATISTICS",
                        "GET_PROJECTS", "GET_PROJECT_BY_ID", "GET_TASK_BY_ID"));
    }

    private List<Permission> findPermissionsByNames(List<Permission> permissions, String... names) {
        List<String> requestedNames = List.of(names);
        List<Permission> matchingPermissions = permissions.stream()
                .filter(permission -> requestedNames.contains(permission.getName())).toList();
        if (matchingPermissions.size() != requestedNames.size()) {
            throw new AppException(AppError.PERMISSIONS_NOT_MATCH);
        }
        return matchingPermissions;
    }

    private void departmentSave(DepartmentRepo departmentRepository) {
        checkDepartmentAndSave(departmentRepository, "ADMIN");
        checkDepartmentAndSave(departmentRepository, "DEV");
        checkDepartmentAndSave(departmentRepository, "BA");
        checkDepartmentAndSave(departmentRepository, "QC");
    }

    private void userSave(UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository,
                          RoleRepo roleRepository, PasswordEncoder passwordEncoder, String demoPassword) {
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "admin@gmail.com", demoPassword, List.of("ADMIN", "PM", "EMPLOYEE"), "Admin", "Admin",
                BigDecimal.valueOf(2), "admin");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "pm1@gmail.com", demoPassword, List.of("PM", "EMPLOYEE"), "PM", "PM1", BigDecimal.valueOf(1.5), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee1@gmail.com", demoPassword, List.of("EMPLOYEE"), "Employee1", "junior",
                BigDecimal.valueOf(1), "DEV");
        checkUserAndSave(userRepository, authRepository, departmentRepository, roleRepository, passwordEncoder,
                "employee2@gmail.com", demoPassword, List.of("EMPLOYEE"), "Employee2", "intern",
                BigDecimal.valueOf(0.5), "DEV");
    }

    private void checkUserAndSave(
            UserRepo userRepository, AuthRepo authRepository, DepartmentRepo departmentRepository,
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
                .user(newUser).build();

        authRepository.save(authentication);

    }

    private void checkPermissionAndSave(
            List<Permission> permissions,
            PermissionRepo permissionRepository,
            String permission,
            String description) {
        Permission entity = permissionRepository.findByName(permission)
                .orElseGet(() -> Permission.builder().name(permission).build());
        if (!description.equals(entity.getDescription())) {
            entity.setDescription(description);
            entity = permissionRepository.save(entity);
        }
        permissions.add(entity);
    }

    private void checkRoleAndSave(
            RoleRepo roleRepository,
            String role, String description,
            List<Permission> permissions) {

        if (roleRepository.findByName(role).isEmpty()) {
            roleRepository.save(Role.builder().name(role).description(description).permissions(permissions).build());
        } else {
            Role entity = roleRepository.findByName(role)
                    .orElseThrow(() -> AppException.builder().appError(AppError.ROLE_NOT_FOUND).build());

            Set<String> desiredPermissionNames = permissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
            Set<String> existingPermissionNames = entity.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
            boolean same = desiredPermissionNames.equals(existingPermissionNames);
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
