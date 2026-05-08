package com.eattea.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDependency {
    private Long id;
    private Long taskId;
    private Long dependsOnId;
    private LocalDateTime createTime;
}
