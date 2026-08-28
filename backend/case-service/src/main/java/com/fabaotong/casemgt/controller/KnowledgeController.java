package com.fabaotong.casemgt.controller;

import com.fabaotong.casemgt.entity.KnowledgeArticle;
import com.fabaotong.casemgt.entity.KnowledgeCategory;
import com.fabaotong.casemgt.service.KnowledgeService;
import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/case/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    public ApiResult<List<KnowledgeCategory>> categories() {
        return ApiResult.success(knowledgeService.listCategories());
    }

    /**
     * 分页文章
     */
    @GetMapping("/articles")
    public ApiResult<PageResult<KnowledgeArticle>> articles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            PageParam pageParam) {
        return ApiResult.success(knowledgeService.listArticles(categoryId, keyword, pageParam));
    }

    /**
     * 文章详情
     */
    @GetMapping("/articles/{id}")
    public ApiResult<KnowledgeArticle> article(@PathVariable Long id) {
        return ApiResult.success(knowledgeService.getArticle(id));
    }

    /**
     * 创建文章
     */
    @PostMapping("/articles")
    public ApiResult<KnowledgeArticle> create(@RequestBody KnowledgeArticle article) {
        return ApiResult.success(knowledgeService.createArticle(article));
    }

    /**
     * 更新文章
     */
    @PutMapping("/articles/{id}")
    public ApiResult<KnowledgeArticle> update(@PathVariable Long id,
                                               @RequestBody KnowledgeArticle article) {
        article.setId(id);
        return ApiResult.success(knowledgeService.updateArticle(article));
    }

    /**
     * 上下架
     */
    @PostMapping("/articles/{id}/status")
    public ApiResult<Void> toggleStatus(@PathVariable Long id,
                                        @RequestParam Integer status) {
        knowledgeService.toggleStatus(id, status);
        return ApiResult.success();
    }
}
