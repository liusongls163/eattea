package com.eattea.controller;

import com.eattea.dto.SearchResult;
import com.eattea.entity.Document;
import com.eattea.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<Document> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "docCategory", required = false) String docCategory,
            @RequestParam(value = "publishDate", required = false) String publishDate,
            @RequestParam(value = "tags", required = false) String tags) {
        try {
            LocalDate pd = (publishDate != null && !publishDate.isEmpty())
                    ? LocalDate.parse(publishDate) : null;
            Document doc = documentService.upload(file, department, docCategory, pd, tags);
            return ResponseEntity.ok(doc);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 全文检索（文档 + 知识库）
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> search(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(documentService.search(keyword));
    }

    /**
     * 文档列表（可筛选）
     */
    @GetMapping
    public ResponseEntity<List<Document>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "docCategory", required = false) String docCategory) {
        return ResponseEntity.ok(documentService.listDocuments(keyword, department, docCategory));
    }

    /**
     * 文档详情（含完整内容）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> get(@PathVariable Long id) {
        Document doc = documentService.getDocument(id);
        return doc != null ? ResponseEntity.ok(doc) : ResponseEntity.notFound().build();
    }

    /**
     * 更新文档元信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Document document) {
        document.setId(id);
        documentService.updateDocument(document);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }
}
