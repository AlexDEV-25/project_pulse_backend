package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.TaskNote;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskNoteRepo extends JpaRepository<TaskNote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TaskNote> findByIdAndAuthor_User_Id(Long id, Long userId);

    List<TaskNote> findByTask_IdOrderByCreatedAtAsc(Long taskId);

    List<TaskNote> findByTask_IdAndAuthor_IdAndTaskNoteStatusOrderByWorkingDateAsc(
            Long taskId, Long authorId, TaskNoteStatus taskNoteStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskNote> findByTask_IdAndAuthor_User_IdAndWorkingDateGreaterThanEqualAndWorkingDateLessThan(
            Long taskId, Long userId, LocalDateTime startAt, LocalDateTime endAt);
}
