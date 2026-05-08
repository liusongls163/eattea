package com.eattea.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Long assigneeId;
    private String status;         // todo/in_progress/done/blocked
    private String priority;       // high/normal/low
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer progress;      // 0-100
    private String tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 联表查询字段（非数据库映射）
    private String assigneeName;
    private String projectName;
}
