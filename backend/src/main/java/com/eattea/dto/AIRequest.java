package com.eattea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发给 LLM 的请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String prompt;
    private String context;        // 附加上下文数据
    private String type;           // health_check / weekly_report / monthly_report / review
}
