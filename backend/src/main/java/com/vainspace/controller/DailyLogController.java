package com.vainspace.controller;

import com.vainspace.entity.DailyLog;
import com.vainspace.repository.DailyLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-logs")
public class DailyLogController {

    private final DailyLogRepository dailyLogRepository;

    public DailyLogController(DailyLogRepository dailyLogRepository) {
        this.dailyLogRepository = dailyLogRepository;
    }

    @GetMapping
    public List<DailyLog> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return dailyLogRepository.findByUserIdOrderByLogDateDesc(userId);
    }

    @GetMapping("/public")
    public List<DailyLog> getPublic() {
        return dailyLogRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/today")
    public DailyLog getToday(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return dailyLogRepository.findByUserIdAndLogDate(userId, LocalDate.now())
                .orElse(new DailyLog());
    }

    @PostMapping
    public DailyLog create(@RequestBody DailyLog dailyLog, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dailyLog.setUserId(userId);
        if (dailyLog.getIsPublic() == null) dailyLog.setIsPublic(false);
        return dailyLogRepository.save(dailyLog);
    }

    @PutMapping("/{id}")
    public DailyLog update(@PathVariable Long id, @RequestBody DailyLog dailyLog, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DailyLog existing = dailyLogRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        dailyLog.setId(id);
        return dailyLogRepository.save(dailyLog);
    }
}
