package com.eattea.controller;

import com.eattea.entity.KnowledgeEntry;
import com.eattea.service.KnowledgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /**
     * 全部词条或按分类筛选
     */
    @GetMapping
    public ResponseEntity<List<KnowledgeEntry>> list(
            @RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(knowledgeService.listByCategory(category));
        }
        return ResponseEntity.ok(knowledgeService.listAll());
    }

    /**
     * 词条详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeEntry> get(@PathVariable Long id) {
        KnowledgeEntry entry = knowledgeService.getEntry(id);
        return entry != null ? ResponseEntity.ok(entry) : ResponseEntity.notFound().build();
    }

    /**
     * 新增词条
     */
    @PostMapping
    public ResponseEntity<KnowledgeEntry> create(@RequestBody KnowledgeEntry entry) {
        return ResponseEntity.ok(knowledgeService.create(entry));
    }

    /**
     * 更新词条
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody KnowledgeEntry entry) {
        entry.setId(id);
        knowledgeService.update(entry);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除词条
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有分类列表（去重）
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        List<KnowledgeEntry> all = knowledgeService.listAll();
        List<String> categories = all.stream()
                .map(KnowledgeEntry::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
