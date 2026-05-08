package com.eattea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private boolean success;
    private String content;
    private String healthStatus;   // 仅 health_check 时有值
    private String error;
}
