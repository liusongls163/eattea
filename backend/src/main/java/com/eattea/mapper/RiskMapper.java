package com.eattea.mapper;

import com.eattea.entity.Risk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RiskMapper {

    List<Risk> selectByProject(@Param("projectId") Long projectId);

    Risk selectById(@Param("id") Long id);

    int insert(Risk risk);

    int update(Risk risk);

    int deleteById(@Param("id") Long id);
}
