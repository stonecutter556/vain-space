package com.vainspace.controller;

import com.vainspace.entity.Goal;
import com.vainspace.repository.GoalRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalRepository goalRepository;

    public GoalController(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @GetMapping
    public List<Goal> getAll() {
        return goalRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Goal create(@RequestBody Goal goal) {
        return goalRepository.save(goal);
    }

    @PutMapping("/{id}")
    public Goal update(@PathVariable Long id, @RequestBody Goal goal) {
        goal.setId(id);
        return goalRepository.save(goal);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        goalRepository.deleteById(id);
    }
}
