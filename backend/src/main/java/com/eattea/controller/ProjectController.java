package com.eattea.controller;

import com.eattea.dto.AIResponse;
import com.eattea.dto.ProjectStats;
import com.eattea.entity.Project;
import com.eattea.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 所有项目（可筛选状态）
     */
    @GetMapping
    public ResponseEntity<List<Project>> list(
            @RequestParam(value = "status", required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(projectService.listByStatus(status));
        }
        return ResponseEntity.ok(projectService.listAll());
    }

    /**
     * 项目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> get(@PathVariable Long id) {
        Project p = projectService.getProject(id);
        return p != null ? ResponseEntity.ok(p) : ResponseEntity.notFound().build();
    }

    /**
     * 项目统计
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStats> stats(@PathVariable Long id) {
        ProjectStats s = projectService.getProjectStats(id);
        return s != null ? ResponseEntity.ok(s) : ResponseEntity.notFound().build();
    }

    /**
     * 所有活跃项目统计（仪表盘）
     */
    @GetMapping("/stats/active")
    public ResponseEntity<List<ProjectStats>> activeStats() {
        return ResponseEntity.ok(projectService.getActiveProjectStats());
    }

    /**
     * 新增项目
     */
    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.create(project));
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectService.update(project);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok().build();
    }
}
