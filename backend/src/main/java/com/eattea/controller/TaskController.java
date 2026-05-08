package com.eattea.controller;

import com.eattea.entity.Task;
import com.eattea.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 按项目查任务列表
     */
    @GetMapping
    public ResponseEntity<List<Task>> list(@RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskService.listByProject(projectId));
    }

    /**
     * 任务详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> get(@PathVariable Long id) {
        Task t = taskService.getTask(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    /**
     * 新增任务
     */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.create(task));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        taskService.update(task);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量导入任务（Excel）
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId) {
        try {
            int count = taskService.importFromExcel(file, projectId);
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * 成员负载数据
     */
    @GetMapping("/load")
    public ResponseEntity<List<Map<String, Object>>> memberLoad(@RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(taskService.getMemberLoad(projectId));
    }
}
