package com.eattea.mapper;

import com.eattea.entity.Stakeholder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StakeholderMapper {

    List<Stakeholder> selectByProject(@Param("projectId") Long projectId);

    Stakeholder selectById(@Param("id") Long id);

    int insert(Stakeholder stakeholder);

    int update(Stakeholder stakeholder);

    int deleteById(@Param("id") Long id);
}
