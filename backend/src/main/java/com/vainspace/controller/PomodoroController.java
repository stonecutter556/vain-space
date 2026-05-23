package com.vainspace.controller;

import com.vainspace.entity.PomodoroSession;
import com.vainspace.repository.PomodoroSessionRepository;
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
    public List<PomodoroSession> getAll() {
        return pomodoroRepository.findAllByOrderByStartTimeDesc();
    }

    @PostMapping
    public PomodoroSession create(@RequestBody PomodoroSession session) {
        return pomodoroRepository.save(session);
    }

    @PutMapping("/{id}")
    public PomodoroSession update(@PathVariable Long id, @RequestBody PomodoroSession session) {
        session.setId(id);
        return pomodoroRepository.save(session);
    }
}
