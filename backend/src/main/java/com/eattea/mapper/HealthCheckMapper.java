package com.eattea.mapper;

import com.eattea.entity.HealthCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HealthCheckMapper {

    List<HealthCheck> selectByProject(@Param("projectId") Long projectId);

    HealthCheck selectLatest(@Param("projectId") Long projectId);

    int insert(HealthCheck healthCheck);

    int deleteByProject(@Param("projectId") Long projectId);
}
