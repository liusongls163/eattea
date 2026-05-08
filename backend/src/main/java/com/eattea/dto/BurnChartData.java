package com.eattea.dto;

import lombok.Data;
import java.util.List;

/**
 * 燃尽图数据
 */
@Data
public class BurnChartData {
    private List<String> labels;           // 日期标签
    private List<Double> idealLine;        // 理想燃尽线（每天等量完成）
    private List<Double> actualLine;       // 实际完成线
    private List<Integer> cumulativeDone;  // 累计完成任务数
}
