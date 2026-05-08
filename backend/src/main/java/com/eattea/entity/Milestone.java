package com.eattea.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Milestone {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private LocalDate targetDate;
    private LocalDate actualDate;
    private String status;         // pending/achieved/missed
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
