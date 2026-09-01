package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.LawyerCase;
import com.fazhitong.consultation.service.LawyerCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consultation/case")
@RequiredArgsConstructor
public class LawyerCaseController {

    private final LawyerCaseService caseService;

    @PostMapping
    public ApiResult<LawyerCase> create(@RequestBody LawyerCase c) {
        return ApiResult.success(caseService.create(c));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<LawyerCase>> list(
            @RequestParam(required = false) Long lawyerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            PageParam pageParam) {
        return ApiResult.success(caseService.list(lawyerId, status, keyword, pageParam));
    }

    @PutMapping
    public ApiResult<LawyerCase> update(@RequestBody LawyerCase c) {
        return ApiResult.success(caseService.update(c));
    }

    @PostMapping("/{id}/transition")
    public ApiResult<LawyerCase> transition(@PathVariable Long id, @RequestParam String action) {
        return ApiResult.success(caseService.transition(id, action));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        caseService.delete(id);
        return ApiResult.success();
    }

    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats(@RequestParam Long lawyerId) {
        return ApiResult.success(caseService.stats(lawyerId));
    }
}
