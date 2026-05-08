package com.eattea.mapper;

import com.eattea.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportMapper {

    List<Report> selectByProject(@Param("projectId") Long projectId);

    List<Report> selectByType(@Param("projectId") Long projectId, @Param("type") String type);

    Report selectById(@Param("id") Long id);

    int insert(Report report);

    int deleteById(@Param("id") Long id);

    int deleteByProject(@Param("projectId") Long projectId);
}
