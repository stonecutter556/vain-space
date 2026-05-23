package com.vainspace.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_logs")
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "pomodoro_count")
    private Integer pomodoroCount;

    @Column(name = "mood_rating")
    private Integer moodRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (pomodoroCount == null) {
            pomodoroCount = 0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPomodoroCount() { return pomodoroCount; }
    public void setPomodoroCount(Integer pomodoroCount) { this.pomodoroCount = pomodoroCount; }
    public Integer getMoodRating() { return moodRating; }
    public void setMoodRating(Integer moodRating) { this.moodRating = moodRating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
