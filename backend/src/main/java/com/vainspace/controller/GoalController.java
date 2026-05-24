package com.vainspace.controller;

import com.vainspace.entity.Goal;
import com.vainspace.repository.GoalRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    public List<Goal> getAll(HttpServletRequest request, @RequestParam(required = false) String type) {
        Long userId = (Long) request.getAttribute("userId");
        if (type != null) {
            return goalRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        }
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/public")
    public List<Goal> getPublic() {
        return goalRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @PostMapping
    public Goal create(@RequestBody Goal goal, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        goal.setUserId(userId);
        if (goal.getIsPublic() == null) goal.setIsPublic(false);
        return goalRepository.save(goal);
    }

    @PutMapping("/{id}")
    public Goal update(@PathVariable Long id, @RequestBody Goal goal, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Goal existing = goalRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        goal.setId(id);
        return goalRepository.save(goal);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Goal existing = goalRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            goalRepository.deleteById(id);
        }
    }
}
