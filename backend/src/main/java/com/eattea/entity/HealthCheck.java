package com.eattea.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HealthCheck {
    private Long id;
    private Long projectId;
    private LocalDateTime checkDate;
    private String healthStatus;   // green/yellow/red
    private Integer overdueTasks;
    private Integer blockedTasks;
    private String riskSummary;
    private String suggestions;
    private String memberLoad;     // JSON
    private LocalDateTime createTime;
}
