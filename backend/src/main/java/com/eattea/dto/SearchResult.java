package com.eattea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String type;            // document / knowledge
    private Long id;
    private String title;           // 标题/术语名
    private String highlight;       // 匹配片段高亮
    private String category;        // 分类
    private String department;      // 部门（文档类型时有值）
}
