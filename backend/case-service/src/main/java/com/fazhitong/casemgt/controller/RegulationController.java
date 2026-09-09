package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.entity.RegulationArticle;
import com.fazhitong.casemgt.service.RegulationService;
import com.fazhitong.casemgt.dto.RegulationImportRow;
import com.fazhitong.casemgt.dto.ImportResult;
import com.fazhitong.casemgt.util.RegulationFileParser;
import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/case/regulation")
@RequiredArgsConstructor
public class RegulationController {

    private final RegulationService regulationService;

    @GetMapping("/search")
    public ApiResult<PageResult<Regulation>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lawType,
            PageParam pageParam) {
        return ApiResult.success(regulationService.search(keyword, lawType, pageParam));
    }

    @GetMapping("/{id}")
    public ApiResult<Regulation> getById(@PathVariable Long id) {
        return ApiResult.success(regulationService.getById(id));
    }

    @GetMapping("/{id}/detail")
    public ApiResult<Regulation> detail(@PathVariable Long id) {
        return ApiResult.success(regulationService.getDetail(id));
    }

    @GetMapping("/{id}/articles")
    public ApiResult<List<RegulationArticle>> articles(@PathVariable Long id) {
        return ApiResult.success(regulationService.listArticles(id));
    }

    @GetMapping("/resolve")
    public ApiResult<List<java.util.Map<String, Object>>> resolveRefs(@RequestParam String quote) {
        return ApiResult.success(regulationService.resolveRefs(quote));
    }

    // ---------- 管理端 CRUD ----------

    @PostMapping
    public ApiResult<Regulation> create(@RequestBody Regulation regulation) {
        return ApiResult.success(regulationService.create(regulation));
    }

    @PutMapping
    public ApiResult<Regulation> update(@RequestBody Regulation regulation) {
        return ApiResult.success(regulationService.update(regulation));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        regulationService.delete(id);
        return ApiResult.success();
    }

    @PostMapping("/{id}/articles")
    public ApiResult<RegulationArticle> createArticle(@PathVariable Long id,
                                                      @RequestBody RegulationArticle article) {
        article.setRegulationId(id);
        regulationService.createArticle(article);
        return ApiResult.success(article);
    }

    @PutMapping("/articles/{articleId}")
    public ApiResult<RegulationArticle> updateArticle(@PathVariable Long articleId,
                                                      @RequestBody RegulationArticle article) {
        article.setId(articleId);
        regulationService.updateArticle(article);
        return ApiResult.success(article);
    }

    @DeleteMapping("/articles/{articleId}")
    public ApiResult<Void> deleteArticle(@PathVariable Long articleId) {
        regulationService.deleteArticle(articleId);
        return ApiResult.success();
    }

    @PostMapping("/import")
    public ApiResult<Integer> importRegulations(@RequestBody List<Regulation> list) {
        return ApiResult.success(regulationService.importRegulations(list));
    }

    @PostMapping("/import-file")
    public ApiResult<ImportResult> importFile(@RequestParam("file") MultipartFile file) throws IOException {
        List<RegulationImportRow> rows = RegulationFileParser.parse(
                file.getOriginalFilename() == null ? "data.csv" : file.getOriginalFilename(),
                file.getInputStream());
        return ApiResult.success(regulationService.importRows(rows));
    }
}