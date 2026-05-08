package com.eattea.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Report {
    private Long id;
    private Long projectId;
    private String type;           // weekly/monthly/review
    private String title;
    private String content;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime createTime;
}
