package com.eattea.dto;

import lombok.Data;

/**
 * 延期预测
 */
@Data
public class DelayPrediction {
    private Long taskId;
    private String taskTitle;
    private String assigneeName;
    private String currentStatus;
    private int currentProgress;
    private double dailyVelocity;          // 日均进度百分比
    private int daysRemaining;             // 距离截止剩余天数
    private double predictedFinalProgress; // 预测最终进度
    private String riskLevel;              // high/medium/low
    private String suggestion;             // 建议措施
}
