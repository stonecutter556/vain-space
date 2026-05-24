package com.vainspace.repository;

import com.vainspace.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByOrderByCreatedAtDesc();
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Goal> findByIsPublicTrueOrderByCreatedAtDesc();
    List<Goal> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}
