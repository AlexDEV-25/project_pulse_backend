package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.entity.TaskNote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskNoteRepo extends JpaRepository<TaskNote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskNote> findByTask_IdOrderByCreatedAtAsc(Long taskId);

    List<TaskNote> findByTask_IdAndAuthor_IdAndTaskNoteStatusOrderByWorkingDateAsc(
            Long taskId, Long authorId, TaskNoteStatus taskNoteStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT taskNote FROM TaskNote taskNote WHERE taskNote.id = :id")
    Optional<TaskNote> findByIdForUpdate(@Param("id") Long id);
}
