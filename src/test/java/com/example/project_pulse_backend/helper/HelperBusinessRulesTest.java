package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.ScheduleStatus;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.mapper.*;
import com.example.project_pulse_backend.repository.*;
import com.example.project_pulse_backend.repository.httpClient.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelperBusinessRulesTest {

    @Test
    void statisticsHelperClassifiesScheduleAndTaskDeadlines() {
        assertEquals(ScheduleStatus.NO_DATA, StatisticsHelper.scheduleStatus(null));
        assertEquals(ScheduleStatus.FAST, StatisticsHelper.scheduleStatus(new BigDecimal("1.1")));
        assertEquals(ScheduleStatus.ON_SCHEDULE, StatisticsHelper.scheduleStatus(BigDecimal.ONE));

        Task task = Task.builder()
                .taskStatus(TaskStatus.TODO)
                .endAt(LocalDate.of(2026, 11, 12))
                .build();
        assertTrue(StatisticsHelper.isInProgressOrNearingDeadline(
                task, LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 16)));
    }

    @Test
    void notificationHelperBuildsLinksAndFormatsPoints() {
        NotificationHelper helper = new NotificationHelper(
                mock(NotificationRepository.class),
                mock(UserNotificationRepository.class),
                mock(NotificationMapper.class));

        assertEquals("/projects/4?tab=tasks&taskId=8", helper.linkToTask(4L, 8L));
        assertEquals("/projects?tab=earnings&type=TRANSFER", helper.linkToTransfer(null, null));
        assertEquals("Bạn vừa nhận được 2.5 point từ task Review ",
                helper.getPointFormTaskNotification(2.5, "Review"));
    }

    @Test
    void projectHelperRecognizesFinishedAndActiveProjects() {
        ProjectHelper helper = new ProjectHelper(
                mock(ProjectRepo.class), mock(ProjectMemberRepo.class), mock(PhaseRepo.class));

        assertTrue(helper.isNotFinished(Project.builder().projectStatus(
                com.example.project_pulse_backend.constant.ProjectStatus.IN_PROGRESS).build()));
        assertFalse(helper.isNotFinished(Project.builder().projectStatus(
                com.example.project_pulse_backend.constant.ProjectStatus.DONE).build()));
    }

    @Test
    void taskNoteHelperRequiresPendingStatus() {
        TaskNoteHelper helper = new TaskNoteHelper(
                mock(TaskRepo.class), mock(TaskNoteRepo.class), mock(ProjectHelper.class),
                mock(NotificationHelper.class));

        assertDoesNotThrow(() -> helper.checkPendingConfirmation(TaskNote.builder()
                .taskNoteStatus(TaskNoteStatus.PENDING_CONFIRMATION).build()));
        assertThrows(AppException.class, () -> helper.checkPendingConfirmation(TaskNote.builder()
                .taskNoteStatus(TaskNoteStatus.CONFIRMED).build()));
    }

    @Test
    void allocationHelperCalculatesPointsFromWorkingDays() {
        AllocationHelper helper = new AllocationHelper(
                mock(AllocationRepo.class), mock(ProjectHelper.class), mock(PhaseHelper.class));
        Phase phase = Phase.builder()
                .startAt(LocalDate.of(2026, 11, 9))
                .endAt(LocalDate.of(2026, 11, 10))
                .build();
        Allocation allocation = Allocation.builder()
                .phase(phase)
                .resourceRateSnapshot(new BigDecimal("3.00"))
                .allocationPercentage(50)
                .build();

        assertEquals(new BigDecimal("3.00"), helper.calculateAllocationPoint(allocation));
        assertEquals(BigDecimal.ZERO, helper.calculateAllocationPoint(null));
    }

    @Test
    void authHelperGeneratesCodesAndExtractsRoles() {
        AuthHelper helper = new AuthHelper(
                mock(AuthRepo.class), mock(OutboundIdentityClient.class), mock(OutboundUserClient.class));
        Auth auth = Auth.builder().user(User.builder().roles(List.of(
                Role.builder().name("PM").build(), Role.builder().name("EMPLOYEE").build())).build()).build();

        assertDoesNotThrow(() -> java.util.UUID.fromString(helper.generateActivationCode()));
        assertEquals(List.of("PM", "EMPLOYEE"), helper.getRoleNames(auth));
    }

    @Test
    void earningHelperTotalsTransfersAndChecksAvailablePoints() {
        EarningTransactionRepo repository = mock(EarningTransactionRepo.class);
        EarningTransactionHelper helper = new EarningTransactionHelper(
                repository, mock(ProjectHelper.class), mock(NotificationHelper.class));
        EarningTransaction source = EarningTransaction.builder()
                .id(12L).points(new BigDecimal("10.00")).build();
        when(repository.findByReferenceTransaction_IdAndEarningTransactionType(
                12L, EarningTransactionType.TRANSFER)).thenReturn(List.of(
                EarningTransaction.builder().points(new BigDecimal("2.00")).build()));

        assertEquals(new BigDecimal("2.00"), helper.totalTransferPoint(source));
        assertDoesNotThrow(() -> helper.checkAvailablePoint(source, new BigDecimal("8.00")));
        assertThrows(AppException.class,
                () -> helper.checkAvailablePoint(source, new BigDecimal("8.01")));
    }

    @Test
    void taskAssignmentHelperVerifiesTaskMembership() {
        TaskAssignmentRepo repository = mock(TaskAssignmentRepo.class);
        TaskAssignmentHelper helper = new TaskAssignmentHelper(
                mock(ProjectMemberRepo.class), repository, mock(TaskAssignmentMapper.class));
        ProjectMember member = ProjectMember.builder().id(5L).build();
        when(repository.findByTask_Id(9L)).thenReturn(List.of(
                TaskAssignment.builder().employee(member).build()));

        assertDoesNotThrow(() -> helper.checkBelongToTask(9L, member));
    }

    @Test
    void taskCostHelperCreatesCostFromConfirmedHours() {
        TaskCostTransactionRepo repository = mock(TaskCostTransactionRepo.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        TaskCostTransactionHelper helper = new TaskCostTransactionHelper(
                repository, allocationRepo, mock(ProjectHelper.class));
        Phase phase = Phase.builder().id(3L).build();
        Task task = Task.builder().phase(phase).taskName("Build").build();
        ProjectMember member = ProjectMember.builder().id(6L).build();
        when(allocationRepo.findByPhase_IdAndMember_Id(3L, 6L)).thenReturn(Optional.of(
                Allocation.builder().resourceRateSnapshot(new BigDecimal("4.00")).build()));

        helper.createTaskCostTransaction(task, member, List.of(
                TaskNote.builder().workingHours(4).build(),
                TaskNote.builder().workingHours(4).build()));

        var transactionCaptor = org.mockito.ArgumentCaptor.forClass(TaskCostTransaction.class);
        verify(repository).save(transactionCaptor.capture());
        assertEquals(0, new BigDecimal("1.00").compareTo(transactionCaptor.getValue().getWorkdays()));
        assertEquals(0, new BigDecimal("4.0000").compareTo(transactionCaptor.getValue().getPoints()));
    }

    @Test
    void taskHelperAllowsOnlyEmployeeStartTransition() {
        TaskHelper helper = new TaskHelper(
                mock(TaskRepo.class), mock(TaskNoteRepo.class), mock(TaskCostTransactionRepo.class),
                mock(EarningTransactionRepo.class), mock(ProjectHelper.class), mock(PhaseHelper.class),
                mock(NotificationHelper.class));

        assertDoesNotThrow(() -> helper.checkEmployeeStatusTransition(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
        assertThrows(AppException.class,
                () -> helper.checkEmployeeStatusTransition(TaskStatus.IN_PROGRESS, TaskStatus.DONE));
    }

    @Test
    void projectMemberHelperAllowsDeletingUnusedMembers() {
        TaskAssignmentRepo taskAssignmentRepo = mock(TaskAssignmentRepo.class);
        TaskNoteRepo taskNoteRepo = mock(TaskNoteRepo.class);
        TaskCostTransactionRepo taskCostRepo = mock(TaskCostTransactionRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        ProjectMemberHelper helper = new ProjectMemberHelper(
                mock(ProjectMemberRepo.class), taskAssignmentRepo, taskNoteRepo, taskCostRepo,
                earningRepo, allocationRepo, mock(ProjectHelper.class));
        when(taskAssignmentRepo.findByEmployee_Id(2L)).thenReturn(List.of());
        when(taskNoteRepo.findByAuthor_Id(2L)).thenReturn(List.of());
        when(taskCostRepo.findByExecutor_Id(2L)).thenReturn(List.of());
        when(earningRepo.findByExecutor_Id(2L)).thenReturn(List.of());
        when(allocationRepo.existsByMember_Id(2L)).thenReturn(false);

        assertDoesNotThrow(() -> helper.checkBeforeDeleteProjectMember(2L));
    }

    @Test
    void userHelperChecksDepartmentReferences() {
        UserRepo userRepo = mock(UserRepo.class);
        UserHelper helper = new UserHelper(
                userRepo, mock(RoleRepo.class), mock(DepartmentRepo.class), mock(UserMapper.class));
        when(userRepo.existsByDepartment_Id(4L)).thenReturn(false);
        when(userRepo.countByDepartment_Id(4L)).thenReturn(3L);

        assertDoesNotThrow(() -> helper.checkDepartmentEmpty(4L));
        assertEquals(3L, helper.getUserCountByDepartment(4L));
    }

    @Test
    void transactionHelperRejectsTasksWithoutAssignments() {
        TransactionHelper helper = new TransactionHelper(
                mock(TaskNoteHelper.class), mock(EarningTransactionHelper.class),
                mock(TaskCostTransactionHelper.class), mock(NotificationHelper.class));

        assertThrows(AppException.class, () -> helper.createCompletionTransactions(
                Task.builder().assignments(List.of()).build()));
    }

    @Test
    void transactionHelperCreatesEarningsCostsAndNotificationsForAssignments() {
        TaskNoteHelper notes = mock(TaskNoteHelper.class);
        EarningTransactionHelper earnings = mock(EarningTransactionHelper.class);
        TaskCostTransactionHelper costs = mock(TaskCostTransactionHelper.class);
        NotificationHelper notifications = mock(NotificationHelper.class);
        TransactionHelper helper = new TransactionHelper(notes, earnings, costs, notifications);
        User employee = User.builder().id(5L).build();
        User pm = User.builder().id(6L).build();
        ProjectMember member = ProjectMember.builder().user(employee).build();
        Project project = Project.builder().id(4L).pm(pm).build();
        Task task = Task.builder().id(9L).taskName("Build")
                .phase(Phase.builder().project(project).build())
                .assignments(List.of(TaskAssignment.builder().employee(member).build())).build();
        TaskNote confirmedNote = TaskNote.builder().build();
        EarningTransaction transaction = EarningTransaction.builder().id(10L).points(new BigDecimal("2"))
                .task(task).executor(member).build();
        when(notes.checkTaskNotesExist(task, member)).thenReturn(List.of(confirmedNote));
        when(earnings.createEarningTransaction(task, task.getAssignments().getFirst(), member))
                .thenReturn(transaction);
        when(notifications.createNotification(any())).thenReturn(Notification.builder().build());

        helper.createCompletionTransactions(task);

        verify(notes).checkTaskNotesExist(task, member);
        verify(costs).createTaskCostTransaction(task, member, List.of(confirmedNote));
        verify(notifications).createNotification(any());
        verify(notifications).createUserNotification(any());
    }

    @Test
    void emailHelperEmbedsResetCodeInFrontendLink() {
        EmailHelper helper = new EmailHelper(mock(EmailClient.class));
        ReflectionTestUtils.setField(helper, "frontendDomain", "https://pulse.example");

        assertTrue(helper.bodyChangePassword("reset-123")
                .contains("https://pulse.example/change-password/reset-123"));
    }

    @Test
    void evmHelperDividesSafelyWhenDenominatorsAreZero() {
        EvmHelper helper = new EvmHelper(
                mock(PhaseEvmSnapshotRepo.class), mock(AllocationRepo.class),
                mock(EarningTransactionRepo.class), mock(TaskCostTransactionRepo.class),
                mock(PhaseRepo.class), mock(ProjectMemberRepo.class), mock(TaskRepo.class),
                mock(AllocationHelper.class), mock(PhaseHelper.class));

        assertEquals(new BigDecimal("2.0000"), helper.cpi(new BigDecimal("10"), new BigDecimal("5")));
        assertNull(helper.spi(new BigDecimal("5"), BigDecimal.ZERO));
    }
}
