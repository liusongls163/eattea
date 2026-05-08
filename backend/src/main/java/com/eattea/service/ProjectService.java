package com.eattea.service;

import com.eattea.dto.ProjectStats;
import com.eattea.entity.Project;
import com.eattea.entity.Task;
import com.eattea.mapper.ProjectMapper;
import com.eattea.mapper.TaskMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    public ProjectService(ProjectMapper projectMapper, TaskMapper taskMapper) {
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
    }

    public List<Project> listAll() {
        return projectMapper.selectAll();
    }

    public List<Project> listByStatus(String status) {
        return projectMapper.selectByStatus(status);
    }

    public Project getProject(Long id) {
        return projectMapper.selectById(id);
    }

    public Project create(Project project) {
        if (project.getStatus() == null) project.setStatus("active");
        projectMapper.insert(project);
        return project;
    }

    public void update(Project project) {
        projectMapper.update(project);
    }

    public void delete(Long id) {
        // 删除关联数据
        taskMapper.deleteByProject(id);
        projectMapper.deleteById(id);
    }

    /**
     * 获取所有活跃项目的统计摘要（仪表盘用）
     */
    public List<ProjectStats> getActiveProjectStats() {
        List<Project> projects = projectMapper.selectByStatus("active");
        List<ProjectStats> statsList = new ArrayList<>();
        for (Project p : projects) {
            statsList.add(buildStats(p));
        }
        return statsList;
    }

    /**
     * 单个项目统计
     */
    public ProjectStats getProjectStats(Long projectId) {
        Project p = projectMapper.selectById(projectId);
        if (p == null) return null;
        return buildStats(p);
    }

    private ProjectStats buildStats(Project p) {
        List<Task> tasks = taskMapper.selectByProject(p.getId());
        LocalDate today = LocalDate.now();

        int total = tasks.size();
        int done = 0, inProgress = 0, todo = 0, blocked = 0, overdue = 0;
        BigDecimal estimatedTotal = BigDecimal.ZERO;
        BigDecimal actualTotal = BigDecimal.ZERO;

        for (Task t : tasks) {
            switch (t.getStatus()) {
                case "done" -> done++;
                case "in_progress" -> inProgress++;
                case "todo" -> todo++;
                case "blocked" -> blocked++;
            }
            if (t.getEstimatedHours() != null) estimatedTotal = estimatedTotal.add(t.getEstimatedHours());
            if (t.getActualHours() != null) actualTotal = actualTotal.add(t.getActualHours());

            // 判断是否逾期：截止日期已过但未完成
            if (!"done".equals(t.getStatus()) && !"blocked".equals(t.getStatus())
                    && t.getDueDate() != null && t.getDueDate().isBefore(today)) {
                overdue++;
            }
            // blocked 且逾期也算
            if ("blocked".equals(t.getStatus()) && t.getDueDate() != null && t.getDueDate().isBefore(today)) {
                overdue++;
            }
        }

        double completionPct = total > 0 ? (double) done / total * 100 : 0;

        // 优先用数据库/ AI 诊断的健康度，否则用规则引擎
        String health = p.getHealth() != null ? p.getHealth() : computeHealth(done, blocked, overdue, total);

        ProjectStats stats = new ProjectStats();
        stats.setProjectId(p.getId());
        stats.setProjectName(p.getName());
        stats.setHealth(health);
        stats.setDescription(p.getDescription());
        stats.setStatus(p.getStatus());
        stats.setStartDate(p.getStartDate() != null ? p.getStartDate().toString() : "");
        stats.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : "");
        stats.setTotalTasks(total);
        stats.setDoneTasks(done);
        stats.setInProgressTasks(inProgress);
        stats.setTodoTasks(todo);
        stats.setBlockedTasks(blocked);
        stats.setOverdueTasks(overdue);
        stats.setCompletionPct(Math.round(completionPct * 10.0) / 10.0);
        stats.setEstimatedTotalHours(estimatedTotal);
        stats.setActualTotalHours(actualTotal);

        return stats;
    }

    /**
     * 规则引擎：红黄绿判定
     */
    private String computeHealth(int done, int blocked, int overdue, int total) {
        if (total == 0) return "green";
        // 红色：有过期任务且超过20%的任务有问题
        if (overdue > 0 && (double)(overdue + blocked) / total > 0.2) return "red";
        // 黄色：有过期或阻塞任务
        if (overdue > 0 || blocked > 0) return "yellow";
        // 黄色：进度低于预期（用简化判断：几乎没有完成的任务）
        if (total > 5 && done == 0) return "yellow";
        return "green";
    }

    public void updateHealth(Long id, String health) {
        projectMapper.updateHealth(id, health);
    }
}
