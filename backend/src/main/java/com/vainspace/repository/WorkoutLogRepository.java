package com.vainspace.repository;

import com.vainspace.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findAllByOrderByWorkoutDateDesc();
    List<WorkoutLog> findByUserIdOrderByWorkoutDateDesc(Long userId);
    List<WorkoutLog> findByIsPublicTrueOrderByCreatedAtDesc();
}
