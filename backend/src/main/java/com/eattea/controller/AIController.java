package com.eattea.controller;

import com.eattea.dto.AIResponse;
import com.eattea.entity.HealthCheck;
import com.eattea.entity.Report;
import com.eattea.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * 触发项目健康诊断
     */
    @PostMapping("/health-check/{projectId}")
    public ResponseEntity<AIResponse> healthCheck(@PathVariable Long projectId) {
        return ResponseEntity.ok(aiService.runHealthCheck(projectId));
    }

    /**
     * 获取最近一次健康诊断
     */
    @GetMapping("/health-check/{projectId}")
    public ResponseEntity<HealthCheck> getLatestHealth(@PathVariable Long projectId) {
        HealthCheck hc = aiService.getLatestHealthCheck(projectId);
        return hc != null ? ResponseEntity.ok(hc) : ResponseEntity.noContent().build();
    }

    /**
     * 生成报告（weekly/monthly/review）
     */
    @PostMapping("/report/{projectId}")
    public ResponseEntity<AIResponse> generateReport(
            @PathVariable Long projectId,
            @RequestParam(value = "type", defaultValue = "weekly") String type) {
        return ResponseEntity.ok(aiService.generateReport(projectId, type));
    }

    /**
     * 获取报告列表
     */
    @GetMapping("/reports/{projectId}")
    public ResponseEntity<List<Report>> getReports(
            @PathVariable Long projectId,
            @RequestParam(value = "type", required = false) String type) {
        return ResponseEntity.ok(aiService.getReports(projectId, type));
    }

    /**
     * 获取单篇报告详情
     */
    @GetMapping("/report/detail/{id}")
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        Report r = aiService.getReport(id);
        return r != null ? ResponseEntity.ok(r) : ResponseEntity.notFound().build();
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/report/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        aiService.deleteReport(id);
        return ResponseEntity.ok().build();
    }
}
