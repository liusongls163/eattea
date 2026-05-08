package com.eattea.dto;

import lombok.Data;
import java.util.List;

/**
 * 任务依赖影响分析
 */
@Data
public class ImpactAnalysis {
    private Long blockedTaskId;
    private String blockedTaskTitle;
    private String blockedTaskStatus;
    private String reason;                     // 阻塞原因简述

    // 受影响的任务（直接和间接依赖）
    private List<AffectedTask> affectedTasks;
    private int totalAffected;                 // 受影响任务总数
    private int totalEstimatedHours;           // 受影响总工时
    private String riskSummary;                // 影响摘要

    @Data
    public static class AffectedTask {
        private Long taskId;
        private String title;
        private String status;
        private String assigneeName;
        private int distance;                  // 依赖深度：1=直接依赖，2=间接
    }
}
