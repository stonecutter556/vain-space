package com.vainspace.repository;

import com.vainspace.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByLogDate(LocalDate logDate);
    List<DailyLog> findAllByOrderByLogDateDesc();
}
