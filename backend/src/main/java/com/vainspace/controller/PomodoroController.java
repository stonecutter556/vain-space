package com.vainspace.controller;

import com.vainspace.entity.PomodoroSession;
import com.vainspace.repository.PomodoroSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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

    @GetMapping("/stats")
    public Map<String, Object> getStats(HttpServletRequest request, @RequestParam String date) {
        Long userId = (Long) request.getAttribute("userId");
        java.time.LocalDate day = java.time.LocalDate.parse(date);
        java.time.LocalDateTime start = day.atStartOfDay();
        java.time.LocalDateTime end = day.plusDays(1).atStartOfDay();

        List<PomodoroSession> sessions = pomodoroRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, start, end);

        Map<Long, Integer> goalMinutes = new HashMap<>();
        int totalMinutes = 0;
        for (PomodoroSession s : sessions) {
            int m = s.getFocusMinutes() != null ? s.getFocusMinutes() : 0;
            totalMinutes += m;
            Long gid = s.getGoalId();
            if (gid != null) {
                goalMinutes.merge(gid, m, Integer::sum);
            }
        }

        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            for (int mm = 0; mm < 60; mm += 30) {
                java.time.LocalDateTime slotStart = day.atTime(h, mm);
                java.time.LocalDateTime slotEnd = slotStart.plusMinutes(30);
                int slotMinutes = 0;
                for (PomodoroSession s : sessions) {
                    if (!s.getStartTime().isBefore(slotStart) && s.getStartTime().isBefore(slotEnd)) {
                        slotMinutes += s.getFocusMinutes() != null ? s.getFocusMinutes() : 0;
                    }
                }
                Map<String, Object> slot = new HashMap<>();
                slot.put("time", String.format("%02d:%02d", h, mm));
                slot.put("minutes", slotMinutes);
                timeline.add(slot);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalMinutes", totalMinutes);
        result.put("goalMinutes", goalMinutes);
        result.put("timeline", timeline);
        result.put("count", sessions.size());
        return result;
    }
}
