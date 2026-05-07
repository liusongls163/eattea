package com.eattea.mapper;

import com.eattea.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DocumentMapper {

    int insert(Document document);

    Document selectById(Long id);

    List<Document> selectList(@Param("keyword") String keyword,
                              @Param("department") String department,
                              @Param("docCategory") String docCategory);

    /**
     * MySQL FULLTEXT + ngram 中文搜索
     */
    List<Document> searchByFulltext(@Param("keyword") String keyword);

    int update(Document document);

    int deleteById(Long id);
}
