package com.fazhitong.contract.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.contract.entity.LegalReview;
import com.fazhitong.contract.service.LegalReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract/legal-review")
@RequiredArgsConstructor
public class LegalReviewController {

    private final LegalReviewService legalReviewService;

    @PostMapping
    public ApiResult<LegalReview> create(@RequestBody LegalReview record) {
        return ApiResult.success(legalReviewService.create(record));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<LegalReview>> list(
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) String status,
            PageParam pageParam) {
        return ApiResult.success(legalReviewService.list(enterpriseId, reviewType, status, pageParam));
    }

    @PutMapping
    public ApiResult<LegalReview> update(@RequestBody LegalReview record) {
        return ApiResult.success(legalReviewService.update(record));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        legalReviewService.delete(id);
        return ApiResult.success();
    }

    @PostMapping("/{id}/review")
    public ApiResult<LegalReview> review(@PathVariable Long id, @RequestBody LegalReview record) {
        return ApiResult.success(legalReviewService.review(id, record));
    }
}
