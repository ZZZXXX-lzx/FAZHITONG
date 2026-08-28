package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.LegalAid;
import com.fazhitong.consultation.service.LegalAidService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation/legal-aid")
@RequiredArgsConstructor
public class LegalAidController {

    private final LegalAidService legalAidService;

    /**
     * 申请
     */
    @PostMapping
    public ApiResult<LegalAid> apply(@RequestBody LegalAid legalAid) {
        return ApiResult.success(legalAidService.apply(legalAid));
    }

    /**
     * 我的申请
     */
    @GetMapping("/my")
    public ApiResult<PageResult<LegalAid>> my(
            @RequestParam Long userId, PageParam pageParam) {
        return ApiResult.success(legalAidService.my(userId, pageParam));
    }

    /**
     * 管理员列表
     */
    @GetMapping("/list")
    public ApiResult<PageResult<LegalAid>> list(
            @RequestParam(required = false) Integer status,
            PageParam pageParam) {
        return ApiResult.success(legalAidService.list(status, pageParam));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ApiResult<LegalAid> getById(@PathVariable Long id) {
        return ApiResult.success(legalAidService.getById(id));
    }

    /**
     * 审核
     */
    @PostMapping("/{id}/audit")
    public ApiResult<LegalAid> audit(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam String remark,
            @RequestParam(required = false) Long assignedLawyerId) {
        return ApiResult.success(
                legalAidService.audit(id, status, remark, assignedLawyerId));
    }
}
