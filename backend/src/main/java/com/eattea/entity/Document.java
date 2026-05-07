package com.eattea.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 监管文档
 */
@Data
public class Document {
    private Long id;
    private String title;           // 文档标题
    private String fileName;        // 原始文件名
    private String fileType;        // 文件类型: pdf/doc/docx/xls/xlsx
    private String filePath;        // 存储路径
    private String content;         // 提取的文本内容（全文检索用）
    private String department;      // 所属部门
    private String docCategory;     // 文档分类/报送类型
    private LocalDate publishDate;  // 发布日期
    private String tags;            // 标签，逗号分隔
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
