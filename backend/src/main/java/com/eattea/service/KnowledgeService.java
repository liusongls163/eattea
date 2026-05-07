package com.eattea.service;

import com.eattea.entity.KnowledgeEntry;
import com.eattea.mapper.KnowledgeEntryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeEntryMapper knowledgeEntryMapper;

    public KnowledgeService(KnowledgeEntryMapper knowledgeEntryMapper) {
        this.knowledgeEntryMapper = knowledgeEntryMapper;
    }

    public List<KnowledgeEntry> listAll() {
        return knowledgeEntryMapper.selectAll();
    }

    public List<KnowledgeEntry> listByCategory(String category) {
        return knowledgeEntryMapper.selectByCategory(category);
    }

    public KnowledgeEntry getEntry(Long id) {
        return knowledgeEntryMapper.selectById(id);
    }

    public KnowledgeEntry create(KnowledgeEntry entry) {
        knowledgeEntryMapper.insert(entry);
        return entry;
    }

    public void update(KnowledgeEntry entry) {
        knowledgeEntryMapper.update(entry);
    }

    public void delete(Long id) {
        knowledgeEntryMapper.deleteById(id);
    }
}
