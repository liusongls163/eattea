package com.eattea.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 金融知识词条
 */
@Data
public class KnowledgeEntry {
    private Long id;
    private String term;            // 术语
    private String definition;      // 定义/解释
    private String category;        // 分类：票据、同业、债券、衍生品等
    private String relatedTerms;    // 关联术语
    private String source;          // 出处
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
