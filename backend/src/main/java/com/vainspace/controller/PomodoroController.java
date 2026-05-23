package com.vainspace.controller;

import com.vainspace.entity.PomodoroSession;
import com.vainspace.repository.PomodoroSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pomodoro")
public class PomodoroController {

    private final PomodoroSessionRepository pomodoroRepository;

    public PomodoroController(PomodoroSessionRepository pomodoroRepository) {
        this.pomodoroRepository = pomodoroRepository;
    }

    @GetMapping
    public List<PomodoroSession> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pomodoroRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    @GetMapping("/public")
    public List<PomodoroSession> getPublic() {
        return pomodoroRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @PostMapping
    public PomodoroSession create(@RequestBody PomodoroSession session, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        session.setUserId(userId);
        if (session.getIsPublic() == null) session.setIsPublic(false);
        return pomodoroRepository.save(session);
    }

    @PutMapping("/{id}")
    public PomodoroSession update(@PathVariable Long id, @RequestBody PomodoroSession session, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PomodoroSession existing = pomodoroRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        session.setId(id);
        return pomodoroRepository.save(session);
    }
}
