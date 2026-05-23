package com.vainspace.controller;

import com.vainspace.entity.WorkoutLog;
import com.vainspace.repository.WorkoutLogRepository;
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
    public List<WorkoutLog> getAll() {
        return workoutLogRepository.findAllByOrderByWorkoutDateDesc();
    }

    @PostMapping
    public WorkoutLog create(@RequestBody WorkoutLog workoutLog) {
        return workoutLogRepository.save(workoutLog);
    }

    @PutMapping("/{id}")
    public WorkoutLog update(@PathVariable Long id, @RequestBody WorkoutLog workoutLog) {
        workoutLog.setId(id);
        return workoutLogRepository.save(workoutLog);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workoutLogRepository.deleteById(id);
    }
}
