package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.dto.request.CreateEarningTransferRequest;
import com.example.project_pulse_backend.dto.response.EarningTransactionResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.EarningTransactionMapper;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EarningTransactionServiceTest {
    @Test
    void getByProjectNormalizesKeywordAndGetByIdMapsEntity() {
        EarningTransactionRepo repository = mock(EarningTransactionRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        EarningTransactionHelper helper = mock(EarningTransactionHelper.class);
        EarningTransactionMapper mapper = mock(EarningTransactionMapper.class);
        User user = User.builder().id(1L).build();
        EarningTransaction transaction = EarningTransaction.builder().id(5L).build();
        EarningTransactionResponse expected = EarningTransactionResponse.builder().id(5L).build();
        when(token.get()).thenReturn(user);
        when(helper.getByProjectId(2L, user, "deploy", 3L, EarningTransactionType.EARN))
                .thenReturn(List.of(transaction));
        when(helper.getById(5L, 2L, user)).thenReturn(transaction);
        when(mapper.toEarningTransactionResponse(transaction)).thenReturn(expected);
        EarningTransactionService service = new EarningTransactionService(repository, token,
                mock(ProjectMemberHelper.class), helper, mock(ProjectHelper.class), mapper);

        assertEquals(List.of(expected), service.getByProject(2L, " deploy ", 3L, EarningTransactionType.EARN));
        assertEquals(expected, service.getById(5L, 2L));
        verify(helper).getByProjectId(2L, user, "deploy", 3L, EarningTransactionType.EARN);
    }

    @Test
    void transferCreatesAndNotifiesMappedTransfer() {
        EarningTransactionRepo repository = mock(EarningTransactionRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectMemberHelper memberHelper = mock(ProjectMemberHelper.class);
        EarningTransactionHelper transactionHelper = mock(EarningTransactionHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        EarningTransactionMapper mapper = mock(EarningTransactionMapper.class);
        User user = User.builder().id(1L).build();
        User sender = User.builder().id(2L).build();
        User targetUser = User.builder().id(3L).build();
        Project project = Project.builder().id(10L).build();
        ProjectMember senderMember = ProjectMember.builder().user(sender).build();
        ProjectMember targetMember = ProjectMember.builder().user(targetUser).build();
        Phase phase = Phase.builder().project(project).build();
        Task task = Task.builder().phase(phase).build();
        EarningTransaction source = EarningTransaction.builder().id(20L).task(task)
                .executor(senderMember).earningTransactionType(EarningTransactionType.EARN).build();
        EarningTransaction transfer = EarningTransaction.builder().points(new BigDecimal("2")).build();
        EarningTransactionResponse response = EarningTransactionResponse.builder().build();
        CreateEarningTransferRequest request = CreateEarningTransferRequest.builder()
                .sourceTransactionId(20L).targetEmployeeId(3L).points(new BigDecimal("2")).build();
        when(token.get()).thenReturn(user);
        when(projectHelper.getProjectById(10L, user)).thenReturn(project);
        when(repository.findByIdForUpdate(20L)).thenReturn(Optional.of(source));
        when(memberHelper.findByProjectAndUser(project, 3L, user)).thenReturn(targetMember);
        when(mapper.toTransfer(request)).thenReturn(transfer);
        when(repository.save(transfer)).thenReturn(transfer);
        when(mapper.toEarningTransactionResponse(transfer)).thenReturn(response);
        EarningTransactionService service = new EarningTransactionService(repository, token,
                memberHelper, transactionHelper, projectHelper, mapper);

        assertEquals(response, service.transfer(10L, request));
        assertSame(task, transfer.getTask());
        assertSame(targetMember, transfer.getExecutor());
        assertSame(source, transfer.getReferenceTransaction());
        assertEquals(EarningTransactionType.TRANSFER, transfer.getEarningTransactionType());
        verify(transactionHelper).checkAvailablePoint(source, new BigDecimal("2"));
        verify(transactionHelper).getTransferFormTaskNotification(source, transfer);
        verify(transactionHelper).transferPointFormTaskNotification(source, transfer);
    }
}
