package com.eattea.service;

import com.eattea.entity.KnowledgeEntry;
import com.eattea.mapper.KnowledgeEntryMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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

    /**
     * 从 Excel 文件批量导入词条
     * 模板列：术语 | 定义 | 分类 | 关联术语 | 出处（第一行为表头）
     */
    public int importFromExcel(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        Workbook workbook;
        if (filename != null && filename.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(file.getInputStream());
        } else {
            workbook = new HSSFWorkbook(file.getInputStream());
        }

        List<KnowledgeEntry> entries = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 跳过表头
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String term = getCellString(row, 0);
            String definition = getCellString(row, 1);
            if (term.isEmpty() || definition.isEmpty()) continue;

            KnowledgeEntry entry = new KnowledgeEntry();
            entry.setTerm(term);
            entry.setDefinition(definition);
            entry.setCategory(getCellString(row, 2));
            entry.setRelatedTerms(getCellString(row, 3));
            entry.setSource(getCellString(row, 4));
            entries.add(entry);
        }
        workbook.close();

        for (KnowledgeEntry entry : entries) {
            knowledgeEntryMapper.insert(entry);
        }
        return entries.size();
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            default -> "";
        };
    }
}
