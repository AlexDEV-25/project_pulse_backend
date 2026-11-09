package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.dto.request.TaskAssignmentRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.mapper.TaskAssignmentMapper;
import com.example.project_pulse_backend.mapper.UserMapper;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HelperRepositoryFlowsTest {
    @Test
    void createsTaskAssignmentsAndChecksMembership() {
        ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
        TaskAssignmentRepo assignmentRepo = mock(TaskAssignmentRepo.class);
        TaskAssignmentMapper mapper = mock(TaskAssignmentMapper.class);
        TaskAssignmentHelper helper = new TaskAssignmentHelper(memberRepo, assignmentRepo, mapper);
        Project project = Project.builder().id(4L).build();
        Task task = Task.builder().id(9L).build();
        ProjectMember member = ProjectMember.builder().id(6L).build();
        TaskAssignmentRequest request = TaskAssignmentRequest.builder().employeeId(6L)
                .contributionPercentage(100).build();
        TaskAssignment assignment = TaskAssignment.builder().build();
        when(memberRepo.findByProject_IdAndUser_Id(4L, 6L)).thenReturn(Optional.of(member));
        when(mapper.toTaskAssignment(request)).thenReturn(assignment);

        assertEquals(List.of(assignment), helper.createAssignments(task, project, List.of(request)));
        assertSame(task, assignment.getTask());
        assertSame(member, assignment.getEmployee());
        assertThrows(AppException.class, () -> helper.createAssignments(task, project, List.of(request, request)));
        when(memberRepo.findByProject_IdAndUser_Id(4L, 6L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.createAssignments(task, project, List.of(request)));

        when(assignmentRepo.findByTask_Id(9L)).thenReturn(List.of(assignment));
        helper.checkBelongToTask(9L, member);
        assertThrows(AppException.class, () ->
                helper.checkBelongToTask(9L, ProjectMember.builder().id(99L).build()));
    }

    @Test
    void readsAndCreatesEarningTransactionsAndNotifiesBothSidesOfTransfers() {
        EarningTransactionRepo repo = mock(EarningTransactionRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        NotificationHelper notification = mock(NotificationHelper.class);
        EarningTransactionHelper helper = new EarningTransactionHelper(repo, projectHelper, notification);
        Project project = Project.builder().id(4L).build();
        User pm = User.builder().id(1L).build();
        User sourceUser = User.builder().id(2L).userName("Source").build();
        User recipientUser = User.builder().id(3L).userName("Recipient").build();
        Project sourceProject = Project.builder().id(4L).pm(pm).build();
        Phase phase = Phase.builder().id(5L).project(sourceProject).build();
        Task task = Task.builder().id(9L).taskName("Build").phase(phase)
                .taskPoint(new BigDecimal("10.00")).build();
        ProjectMember sourceMember = ProjectMember.builder().id(2L).user(sourceUser).build();
        ProjectMember targetMember = ProjectMember.builder().id(3L).user(recipientUser).build();
        EarningTransaction earning = EarningTransaction.builder().id(11L).build();
        when(projectHelper.getProjectById(4L, sourceUser)).thenReturn(project);
        when(repo.filterEarningTransaction(4L, 5L, EarningTransactionType.EARN, "build"))
                .thenReturn(List.of(earning));
        assertEquals(List.of(earning), helper.getByProjectId(4L, sourceUser, "build", 5L,
                EarningTransactionType.EARN));
        when(repo.findById(11L)).thenReturn(Optional.of(earning));
        assertSame(earning, helper.getById(11L, 4L, sourceUser));
        when(repo.findById(12L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getById(12L, 4L, sourceUser));

        TaskAssignment assignment = TaskAssignment.builder().contributionPercentage(40).build();
        when(repo.save(any(EarningTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EarningTransaction created = helper.createEarningTransaction(task, assignment, targetMember);
        assertEquals(new BigDecimal("4.00"), created.getPoints());
        assertEquals(targetMember, created.getExecutor());

        Notification savedNotification = Notification.builder().build();
        when(notification.createNotification(any())).thenReturn(savedNotification);
        EarningTransaction source = EarningTransaction.builder().id(11L).task(task)
                .executor(sourceMember).points(new BigDecimal("10")).build();
        EarningTransaction transfer = EarningTransaction.builder().id(12L).task(task)
                .executor(targetMember).points(new BigDecimal("2.50")).build();
        helper.getTransferFormTaskNotification(source, transfer);
        helper.transferPointFormTaskNotification(source, transfer);
        verify(notification, times(2)).createNotification(any());
        verify(notification, times(2)).createUserNotification(any());
    }

    @Test
    void looksUpProjectMembersAndChecksWhetherTheyAreInUse() {
        ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
        TaskAssignmentRepo assignmentRepo = mock(TaskAssignmentRepo.class);
        TaskNoteRepo noteRepo = mock(TaskNoteRepo.class);
        TaskCostTransactionRepo costRepo = mock(TaskCostTransactionRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        ProjectMemberHelper helper = new ProjectMemberHelper(memberRepo, assignmentRepo, noteRepo, costRepo,
                earningRepo, allocationRepo, projectHelper);
        User user = User.builder().id(7L).build();
        Project project = Project.builder().id(4L).build();
        ProjectMember member = ProjectMember.builder().id(6L).project(project).build();
        when(memberRepo.findById(6L)).thenReturn(Optional.of(member));
        when(memberRepo.findByProject_IdAndUser_Id(4L, 8L)).thenReturn(Optional.of(member));

        assertSame(member, helper.getProjectMemberById(6L, user));
        assertSame(member, helper.findByProjectAndUser(project, 8L, user));
        verify(projectHelper, times(2)).checkAccessToProject(4L, user);
        when(memberRepo.findById(6L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getProjectMemberById(6L, user));
        when(memberRepo.findByProject_IdAndUser_Id(4L, 8L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.findByProjectAndUser(project, 8L, user));

        helper.checkBeforeDeleteProjectMember(6L);
        when(assignmentRepo.findByEmployee_Id(6L)).thenReturn(List.of(TaskAssignment.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDeleteProjectMember(6L));
    }

    @Test
    void createsUsersFindsRolesAndChecksDepartment() {
        UserRepo userRepo = mock(UserRepo.class);
        RoleRepo roleRepo = mock(RoleRepo.class);
        DepartmentRepo departmentRepo = mock(DepartmentRepo.class);
        UserMapper mapper = mock(UserMapper.class);
        UserHelper helper = new UserHelper(userRepo, roleRepo, departmentRepo, mapper);
        Role role = Role.builder().name("PM").build();
        Department department = Department.builder().id(4L).build();
        CreateAccountRequest request = CreateAccountRequest.builder().roles(List.of("PM", "PM"))
                .departmentId(4L).build();
        User mapped = User.builder().userName("Pat").build();
        when(roleRepo.findAllById(List.of("PM"))).thenReturn(List.of(role));
        when(departmentRepo.findById(4L)).thenReturn(Optional.of(department));
        when(mapper.toUser(request)).thenReturn(mapped);
        when(userRepo.save(mapped)).thenReturn(mapped);

        assertEquals(List.of(role), helper.findRoles(List.of("PM", "PM")));
        assertSame(mapped, helper.createUser(request));
        assertEquals(List.of(role), mapped.getRoles());
        assertSame(department, mapped.getDepartment());
        when(roleRepo.findAllById(List.of("UNKNOWN"))).thenReturn(List.of());
        assertThrows(AppException.class, () -> helper.findRoles(List.of("UNKNOWN")));
        when(departmentRepo.findById(4L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.createUser(request));

        when(userRepo.existsByDepartment_Id(4L)).thenReturn(false);
        helper.checkDepartmentEmpty(4L);
        when(userRepo.existsByDepartment_Id(4L)).thenReturn(true);
        assertThrows(AppException.class, () -> helper.checkDepartmentEmpty(4L));
        when(userRepo.countByDepartment_Id(4L)).thenReturn(3L);
        assertEquals(3L, helper.getUserCountByDepartment(4L));
    }

    @Test
    void fetchesTaskCostsAfterProjectAuthorization() {
        TaskCostTransactionRepo repo = mock(TaskCostTransactionRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        TaskCostTransactionHelper helper = new TaskCostTransactionHelper(repo, allocationRepo, projectHelper);
        User user = User.builder().id(2L).build();
        Project project = Project.builder().id(4L).build();
        TaskCostTransaction transaction = TaskCostTransaction.builder().id(6L).build();
        when(projectHelper.getProjectById(4L, user)).thenReturn(project);
        when(repo.filterTaskCost(4L, 3L, "cost")).thenReturn(List.of(transaction));
        assertEquals(List.of(transaction), helper.getByProjectId(4L, user, "cost", 3L));

        Task task = Task.builder().phase(Phase.builder().project(project).build()).build();
        transaction.setTask(task);
        when(repo.findById(6L)).thenReturn(Optional.of(transaction));
        assertSame(transaction, helper.getById(6L, user));
        verify(projectHelper).checkAccessToProject(4L, user);
        when(repo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getById(7L, user));

        Task costTask = Task.builder().phase(Phase.builder().id(3L).build()).build();
        ProjectMember employee = ProjectMember.builder().id(8L).build();
        when(allocationRepo.findByPhase_IdAndMember_Id(3L, 8L)).thenReturn(Optional.empty());
        assertThrows(AppException.class,
                () -> helper.createTaskCostTransaction(costTask, employee, List.of()));
    }
}
