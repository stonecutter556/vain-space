package com.vainspace.controller;

import com.vainspace.entity.DailyLog;
import com.vainspace.repository.DailyLogRepository;
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
    public List<DailyLog> getAll() {
        return dailyLogRepository.findAllByOrderByLogDateDesc();
    }

    @GetMapping("/today")
    public DailyLog getToday() {
        return dailyLogRepository.findByLogDate(LocalDate.now())
                .orElse(new DailyLog());
    }

    @PostMapping
    public DailyLog create(@RequestBody DailyLog dailyLog) {
        return dailyLogRepository.save(dailyLog);
    }

    @PutMapping("/{id}")
    public DailyLog update(@PathVariable Long id, @RequestBody DailyLog dailyLog) {
        dailyLog.setId(id);
        return dailyLogRepository.save(dailyLog);
    }
}
