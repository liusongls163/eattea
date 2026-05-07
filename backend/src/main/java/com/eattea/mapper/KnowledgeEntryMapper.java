package com.eattea.mapper;

import com.eattea.entity.KnowledgeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeEntryMapper {

    int insert(KnowledgeEntry entry);

    KnowledgeEntry selectById(Long id);

    List<KnowledgeEntry> selectByCategory(@Param("category") String category);

    List<KnowledgeEntry> selectAll();

    /**
     * MySQL FULLTEXT + ngram 中文搜索
     */
    List<KnowledgeEntry> searchByFulltext(@Param("keyword") String keyword);

    int update(KnowledgeEntry entry);

    int deleteById(Long id);
}
