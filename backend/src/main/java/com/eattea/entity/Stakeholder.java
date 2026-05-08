package com.eattea.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Stakeholder {
    private Long id;
    private Long projectId;
    private String name;
    private String role;
    private String department;
    private String influence;      // high/normal/low
    private String contact;
    private String expectations;
    private LocalDateTime createTime;
}
