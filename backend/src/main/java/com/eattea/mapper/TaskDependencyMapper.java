package com.eattea.mapper;

import com.eattea.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TaskDependencyMapper {

    /** 查某任务依赖哪些任务（前置任务） */
    List<TaskDependency> selectByTaskId(@Param("taskId") Long taskId);

    /** 查哪些任务依赖某任务（后续任务） */
    List<TaskDependency> selectByDependsOn(@Param("dependsOnId") Long dependsOnId);

    /** 查某项目的所有依赖关系 */
    List<TaskDependency> selectByProject(@Param("projectId") Long projectId);

    int insert(TaskDependency dep);

    int deleteById(@Param("id") Long id);

    int deleteByTaskId(@Param("taskId") Long taskId);
}
