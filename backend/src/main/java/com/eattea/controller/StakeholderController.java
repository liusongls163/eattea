package com.eattea.controller;

import com.eattea.entity.Stakeholder;
import com.eattea.mapper.StakeholderMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stakeholders")
public class StakeholderController {

    private final StakeholderMapper stakeholderMapper;

    public StakeholderController(StakeholderMapper stakeholderMapper) {
        this.stakeholderMapper = stakeholderMapper;
    }

    @GetMapping
    public ResponseEntity<List<Stakeholder>> list(@RequestParam("projectId") Long projectId) {
        return ResponseEntity.ok(stakeholderMapper.selectByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<Stakeholder> create(@RequestBody Stakeholder s) {
        if (s.getInfluence() == null) s.setInfluence("normal");
        stakeholderMapper.insert(s);
        return ResponseEntity.ok(s);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Stakeholder s) {
        s.setId(id);
        stakeholderMapper.update(s);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stakeholderMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
