package com.vainspace.controller;

import com.vainspace.entity.WorkoutLog;
import com.vainspace.repository.WorkoutLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workout-logs")
public class WorkoutLogController {

    private final WorkoutLogRepository workoutLogRepository;

    public WorkoutLogController(WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    @GetMapping
    public List<WorkoutLog> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return workoutLogRepository.findByUserIdOrderByWorkoutDateDesc(userId);
    }

    @GetMapping("/public")
    public List<WorkoutLog> getPublic() {
        return workoutLogRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @PostMapping
    public WorkoutLog create(@RequestBody WorkoutLog workoutLog, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        workoutLog.setUserId(userId);
        if (workoutLog.getIsPublic() == null) workoutLog.setIsPublic(false);
        return workoutLogRepository.save(workoutLog);
    }

    @PutMapping("/{id}")
    public WorkoutLog update(@PathVariable Long id, @RequestBody WorkoutLog workoutLog, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        WorkoutLog existing = workoutLogRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        workoutLog.setId(id);
        return workoutLogRepository.save(workoutLog);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        WorkoutLog existing = workoutLogRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            workoutLogRepository.deleteById(id);
        }
    }
}
