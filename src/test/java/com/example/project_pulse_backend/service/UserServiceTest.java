package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.dto.response.UserStatisticsResponse;
import com.example.project_pulse_backend.dto.response.RoleResponse;
import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.FileManager;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.UserHelper;
import com.example.project_pulse_backend.helper.TestAuthentication;
import com.example.project_pulse_backend.mapper.UserMapper;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private AuthRepo authRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private DepartmentRepo departmentRepo;

    @Mock
    private GetUserByToken getUserByToken;

    @Mock
    private FileManager fileStorage;

    @Mock
    private UserHelper userHelper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyInfo_shouldReturnMappedResponse() {
        Department department = new Department();
        department.setId(3L);
        department.setDepartmentName("IT");

        User user = new User();
        user.setId(12L);
        user.setUserName("Alice");
        user.setPosition("Developer");
        user.setDepartment(department);
        user.setResourceRate(new BigDecimal("2.50"));
        user.setRoles(List.of(Role.builder().name("EMPLOYEE").build()));

        when(getUserByToken.get()).thenReturn(user);
        when(userMapper.toUserResponse(user, null)).thenReturn(UserResponse.builder()
                .userName("Alice")
                .departmentName("IT")
                .roles(List.of(RoleResponse.builder().name("EMPLOYEE").build()))
                .build());

        UserResponse response = userService.getMyInfo();

        assertEquals("Alice", response.getUserName());
        assertEquals("IT", response.getDepartmentName());
        assertEquals(1, response.getRoles().size());
    }

    @Test
    void updateAvatar_shouldUploadAndSaveNewAvatarUrl() throws Exception {
        Department department = new Department();
        department.setDepartmentName("IT");

        User user = new User();
        user.setId(12L);
        user.setUserName("Alice");
        user.setPosition("Developer");
        user.setDepartment(department);
        user.setRoles(List.of(Role.builder().name("EMPLOYEE").build()));

        MultipartFile file = mock(MultipartFile.class);
        Map<Object, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cdn/test.png");
        doReturn(uploadResult).when(fileStorage).uploadImage(file);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(getUserByToken.get()).thenReturn(user);
        when(userMapper.toUserResponse(user, null)).thenReturn(UserResponse.builder()
                .avatarUrl("https://cdn/test.png")
                .build());

        UserResponse response = userService.updateAvatar(file);

        assertEquals("https://cdn/test.png", response.getAvatarUrl());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void getUsers_shouldApplyFiltersForAdmin() {
        TestAuthentication.selectRole("ADMIN");
        Department department = Department.builder().id(7L).departmentName("Engineering").build();
        Role pmRole = Role.builder().name("PM").build();
        User employee = User.builder()
                .id(12L)
                .userName("Alice")
                .position("Developer")
                .department(department)
                .roles(List.of(pmRole))
                .build();
        User admin = User.builder().roles(List.of(Role.builder().name("ADMIN").build())).build();

        when(getUserByToken.get()).thenReturn(admin);
        when(userRepo.findUsers(any(), anyBoolean(), anyList(), anyBoolean(), anyList()))
                .thenReturn(List.of(employee));
        Auth auth = Auth.builder().id(3L).email("alice@example.com").build();
        when(authRepo.findByUser_Id(12L)).thenReturn(java.util.Optional.of(auth));
        when(userMapper.toUserResponse(employee, auth)).thenReturn(UserResponse.builder()
                .email("alice@example.com")
                .roles(List.of(RoleResponse.builder().name("PM").build()))
                .departmentId(7L)
                .build());

        List<UserResponse> results = userService.getUsers(List.of("PM"), List.of(7L), "Alice");

        assertEquals(1, results.size());
        assertEquals("alice@example.com", results.get(0).getEmail());
        assertEquals("PM", results.get(0).getRoles().get(0).getName());
        assertEquals(7L, results.get(0).getDepartmentId());
    }

    @Test
    void getUsers_shouldReturnOnlyApprovedFieldsForPm() throws Exception {
        TestAuthentication.selectRole("PM");
        Department department = Department.builder().id(7L).departmentName("Engineering").build();
        User user = User.builder().id(12L).userName("Alice").position("Developer")
                .resourceRate(new BigDecimal("1.5")).avatarUrl("https://cdn/avatar.png")
                .department(department).roles(List.of(Role.builder().name("EMPLOYEE").build())).build();
        when(getUserByToken.get()).thenReturn(User.builder().id(5L).build());
        when(userRepo.findUsers(any(), anyBoolean(), anyList(), anyBoolean(), anyList()))
                .thenReturn(List.of(user));
        Auth auth = Auth.builder().id(3L).email("alice@example.com").build();
        when(authRepo.findByUser_Id(12L)).thenReturn(java.util.Optional.of(auth));
        when(userMapper.toUserResponseForPm(user, auth)).thenReturn(UserResponse.builder()
                .id(12L)
                .email("alice@example.com")
                .userName("Alice")
                .position("Developer")
                .resourceRate(new BigDecimal("1.5"))
                .avatarUrl("https://cdn/avatar.png")
                .departmentId(7L)
                .departmentName("Engineering")
                .build());

        List<UserResponse> results = userService.getUsers(null, null, null);

        var json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(results.get(0)));
        assertEquals(java.util.Set.of("id", "email", "userName", "position", "resourceRate", "avatarUrl",
                        "departmentId", "departmentName"),
                java.util.stream.StreamSupport.stream(
                                java.util.Spliterators.spliteratorUnknownSize(
                                        json.fieldNames(), java.util.Spliterator.ORDERED), false)
                        .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void getUserStatistics_shouldReturnTotalAndCountsByRole() {
        TestAuthentication.selectRole("ADMIN");
        User admin = User.builder().roles(List.of(Role.builder().name("ADMIN").build())).build();
        when(userRepo.count()).thenReturn(10L);
        when(userRepo.countUsersByRoleName("EMPLOYEE")).thenReturn(5L);
        when(userRepo.countUsersByRoleName("ADMIN")).thenReturn(2L);
        when(userRepo.countUsersByRoleName("PM")).thenReturn(3L);

        UserStatisticsResponse response = userService.getUserStatistics();

        assertEquals(10L, response.getTotalUsers());
        assertEquals(2L, response.getUsersByRole().get("ADMIN"));
        assertEquals(3L, response.getUsersByRole().get("PM"));
    }

    @Test
    void getByIdSelectsAdminProjectionAndUpdateUserPersistsMappedFields() {
        TestAuthentication.selectRole("ADMIN");
        User admin = User.builder().id(1L).roles(List.of(Role.builder().name("ADMIN").build())).build();
        User target = User.builder().id(12L).build();
        Department department = Department.builder().id(3L).departmentName("Engineering").build();
        UpdateUserRequest request = UpdateUserRequest.builder().id(12L).userName("Alice")
                .position("Developer").resourceRate(new BigDecimal("2.0"))
                .departmentId(3L).roles(List.of("EMPLOYEE")).build();
        UserResponse expected = UserResponse.builder().id(12L).userName("Alice").build();
        when(getUserByToken.get()).thenReturn(admin);
        when(userRepo.findById(12L)).thenReturn(Optional.of(target));
        when(userHelper.findRoles(List.of("EMPLOYEE"))).thenReturn(List.of(Role.builder().name("EMPLOYEE").build()));
        when(departmentRepo.findById(3L)).thenReturn(Optional.of(department));
        when(userRepo.save(target)).thenReturn(target);
        when(authRepo.findByUser_Id(12L)).thenReturn(Optional.empty());
        when(userMapper.toUserResponse(target, null)).thenReturn(expected);

        assertEquals(expected, userService.getById(12L));
        assertEquals(expected, userService.updateUser(request));
        assertEquals(department, target.getDepartment());
        assertEquals("EMPLOYEE", target.getRoles().getFirst().getName());
        verify(userMapper).updateUser(request, target);
    }

    @Test
    void emptyAvatarReturnsCurrentUserWithoutUploading() {
        User user = User.builder().id(2L).avatarUrl("old-url").build();
        when(getUserByToken.get()).thenReturn(user);
        UserResponse expected = UserResponse.builder().avatarUrl("old-url").build();
        when(userMapper.toUserResponse(user, null)).thenReturn(expected);
        MultipartFile empty = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);

        assertEquals(expected, userService.updateAvatar(empty));
        verifyNoInteractions(fileStorage);
        verify(userRepo, never()).save(any(User.class));
    }
}
