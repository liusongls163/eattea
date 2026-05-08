package com.eattea.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Risk {
    private Long id;
    private Long projectId;
    private String title;
    private String category;       // 技术风险/资源风险/进度风险/外部风险/合规风险
    private String probability;    // high/medium/low
    private String impact;         // high/medium/low
    private String level;          // high/medium/low
    private String status;         // open/mitigating/closed
    private String mitigation;
    private Long ownerId;
    private LocalDate identifiedDate;
    private LocalDate resolvedDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String ownerName;      // join
}
