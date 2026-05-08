package com.eattea.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 工时偏差预警
 */
@Data
public class HourDeviation {
    private Long taskId;
    private String taskTitle;
    private String assigneeName;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private double deviationPct;               // 偏差百分比（正=超时，负=节余）
    private String level;                      // high(>50%) / medium(30-50%) / normal(<30%)
}
