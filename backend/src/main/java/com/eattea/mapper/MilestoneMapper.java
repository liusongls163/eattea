package com.eattea.mapper;

import com.eattea.entity.Milestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MilestoneMapper {

    List<Milestone> selectByProject(@Param("projectId") Long projectId);

    Milestone selectById(@Param("id") Long id);

    int insert(Milestone milestone);

    int update(Milestone milestone);

    int deleteById(@Param("id") Long id);
}
