package com.eattea.controller;

import com.eattea.dto.*;
import com.eattea.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** 燃尽图 */
    @GetMapping("/burn-chart/{projectId}")
    public ResponseEntity<BurnChartData> burnChart(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.burnChartData(projectId));
    }

    /** 延期预测 */
    @GetMapping("/delay-prediction/{projectId}")
    public ResponseEntity<List<DelayPrediction>> delayPrediction(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.predictDelays(projectId));
    }

    /** 阻塞影响分析 */
    @GetMapping("/blocked-impact/{projectId}")
    public ResponseEntity<List<ImpactAnalysis>> blockedImpact(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.analyzeBlockedImpact(projectId));
    }

    /** 工时偏差预警 */
    @GetMapping("/hour-deviation/{projectId}")
    public ResponseEntity<List<HourDeviation>> hourDeviation(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.getHourDeviations(projectId));
    }
}
