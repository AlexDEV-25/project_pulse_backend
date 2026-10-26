package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByReceiver_IdAndReadFalse(Long receiverId);

    List<UserNotification> findByReceiver_IdAndReadFalseOrderByCreatedAtDescIdDesc(Long receiverId);

    Optional<UserNotification> findByIdAndReceiver_Id(Long id, Long receiverId);

    List<UserNotification> findByReceiver_IdOrderByCreatedAtDescIdDesc(Long receiverId);
}
