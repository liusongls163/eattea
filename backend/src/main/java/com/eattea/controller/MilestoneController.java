package com.eattea.controller;

import com.eattea.entity.Milestone;
import com.eattea.mapper.MilestoneMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneMapper milestoneMapper;

    public MilestoneController(MilestoneMapper milestoneMapper) {
        this.milestoneMapper = milestoneMapper;
    }

    @GetMapping
    public ResponseEntity<List<Milestone>> list(@RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(milestoneMapper.selectByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<Milestone> create(@RequestBody Milestone m) {
        if (m.getStatus() == null) m.setStatus("pending");
        milestoneMapper.insert(m);
        return ResponseEntity.ok(m);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Milestone m) {
        m.setId(id);
        milestoneMapper.update(m);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        milestoneMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
