package com.eattea.entity;

import lombok.Data;

/**
 * 分类标签
 */
@Data
public class Category {
    private Long id;
    private String name;            // 分类名称
    private String type;            // 类型: document / knowledge
    private Long parentId;          // 父分类ID，支持层级
}
