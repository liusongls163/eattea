package com.eattea.mapper;

import com.eattea.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectByType(@Param("type") String type);

    int insert(Category category);
}
