package com.eattea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 项目统计摘要（驾驶舱首页用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStats {
    private Long projectId;
    private String projectName;
    private String health;                      // green/yellow/red
    private String description;                 // 项目描述
    private String status;                      // active/closed/paused
    private String startDate;
    private String endDate;

    // 任务统计
    private int totalTasks;
    private int doneTasks;
    private int inProgressTasks;
    private int todoTasks;
    private int blockedTasks;
    private int overdueTasks;                   // 截止日期已过但未完成
    private double completionPct;               // 完成百分比

    // 工时统计
    private BigDecimal estimatedTotalHours;
    private BigDecimal actualTotalHours;

    // 风险摘要
    private String riskSummary;                 // AI 生成
}
