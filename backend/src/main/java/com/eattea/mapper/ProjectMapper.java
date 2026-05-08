package com.eattea.mapper;

import com.eattea.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProjectMapper {

    List<Project> selectAll();

    List<Project> selectByStatus(@Param("status") String status);

    Project selectById(@Param("id") Long id);

    int insert(Project project);

    int update(Project project);

    int updateHealth(@Param("id") Long id, @Param("health") String health);

    int deleteById(@Param("id") Long id);
}
