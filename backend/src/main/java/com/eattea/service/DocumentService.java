package com.eattea.service;

import com.eattea.dto.SearchResult;
import com.eattea.entity.Document;
import com.eattea.entity.KnowledgeEntry;
import com.eattea.mapper.DocumentMapper;
import com.eattea.mapper.KnowledgeEntryMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final KnowledgeEntryMapper knowledgeEntryMapper;

    @Value("${eattea.upload-path}")
    private String uploadPath;

    public DocumentService(DocumentMapper documentMapper, KnowledgeEntryMapper knowledgeEntryMapper) {
        this.documentMapper = documentMapper;
        this.knowledgeEntryMapper = knowledgeEntryMapper;
    }

    /**
     * 上传并解析文档
     */
    public Document upload(MultipartFile file, String department, String docCategory,
                           LocalDate publishDate, String tags) throws IOException {
        // 保存文件
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        String savedName = UUID.randomUUID() + "." + ext;
        File dest = new File(dir, savedName);
        file.transferTo(dest);

        // 提取文本内容
        String content = extractText(dest, ext);

        // 生成标题：优先用文件名去掉扩展名
        String title = originalName != null && originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : originalName;

        Document doc = new Document();
        doc.setTitle(title);
        doc.setFileName(originalName);
        doc.setFileType(ext);
        doc.setFilePath(dest.getAbsolutePath());
        doc.setContent(content);
        doc.setDepartment(department);
        doc.setDocCategory(docCategory);
        doc.setPublishDate(publishDate);
        doc.setTags(tags);

        documentMapper.insert(doc);
        return doc;
    }

    /**
     * 全文检索 —— 文档 + 知识库联合搜索
     */
    public List<SearchResult> search(String keyword) {
        // 预处理关键词：MySQL FULLTEXT 需要用 + 前缀表示必须包含
        String ftKeyword = "+" + keyword.replaceAll("\\s+", " +");

        List<SearchResult> results = new ArrayList<>();

        // 搜索文档
        List<Document> docs = documentMapper.searchByFulltext(ftKeyword);
        for (Document doc : docs) {
            String highlight = extractHighlight(doc.getContent(), keyword, 150);
            results.add(new SearchResult(
                    "document", doc.getId(), doc.getTitle(),
                    highlight, doc.getDocCategory(), doc.getDepartment()
            ));
        }

        // 搜索知识库
        List<KnowledgeEntry> entries = knowledgeEntryMapper.searchByFulltext(ftKeyword);
        for (KnowledgeEntry entry : entries) {
            String highlight = extractHighlight(entry.getDefinition(), keyword, 150);
            results.add(new SearchResult(
                    "knowledge", entry.getId(), entry.getTerm(),
                    highlight, entry.getCategory(), null
            ));
        }

        return results;
    }

    public Document getDocument(Long id) {
        return documentMapper.selectById(id);
    }

    public List<Document> listDocuments(String keyword, String department, String docCategory) {
        return documentMapper.selectList(keyword, department, docCategory);
    }

    public void updateDocument(Document document) {
        documentMapper.update(document);
    }

    public void deleteDocument(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc != null) {
            // 删除物理文件
            File file = new File(doc.getFilePath());
            if (file.exists()) file.delete();
            documentMapper.deleteById(id);
        }
    }

    /**
     * 根据文件类型提取文本
     */
    private String extractText(File file, String ext) throws IOException {
        return switch (ext.toLowerCase()) {
            case "pdf" -> extractPdfText(file);
            case "docx", "doc" -> extractWordText(file);
            case "xlsx", "xls" -> extractExcelText(file);
            default -> "";
        };
    }

    private String extractPdfText(File file) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(pdf);
        }
    }

    private String extractWordText(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
            return extractor.getText();
        }
    }

    private String extractExcelText(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx")
                     ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                sb.append("【Sheet: ").append(sheet.getSheetName()).append("】\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String val = getCellValue(cell);
                        if (!val.isEmpty()) {
                            sb.append(val).append("\t");
                        }
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) && !Double.isInfinite(val)
                        ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private String extractHighlight(String content, String keyword, int contextLen) {
        if (content == null || content.isEmpty()) return "";
        int idx = content.indexOf(keyword);
        if (idx < 0) {
            // 全文索引匹配时，可能不是精确子串匹配，返回前150字
            return content.length() > contextLen ? content.substring(0, contextLen) + "…" : content;
        }
        int start = Math.max(0, idx - contextLen / 2);
        int end = Math.min(content.length(), idx + keyword.length() + contextLen / 2);
        return (start > 0 ? "…" : "") + content.substring(start, end) + (end < content.length() ? "…" : "");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
