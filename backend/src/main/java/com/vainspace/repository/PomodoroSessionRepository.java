package com.vainspace.repository;

import com.vainspace.entity.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findAllByOrderByStartTimeDesc();
    List<PomodoroSession> findByUserIdOrderByStartTimeDesc(Long userId);
    List<PomodoroSession> findByIsPublicTrueOrderByCreatedAtDesc();
    List<PomodoroSession> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(Long userId, LocalDateTime start, LocalDateTime end);
    List<PomodoroSession> findByUserIdAndGoalIdAndStartTimeBetweenOrderByStartTimeAsc(Long userId, Long goalId, LocalDateTime start, LocalDateTime end);
}
