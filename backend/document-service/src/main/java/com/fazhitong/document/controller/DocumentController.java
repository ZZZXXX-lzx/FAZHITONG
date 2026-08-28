package com.fazhitong.document.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.document.entity.DocumentTemplate;
import com.fazhitong.document.entity.DocumentRecord;
import com.fazhitong.document.entity.DocumentCategory;
import com.fazhitong.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/categories")
    public ApiResult<List<DocumentCategory>> listCategories() {
        return ApiResult.success(documentService.listCategories());
    }

    @GetMapping("/templates")
    public ApiResult<PageResult<DocumentTemplate>> listTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            PageParam pageParam) {
        return ApiResult.success(documentService.listTemplates(category, keyword, pageParam));
    }

    @GetMapping("/templates/{id}")
    public ApiResult<DocumentTemplate> getTemplate(@PathVariable Long id) {
        return ApiResult.success(documentService.getTemplate(id));
    }

    @PostMapping("/templates")
    public ApiResult<DocumentTemplate> createTemplate(@RequestBody DocumentTemplate template) {
        return ApiResult.success(documentService.createTemplate(template));
    }

    @PutMapping("/templates/{id}")
    public ApiResult<DocumentTemplate> updateTemplate(@PathVariable Long id, @RequestBody DocumentTemplate template) {
        template.setId(id);
        return ApiResult.success(documentService.updateTemplate(template));
    }

    @PostMapping("/templates/{id}/status")
    public ApiResult<Void> toggleTemplateStatus(@PathVariable Long id, @RequestParam int status) {
        documentService.updateTemplateStatus(id, status);
        return ApiResult.success();
    }

    @PostMapping("/generate")
    public ApiResult<DocumentRecord> generate(
            @RequestParam Long userId,
            @RequestParam Long templateId,
            @RequestBody Map<String, String> fields) {
        return ApiResult.success(documentService.generate(userId, templateId, fields));
    }

    @GetMapping("/records")
    public ApiResult<List<DocumentRecord>> myRecords(@RequestParam Long userId) {
        return ApiResult.success(documentService.myRecords(userId));
    }

    /**
     * AI 文书起草（输入文书类型 + 需求描述，返回生成草稿）
     */
    @PostMapping("/ai-draft")
    public ApiResult<String> aiDraft(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("type", "法律文书");
        String description = body.getOrDefault("description", "");
        return ApiResult.success(documentService.aiDraft(type, description));
    }
}
