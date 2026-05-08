package com.eattea.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Member {
    private Long id;
    private String name;
    private String role;           // PM/开发/测试/实施
    private String email;
    private String department;
    private LocalDateTime createTime;
}
