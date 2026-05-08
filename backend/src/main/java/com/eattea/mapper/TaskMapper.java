package com.eattea.mapper;

import com.eattea.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TaskMapper {

    List<Task> selectByProject(@Param("projectId") Long projectId);

    List<Task> selectByProjectWithMember(@Param("projectId") Long projectId);

    Task selectById(@Param("id") Long id);

    List<Task> selectByAssignee(@Param("assigneeId") Long assigneeId);

    List<Task> selectBlockedByProject(@Param("projectId") Long projectId);

    int insert(Task task);

    int update(Task task);

    int deleteById(@Param("id") Long id);

    int deleteByProject(@Param("projectId") Long projectId);
}
