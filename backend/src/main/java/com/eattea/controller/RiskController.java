package com.eattea.controller;

import com.eattea.entity.Risk;
import com.eattea.mapper.RiskMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskMapper riskMapper;

    public RiskController(RiskMapper riskMapper) {
        this.riskMapper = riskMapper;
    }

    @GetMapping
    public ResponseEntity<List<Risk>> list(@RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(riskMapper.selectByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<Risk> create(@RequestBody Risk r) {
        if (r.getProbability() == null) r.setProbability("medium");
        if (r.getImpact() == null) r.setImpact("medium");
        if (r.getStatus() == null) r.setStatus("open");
        r.setLevel(computeLevel(r.getProbability(), r.getImpact()));
        riskMapper.insert(r);
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Risk r) {
        r.setId(id);
        if (r.getProbability() != null && r.getImpact() != null) {
            r.setLevel(computeLevel(r.getProbability(), r.getImpact()));
        }
        riskMapper.update(r);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        riskMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private String computeLevel(String prob, String imp) {
        int p = "high".equals(prob) ? 3 : "medium".equals(prob) ? 2 : 1;
        int i = "high".equals(imp) ? 3 : "medium".equals(imp) ? 2 : 1;
        int score = p * i;
        if (score >= 6) return "high";
        if (score >= 3) return "medium";
        return "low";
    }
}
